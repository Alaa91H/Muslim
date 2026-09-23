package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.feature.learn.R

internal object WillDraftProtectionTestTags {
    const val PROTECTION_CARD = "will_protection_card"
    const val ENABLE = "will_protection_enable"
    const val LOCK_NOW = "will_protection_lock_now"
    const val DISABLE = "will_protection_disable"
    const val LOCKED_CONTENT = "will_locked_content"
    const val UNLOCK = "will_unlock"
}

@Composable
fun WillDraftProtectionCard(
    session: WillDraftProtectionSession,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(WillDraftProtectionTestTags.PROTECTION_CARD),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProtectionCardHeader()
            Text(
                text = stringResource(R.string.funeral_will_protection_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            ProtectionCardActions(session)
            session.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ProtectionCardHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.funeral_will_protection_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ProtectionCardActions(
    session: WillDraftProtectionSession,
) {
    if (session.enabled) {
        Text(
            text = stringResource(R.string.funeral_will_protection_enabled),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Button(
            onClick = session.actions.lockNow,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(WillDraftProtectionTestTags.LOCK_NOW),
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.funeral_will_protection_lock_now))
        }
        OutlinedButton(
            onClick = session.actions.disable,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(WillDraftProtectionTestTags.DISABLE),
        ) {
            Text(stringResource(R.string.funeral_will_protection_disable))
        }
    } else {
        Button(
            onClick = session.actions.enable,
            enabled = session.availability == WillDraftAuthenticationAvailability.Available,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(WillDraftProtectionTestTags.ENABLE),
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.funeral_will_protection_enable))
        }
        ProtectionAvailabilityMessage(session)
    }
}

@Composable
fun WillDraftLockedContent(
    session: WillDraftProtectionSession,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(WillDraftProtectionTestTags.LOCKED_CONTENT)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.funeral_will_locked_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = stringResource(R.string.funeral_will_locked_text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = session.actions.unlock,
            enabled = session.availability == WillDraftAuthenticationAvailability.Available,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(WillDraftProtectionTestTags.UNLOCK)
                .padding(top = 18.dp),
        ) {
            Icon(Icons.Filled.LockOpen, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.funeral_will_unlock))
        }
        ProtectionAvailabilityMessage(session)
        session.errorMessage?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
fun WillDraftProtectionLoading(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.funeral_will_protection_loading),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ProtectionAvailabilityMessage(
    session: WillDraftProtectionSession,
) {
    val message = when (session.availability) {
        WillDraftAuthenticationAvailability.Available -> null
        WillDraftAuthenticationAvailability.NotConfigured ->
            stringResource(R.string.funeral_will_auth_not_configured)

        WillDraftAuthenticationAvailability.Unavailable ->
            stringResource(R.string.funeral_will_auth_unavailable)
    }
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
