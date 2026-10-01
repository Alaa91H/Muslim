package org.muslim.app.core.cast

import java.io.Closeable
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Token-protected, temporary HTTP source for app-private audio files during an active Cast session. */
class LocalCastMediaServer(
    private val file: File,
    private val ttlMs: Long = TimeUnit.HOURS.toMillis(4),
    private val nowMs: () -> Long = System::currentTimeMillis,
) : Closeable {
    private val token = ByteArray(32).also(SecureRandom()::nextBytes).let {
        Base64.getUrlEncoder().withoutPadding().encodeToString(it)
    }
    private val startedAt = nowMs()
    private val pool = Executors.newCachedThreadPool()
    @Volatile private var closed = false
    private var socket: ServerSocket? = null
    private var address: String? = null

    private data class Request(val method: String, val path: String, val headers: Map<String, String>)

    @Synchronized fun start(): String {
        check(file.isFile && file.length() > 0L) { "Recitation file is unavailable" }
        if (socket != null) return mediaUrl()
        val localAddress = activeIpv4Address() ?: error("No local network address is available")
        val server = ServerSocket(0, 8, localAddress)
        socket = server
        address = localAddress.hostAddress
        pool.execute {
            while (!closed) runCatching { server.accept() }.onSuccess { client -> pool.execute { serve(client) } }
        }
        return mediaUrl()
    }

    @Synchronized private fun mediaUrl(): String = "http://${address}:${socket?.localPort}/$token/audio.mp3"

    private fun serve(client: Socket) = client.use { peer ->
        peer.soTimeout = 5_000
        val request = readRequest(peer) ?: return@use
        if (nowMs() - startedAt > ttlMs || request.path != "/$token/audio.mp3") {
            respond(peer, "404 Not Found", "Content-Length: 0\r\n")
            return@use
        }
        if (request.method != "GET" && request.method != "HEAD") {
            respond(peer, "405 Method Not Allowed", "Allow: GET, HEAD\r\nContent-Length: 0\r\n")
            return@use
        }
        serveFile(peer, request)
    }

    private fun readRequest(peer: Socket): Request? {
        val input = peer.getInputStream().bufferedReader(Charsets.US_ASCII)
        val line = input.readLine() ?: return null
        val parts = line.split(' ')
        val method = parts.getOrNull(0) ?: return null
        val path = parts.getOrNull(1) ?: return null
        val headers = mutableMapOf<String, String>()
        while (true) {
            val line = input.readLine() ?: break
            if (line.isEmpty()) break
            val colon = line.indexOf(':')
            if (colon > 0) headers[line.substring(0, colon).trim().lowercase()] = line.substring(colon + 1).trim()
        }
        return Request(method, path, headers)
    }

    private fun serveFile(peer: Socket, request: Request) {
        val length = file.length()
        val rangeHeader = request.headers["range"]
        val range = if (rangeHeader == null) null else ByteRange.parse(rangeHeader, length)
        if (rangeHeader != null && range == null) {
            respond(peer, "416 Range Not Satisfiable", "Content-Range: bytes */$length\r\nContent-Length: 0\r\n")
            return
        }
        val start = range?.start ?: 0L
        val bytes = range?.length ?: length
        val status = if (range == null) "200 OK" else "206 Partial Content"
        val extra = buildString {
            append("Content-Type: audio/mpeg\r\nContent-Length: $bytes\r\nAccept-Ranges: bytes\r\n")
            if (range != null) append("Content-Range: bytes ${range.start}-${range.endInclusive}/$length\r\n")
        }
        respond(peer, status, extra)
        if (request.method == "HEAD") return
        file.inputStream().use { stream ->
            copyRange(stream, peer, start, bytes)
        }
    }

    private fun copyRange(stream: java.io.InputStream, peer: Socket, start: Long, byteCount: Long) {
        var skipped = 0L
        while (skipped < start) {
            val count = stream.skip(start - skipped)
            if (count > 0L) skipped += count else if (stream.read() >= 0) skipped++ else return
        }
        val buffer = ByteArray(16 * 1024)
        var remaining = byteCount
        while (remaining > 0L) {
            val read = stream.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            if (read <= 0) break
            peer.getOutputStream().write(buffer, 0, read)
            remaining -= read
        }
    }

    private fun respond(client: Socket, status: String, headers: String) {
        client.getOutputStream().write("HTTP/1.1 $status\r\nConnection: close\r\n$headers\r\n".toByteArray(Charsets.US_ASCII))
        client.getOutputStream().flush()
    }

    private fun activeIpv4Address(): Inet4Address? {
        val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            .filter { it.isUp && !it.isLoopback }
        val preferred = interfaces.sortedByDescending { it.name.contains("wlan", true) || it.name.contains("wifi", true) }
        return preferred.asSequence()
            .flatMap { it.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress && !it.isLinkLocalAddress }
    }

    @Synchronized override fun close() {
        closed = true
        runCatching { socket?.close() }
        socket = null
        pool.shutdownNow()
        address = null
    }
}
