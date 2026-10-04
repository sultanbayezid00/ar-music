package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerState
import com.example.data.model.RepeatMode
import com.example.data.model.Track
import com.example.player.PlayerManager
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.YouTubeEmbeddedPlayer
import com.example.ui.theme.ARBackground
import com.example.ui.theme.ARPrimary
import com.example.ui.theme.ARSecondary
import com.example.ui.theme.ARSurface
import com.example.ui.theme.ARSurfaceCard
import com.example.ui.theme.ARTertiary
import com.example.ui.theme.ARWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerState: PlayerState,
    playerManager: PlayerManager,
    onDismiss: () -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onDismiss()
    }

    val context = LocalContext.current
    val track = playerState.currentTrack ?: return
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ARBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header: Collapse Button, AR Branding, Sleep Timer & Queue Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("now_playing_collapse")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM",
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Official YouTube Player",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ARPrimary
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sleep Timer button
                    IconButton(
                        onClick = { showSleepTimerDialog = true },
                        modifier = Modifier.testTag("sleep_timer_btn")
                    ) {
                        Icon(
                            imageVector = if (playerState.sleepTimerSecondsRemaining != null) Icons.Default.Bedtime else Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (playerState.sleepTimerSecondsRemaining != null) ARWarning else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Queue button
                    IconButton(
                        onClick = { showQueueSheet = true },
                        modifier = Modifier.testTag("now_playing_queue")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Playing Queue",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Display: Official YouTube IFrame Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                YouTubeEmbeddedPlayer(
                    playerManager = playerManager,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Sleep Timer countdown badge if active
            if (playerState.sleepTimerSecondsRemaining != null) {
                val rem = playerState.sleepTimerSecondsRemaining
                val m = rem / 60
                val s = rem % 60
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ARWarning.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = ARWarning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sleep Timer: %02d:%02d".format(m, s),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ARWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title, Artist, and Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onFavoriteToggle(track) },
                    modifier = Modifier.testTag("now_playing_fav")
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) ARTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seek Bar
            var isUserSeeking by remember { mutableStateOf(false) }
            var sliderPosition by remember { mutableFloatStateOf(0f) }

            val currentSec = (playerState.currentPositionMs / 1000).toInt()
            val totalSec = (playerState.durationMs / 1000).toInt().coerceAtLeast(1)
            val displaySec = if (isUserSeeking) sliderPosition.toInt() else currentSec

            Slider(
                value = if (isUserSeeking) sliderPosition else currentSec.toFloat().coerceIn(0f, totalSec.toFloat()),
                onValueChange = {
                    isUserSeeking = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    playerManager.seekTo((sliderPosition * 1000).toLong())
                    isUserSeeking = false
                },
                valueRange = 0f..totalSec.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = ARPrimary,
                    activeTrackColor = ARPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("now_playing_seek_bar")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "%d:%02d".format(displaySec / 60, displaySec % 60),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%d:%02d".format(totalSec / 60, totalSec % 60),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Playback Controls: Shuffle, Previous, Play/Pause, Next, Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = { playerManager.toggleShuffle() },
                    modifier = Modifier.testTag("now_playing_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.isShuffle) ARPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = { playerManager.previous() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_prev")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause FAB
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ARPrimary, ARSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { playerManager.togglePlayPause() },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("now_playing_play_pause")
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = { playerManager.next() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Mode
                IconButton(
                    onClick = { playerManager.toggleRepeat() },
                    modifier = Modifier.testTag("now_playing_repeat")
                ) {
                    val repeatIcon = if (playerState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    val repeatTint = when (playerState.repeatMode) {
                        RepeatMode.OFF -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        RepeatMode.ALL -> ARPrimary
                        RepeatMode.ONE -> ARTertiary
                    }
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = repeatTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Volume Control Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeDown,
                    contentDescription = "Volume Down",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = playerState.volume,
                    onValueChange = { playerManager.setVolume(it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = ARSecondary,
                        activeTrackColor = ARSecondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("now_playing_volume")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume Up",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Up Next Queue Bottom Sheet
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = ARSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Playing Queue (${playerState.queue.size} tracks)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                    itemsIndexed(playerState.queue) { idx, qTrack ->
                        val isCurr = idx == playerState.currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurr) ARSurfaceCard else Color.Transparent)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurr) ARPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = qTrack.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurr) ARPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = qTrack.artist,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    playerManager.playTrack(qTrack, playerState.queue)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isCurr && playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = if (isCurr) ARPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            remainingSeconds = playerState.sleepTimerSecondsRemaining,
            onSetTimer = { playerManager.startSleepTimer(it) },
            onCancelTimer = { playerManager.cancelSleepTimer() },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}
