package org.muslim.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.feature.settings.R

// Developer contact details (shown in-app and in the README).
private const val GITHUB_URL = "https://github.com/Alaa91H"
private const val EMAIL = "alahus2591@gmail.com"
private const val TELEGRAM_URL = "https://t.me/Alaa91h"
private const val SUPPORT_URL = "https://ko-fi.com/alaa91h"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    versionName: String,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            AboutCard(title = stringResource(R.string.about_license_title)) {
                Text(stringResource(R.string.about_license_body))
            }

            Spacer(Modifier.height(12.dp))

            AboutCard(title = stringResource(R.string.about_principles_title)) {
                Text(stringResource(R.string.about_principles_body))
            }

            Spacer(Modifier.height(12.dp))

            AboutCard(title = stringResource(R.string.about_content_review_title)) {
                Text(stringResource(R.string.about_content_review_body))
            }

            Spacer(Modifier.height(12.dp))

            AboutCard(title = stringResource(R.string.about_contact_title)) {
                ContactRow(
                    icon = Icons.Filled.Code,
                    label = stringResource(R.string.about_contact_github),
                    value = "github.com/Alaa91H",
                    uri = GITHUB_URL,
                )
                ContactRow(
                    icon = Icons.Filled.Email,
                    label = stringResource(R.string.about_contact_email),
                    value = EMAIL,
                    uri = "mailto:$EMAIL",
                )
                ContactRow(
                    icon = Icons.AutoMirrored.Filled.Send,
                    label = stringResource(R.string.about_contact_telegram),
                    value = "t.me/Alaa91h",
                    uri = TELEGRAM_URL,
                )
                ContactRow(
                    icon = Icons.Filled.Favorite,
                    label = stringResource(R.string.about_contact_support),
                    value = "ko-fi.com/alaa91h",
                    uri = SUPPORT_URL,
                )
            }
        }
    }
}

@Composable
private fun AboutCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String, uri: String) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { runCatching { uriHandler.openUri(uri) } }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
