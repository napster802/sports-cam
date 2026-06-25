package com.sportcasterpro.app.feature.livestream.presentation

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.pedro.library.view.OpenGlView
import com.sportcasterpro.app.R
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.ui.components.FullScreenLoading
import com.sportcasterpro.app.feature.scoreboard.domain.ScoringTeam
import com.sportcasterpro.app.feature.scoreboard.presentation.ScoreboardOverlay
import com.sportcasterpro.app.feature.streaming.domain.ConnectionStatus
import com.sportcasterpro.app.feature.streaming.domain.StreamSettings

private val STREAMING_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

// Controls in this screen sit directly on top of the live camera feed rather than a themed
// surface, so they need a color that stays legible regardless of light/dark theme instead of
// theme defaults like onSurface, which are tuned for a white or navy backdrop.
private val OverlayContentColor = Color.White

@Composable
fun LiveStreamScreen(
    matchId: String,
    onBack: () -> Unit,
    viewModel: LiveStreamViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var permissionsGranted by remember {
        mutableStateOf(
            STREAMING_PERMISSIONS.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED },
        )
    }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        permissionsGranted = grants.values.all { it }
    }
    LaunchedEffect(Unit) {
        if (!permissionsGranted) permissionLauncher.launch(STREAMING_PERMISSIONS)
    }

    // Live streaming benefits from landscape framing; unlock rotation here and restore the
    // app-wide portrait lock (see AndroidManifest.xml) once this screen is left.
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        onDispose {
            activity?.requestedOrientation = previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    Scaffold { padding ->
        if (uiState.isLoading || !permissionsGranted) {
            FullScreenLoading(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { ctx -> OpenGlView(ctx).also { viewModel.onPreviewReady(it) } },
                modifier = Modifier.fillMaxSize(),
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OverlayContentColor)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StreamStatusBadge(status = uiState.streamStats.connectionStatus)
                        IconButton(onClick = { isSettingsDialogOpen = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.stream_settings_title), tint = OverlayContentColor)
                        }
                    }
                }

                uiState.scoreboard?.let { scoreboard ->
                    ScoreboardOverlay(
                        state = scoreboard,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                            .fillMaxWidth(0.92f),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                LiveStreamControls(
                    viewModel = viewModel,
                    sport = uiState.scoreboard?.sport,
                    isTimerRunning = uiState.scoreboard?.isTimerRunning == true,
                    isRecording = uiState.streamStats.isRecording,
                    isStreaming = uiState.streamStats.connectionStatus == ConnectionStatus.STREAMING ||
                        uiState.streamStats.connectionStatus == ConnectionStatus.CONNECTING,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                )
            }
        }

        if (isSettingsDialogOpen) {
            StreamSettingsDialog(
                initialSettings = uiState.streamSettings,
                onDismiss = { isSettingsDialogOpen = false },
                onSave = { settings ->
                    viewModel.onStreamSettingsChanged(settings)
                    isSettingsDialogOpen = false
                },
            )
        }
    }
}

private enum class StreamPlatform(val label: String, val defaultRtmpUrl: String?) {
    FACEBOOK("Facebook", "rtmps://live-api-s.facebook.com:443/rtmp/"),
    CUSTOM("Custom RTMP", null),
}

