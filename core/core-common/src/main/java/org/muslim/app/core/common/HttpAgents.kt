package org.muslim.app.core.common

/**
 * HTTP user agent identifying this app to OpenStreetMap-family services
 * (the Overpass API and OpenFreeMap tiles). Those services block requests
 * with a missing or generic user agent (HTTP 403/406), so every request must
 * carry this identifying string.
 */
object HttpAgents {
    const val APP_USER_AGENT =
        "Muslim/Android (https://github.com/Alaa91H/Muslim; alahus2591@gmail.com)"
}
