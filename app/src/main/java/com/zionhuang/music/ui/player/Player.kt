package com.zionhuang.music.ui.player

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import android.widget.Toast
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.media3.common.C
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.navigation.NavController
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalDownloadUtil
import com.zionhuang.music.LocalPlayerConnection
import androidx.core.net.toUri
import com.zionhuang.music.R
import com.zionhuang.music.constants.DarkModeKey
import com.zionhuang.music.constants.PlayerHorizontalPadding
import com.zionhuang.music.constants.PlayerTextAlignmentKey
import com.zionhuang.music.constants.PureBlackKey
import com.zionhuang.music.constants.QueuePeekHeight
import com.zionhuang.music.constants.SliderStyle
import com.zionhuang.music.constants.SliderStyleKey
import com.zionhuang.music.extensions.togglePlayPause
import com.zionhuang.music.extensions.toggleRepeatMode
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.ui.component.BottomSheet
import com.zionhuang.music.ui.component.BottomSheetState
import com.zionhuang.music.ui.component.ResizableIconButton
import com.zionhuang.music.ui.component.AnimatedGradientBackground
import com.zionhuang.music.ui.component.rememberBottomSheetState
import com.zionhuang.music.ui.screens.settings.DarkMode
import com.zionhuang.music.ui.screens.settings.PlayerTextAlignment
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.menu.PlayerMenu
import com.zionhuang.music.ui.menu.SongMenu
import me.saket.squiggles.SquigglySlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.TIME)
    val pureBlack by rememberPreference(PureBlackKey, defaultValue = false)
    val useDarkTheme = remember(isSystemInDarkTheme, darkTheme) {
        when (darkTheme) {
            DarkMode.ON -> true
            DarkMode.OFF -> false
            DarkMode.AUTO -> isSystemInDarkTheme
            DarkMode.TIME -> isNightByTime()
        }
    }
    val useBlackBackground = remember(useDarkTheme, pureBlack) {
        useDarkTheme && pureBlack
    }
    val backgroundColor = if (useBlackBackground && state.value > state.collapsedBound) {
        lerp(MaterialTheme.colorScheme.surfaceContainer, Color.Black, state.progress)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val sheetBackground = backgroundColor.copy(alpha = if (useDarkTheme) 0.85f else 0.75f)
    val overlayAlpha = if (useDarkTheme) 0.55f else 0.45f

    val playerTextAlignment by rememberEnumPreference(PlayerTextAlignmentKey, PlayerTextAlignment.CENTER)
    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.DEFAULT)

    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    var showDetailsDialog by remember { mutableStateOf(false) }
    if (showDetailsDialog) {
        DetailsDialog(
            onDismiss = { showDetailsDialog = false }
        )
    }

    var position by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.currentPosition)
    }
    var duration by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.duration)
    }
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(100)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        expandedBound = state.expandedBound,
    )

    BottomSheet(
        state = state,
        modifier = modifier,
        backgroundColor = sheetBackground,
        onDismiss = {
            playerConnection.player.stop()
            playerConnection.player.clearMediaItems()
        },
        collapsedContent = {
            MiniPlayer(
                position = position,
                duration = duration,
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedGradientBackground(modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = overlayAlpha))
            )
        }
        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->
            val playPauseRoundness by animateDpAsState(
                targetValue = if (isPlaying) 24.dp else 36.dp,
                animationSpec = tween(durationMillis = 100, easing = LinearEasing),
                label = "playPauseRoundness"
            )

            Text(
                text = mediaMetadata.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = PlayerHorizontalPadding)
                    .basicMarquee()
                    .clickable(enabled = mediaMetadata.album != null) {
                        navController.navigate("album/${mediaMetadata.album!!.id}")
                        state.collapseSoft()
                    }
                    .align(
                        when (playerTextAlignment) {
                            PlayerTextAlignment.SIDED -> Alignment.Start
                            PlayerTextAlignment.CENTER -> Alignment.CenterHorizontally
                        },
                    )
            )

            Spacer(Modifier.height(6.dp))

            Row(
                horizontalArrangement = when (playerTextAlignment) {
                    PlayerTextAlignment.SIDED -> Arrangement.Start
                    PlayerTextAlignment.CENTER -> Arrangement.Center
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                mediaMetadata.artists.fastForEachIndexed { index, artist ->
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        modifier = Modifier
                            .clickable(enabled = artist.id != null) {
                                navController.navigate("artist/${artist.id}")
                                state.collapseSoft()
                            }
                    )

                    if (index != mediaMetadata.artists.lastIndex) {
                        Text(
                            text = ", ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // YouTube Music Action Chips Strip (Like, Save, Share, Download)
            val context = LocalContext.current
            val clipboardManager = LocalClipboardManager.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                val isLiked = currentSong?.song?.liked == true
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { playerConnection.toggleLike() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Save Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { playerConnection.toggleLibrary() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.library_add),
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Share Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable {
                            val shareUrl = "https://iamraj.me/projects/raj-music.html"
                            val intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "Listen to ${mediaMetadata.title} on Raj Music: $shareUrl")
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, null))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.share),
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Download Button
                val database = LocalDatabase.current
                val downloadUtil = LocalDownloadUtil.current
                val downloadMap by downloadUtil.downloads.collectAsState()
                val download = downloadMap[mediaMetadata.id]

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable {
                            if (download?.state == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED) {
                                Toast.makeText(context, "Already downloaded", Toast.LENGTH_SHORT).show()
                            } else if (download?.state == androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING || download?.state == androidx.media3.exoplayer.offline.Download.STATE_QUEUED) {
                                Toast.makeText(context, "Download in progress...", Toast.LENGTH_SHORT).show()
                            } else {
                                database.transaction {
                                    insert(mediaMetadata)
                                }
                                val downloadRequest = androidx.media3.exoplayer.offline.DownloadRequest.Builder(mediaMetadata.id, mediaMetadata.id.toUri())
                                    .setCustomCacheKey(mediaMetadata.id)
                                    .setData(mediaMetadata.title.toByteArray())
                                    .build()
                                androidx.media3.exoplayer.offline.DownloadService.sendAddDownload(
                                    context,
                                    com.zionhuang.music.playback.ExoDownloadService::class.java,
                                    downloadRequest,
                                    false
                                )
                                Toast.makeText(context, "Downloading ${mediaMetadata.title}...", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val iconRes = when (download?.state) {
                        androidx.media3.exoplayer.offline.Download.STATE_COMPLETED -> R.drawable.library_add_check
                        androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING, androidx.media3.exoplayer.offline.Download.STATE_QUEUED -> R.drawable.sync
                        else -> R.drawable.download
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = "Download",
                        tint = if (download?.state == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when (sliderStyle) {
                SliderStyle.DEFAULT -> {
                    Slider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            sliderPosition = it.toLong()
                        },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                playerConnection.player.seekTo(it)
                                position = it
                            }
                            sliderPosition = null
                        },
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
                    )
                }

                SliderStyle.SQUIGGLY -> {
                    SquigglySlider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            sliderPosition = it.toLong()
                        },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                playerConnection.player.seekTo(it)
                                position = it
                            }
                            sliderPosition = null
                        },
                        squigglesSpec = SquigglySlider.SquigglesSpec(
                            amplitude = if (isPlaying) 2.dp else 0.dp,
                            strokeWidth = 4.dp,
                        ),
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding + 4.dp)
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: position),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Playback Action Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                // Shuffle
                val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()
                IconButton(
                    onClick = { playerConnection.player.shuffleModeEnabled = !shuffleModeEnabled }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.shuffle),
                        contentDescription = "Shuffle",
                        tint = if (shuffleModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Skip Previous
                IconButton(
                    enabled = canSkipPrevious,
                    onClick = playerConnection::seekToPrevious
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_previous),
                        contentDescription = "Previous",
                        tint = if (canSkipPrevious) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Main White Circle Play / Pause
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            if (playbackState == STATE_ENDED) {
                                playerConnection.player.seekTo(0, 0)
                                playerConnection.player.playWhenReady = true
                            } else {
                                playerConnection.player.togglePlayPause()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (playbackState == STATE_ENDED) R.drawable.replay else if (isPlaying) R.drawable.pause else R.drawable.play),
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Skip Next
                IconButton(
                    enabled = canSkipNext,
                    onClick = playerConnection::seekToNext
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = "Next",
                        tint = if (canSkipNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat
                IconButton(
                    onClick = playerConnection.player::toggleRepeatMode
                ) {
                    Icon(
                        painter = painterResource(
                            when (repeatMode) {
                                REPEAT_MODE_OFF, REPEAT_MODE_ALL -> R.drawable.repeat
                                REPEAT_MODE_ONE -> R.drawable.repeat_one
                                else -> R.drawable.repeat
                            }
                        ),
                        contentDescription = "Repeat",
                        tint = if (repeatMode != REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        val configuration = LocalConfiguration.current
        val isWideScreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || configuration.screenWidthDp >= 500

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            ) {
                // Left Side: Full Player
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    var isVideoMode by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { state.collapseSoft() }) {
                            Icon(
                                painter = painterResource(R.drawable.expand_more),
                                contentDescription = "Collapse",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        PlayerMenu(
                                            mediaMetadata = mediaMetadata,
                                            navController = navController,
                                            bottomSheetState = state,
                                            onShowSleepTimerDialog = { showSleepTimerDialog = true },
                                            onShowDetailsDialog = { showDetailsDialog = true },
                                            onDismiss = menuState::dismiss
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.tune),
                                    contentDescription = "Controls & Settings",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    val song = currentSong
                                    if (song != null) {
                                        menuState.show {
                                            SongMenu(
                                                originalSong = song,
                                                navController = navController,
                                                onDismiss = menuState::dismiss
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.more_vert),
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Thumbnail(
                            sliderPositionProvider = { sliderPosition },
                            modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection)
                        )
                    }

                    mediaMetadata?.let {
                        controlsContent(it)
                    }
                }

                // Right Side: Queue / Lyrics / Related Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(top = 8.dp, bottom = 8.dp, end = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f))
                ) {
                    QueueContent(navController = navController)
                }
            }
        } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = queueSheetState.collapsedBound)
                ) {
                    // Top Player Bar: Down Arrow + [ Song | Video ] Toggle + Cast & Menu Options
                    var isVideoMode by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { state.collapseSoft() }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.expand_more),
                                contentDescription = "Collapse",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        PlayerMenu(
                                            mediaMetadata = mediaMetadata,
                                            navController = navController,
                                            bottomSheetState = state,
                                            onShowSleepTimerDialog = { showSleepTimerDialog = true },
                                            onShowDetailsDialog = { showDetailsDialog = true },
                                            onDismiss = menuState::dismiss
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.tune),
                                    contentDescription = "Controls & Settings",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    val song = currentSong
                                    if (song != null) {
                                        menuState.show {
                                            SongMenu(
                                                originalSong = song,
                                                navController = navController,
                                                onDismiss = menuState::dismiss
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.more_vert),
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Thumbnail(
                            sliderPositionProvider = { sliderPosition },
                            modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection)
                        )
                    }

                    mediaMetadata?.let {
                        controlsContent(it)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }

        if (!isWideScreen) {
            Queue(
                state = queueSheetState,
                playerBottomSheetState = state,
                backgroundColor = backgroundColor,
                navController = navController
            )
        }
    }
}

private fun isNightByTime(): Boolean {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return hour < 4 || hour >= 17
}