private fun platformFor(rtmpUrl: String): StreamPlatform =
    StreamPlatform.entries.firstOrNull { it.defaultRtmpUrl == rtmpUrl } ?: StreamPlatform.CUSTOM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreamSettingsDialog(
    initialSettings: StreamSettings,
    onDismiss: () -> Unit,
    onSave: (StreamSettings) -> Unit,
) {
    var platform by remember { mutableStateOf(platformFor(initialSettings.rtmpUrl)) }
    var platformMenuExpanded by remember { mutableStateOf(false) }
    var rtmpUrl by remember { mutableStateOf(initialSettings.rtmpUrl) }
    var streamKey by remember { mutableStateOf(initialSettings.streamKey) }
    var resolution by remember { mutableStateOf(initialSettings.videoWidth to initialSettings.videoHeight) }
    var fps by remember { mutableStateOf(initialSettings.fps) }
    var bitrateBps by remember { mutableStateOf(initialSettings.videoBitrateBps) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.stream_settings_title)) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = platformMenuExpanded,
                    onExpandedChange = { platformMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = platform.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_platform)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = platformMenuExpanded,
                        onDismissRequest = { platformMenuExpanded = false },
                        modifier = Modifier.exposedDropdownSize(),
                    ) {
                        StreamPlatform.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    platform = option
                                    option.defaultRtmpUrl?.let { rtmpUrl = it }
                                    platformMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                if (platform == StreamPlatform.CUSTOM) {
                    OutlinedTextField(
                        value = rtmpUrl,
                        onValueChange = { rtmpUrl = it },
                        label = { Text(stringResource(R.string.label_rtmp_url)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
                OutlinedTextField(
                    value = streamKey,
                    onValueChange = { streamKey = it },
                    label = { Text(stringResource(R.string.label_stream_key)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )

                Text(
                    text = stringResource(R.string.label_resolution),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    StreamSettings.RESOLUTION_PRESETS.forEach { preset ->
                        PresetChip(
                            label = "${preset.first}x${preset.second}",
                            selected = resolution == preset,
                            onClick = { resolution = preset },
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.label_frame_rate),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    StreamSettings.FPS_PRESETS.forEach { preset ->
                        PresetChip(label = "$preset fps", selected = fps == preset, onClick = { fps = preset })
                    }
                }

                Text(
                    text = stringResource(R.string.label_bitrate),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    StreamSettings.BITRATE_PRESETS_BPS.forEach { preset ->
                        PresetChip(label = "${preset / 1_000_000}.${(preset / 100_000) % 10} Mbps", selected = bitrateBps == preset, onClick = { bitrateBps = preset })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        StreamSettings(
                            rtmpUrl = rtmpUrl.trim(),
                            streamKey = streamKey.trim(),
                            videoWidth = resolution.first,
                            videoHeight = resolution.second,
                            fps = fps,
                            videoBitrateBps = bitrateBps,
                            audioBitrateBps = initialSettings.audioBitrateBps,
                        ),
                    )
                },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun PresetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.padding(0.dp),
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun StreamStatusBadge(status: ConnectionStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        ConnectionStatus.STREAMING -> "LIVE" to MaterialTheme.colorScheme.error
        ConnectionStatus.CONNECTING -> "CONNECTING" to MaterialTheme.colorScheme.primary
        ConnectionStatus.FAILED -> "FAILED" to MaterialTheme.colorScheme.error
        ConnectionStatus.DISCONNECTED -> "DISCONNECTED" to MaterialTheme.colorScheme.error
        ConnectionStatus.IDLE -> "OFFLINE" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(modifier = modifier, color = color.copy(alpha = 0.85f), shape = MaterialTheme.shapes.small) {
        Text(text = label, color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun LiveStreamControls(
    viewModel: LiveStreamViewModel,
    sport: Sport?,
    isTimerRunning: Boolean,
    isRecording: Boolean,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ScoreButtons(team = ScoringTeam.HOME, sport = sport, viewModel = viewModel)
            ScoreButtons(team = ScoringTeam.AWAY, sport = sport, viewModel = viewModel)
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = viewModel::nextPeriod,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OverlayContentColor),
            ) { Text(stringResource(R.string.action_next_period)) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { if (isTimerRunning) viewModel.pauseTimer() else viewModel.startTimer() }) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isTimerRunning) {
                            stringResource(R.string.action_pause_timer)
                        } else {
                            stringResource(R.string.action_play_timer)
                        },
                        tint = OverlayContentColor,
                    )
                }
                OutlinedButton(
                    onClick = viewModel::swapTeams,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OverlayContentColor),
                ) { Text(stringResource(R.string.action_swap_teams)) }
            }

            OutlinedButton(
                onClick = viewModel::toggleRecording,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OverlayContentColor),
            ) {
                Text(if (isRecording) stringResource(R.string.action_pause) else stringResource(R.string.action_record))
            }
        }
        Button(
            onClick = { if (isStreaming) viewModel.endBroadcast() else viewModel.startBroadcast() },
            colors = if (isStreaming) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors(),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(if (isStreaming) stringResource(R.string.action_stop_stream) else stringResource(R.string.action_start_stream))
        }
    }
}

@Composable
private fun ScoreButtons(team: ScoringTeam, sport: Sport?, viewModel: LiveStreamViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (sport == Sport.BASKETBALL) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1, 2, 3).forEach { points ->
                    Button(
                        onClick = { viewModel.addPoint(team, points) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text("+$points")
                    }
                }
            }
        } else {
            Button(onClick = { viewModel.addPoint(team) }) { Text(stringResource(R.string.action_add_point)) }
        }
        OutlinedButton(onClick = { viewModel.removePoint(team) }, modifier = Modifier.padding(top = 4.dp)) {
            Text(stringResource(R.string.action_remove_point))
        }
    }
}
