package com.sportcasterpro.app.feature.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sportcasterpro.app.BuildConfig
import com.sportcasterpro.app.R
import com.sportcasterpro.app.core.ui.components.SportCasterTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val authUser by viewModel.authUser.collectAsState()

    LaunchedEffect(authUser) {
        if (authUser == null) onSignedOut()
    }

    Scaffold(topBar = { SportCasterTopBar(stringResource(R.string.settings_title), onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(text = authUser?.email.orEmpty(), style = MaterialTheme.typography.titleMedium)

            Text(
                text = stringResource(R.string.settings_subscription),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = if (BuildConfig.IS_PRO) "Pro" else "Free",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = viewModel::onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            ) {
                Text(stringResource(R.string.action_sign_out))
            }
        }
    }
}
