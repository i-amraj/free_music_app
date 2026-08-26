package com.zionhuang.music.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.LyricsLanguage
import com.zionhuang.music.constants.LyricsLanguageKey
import com.zionhuang.music.constants.PlayerTextAlignmentKey
import com.zionhuang.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.zionhuang.music.lyrics.LyricsEntry
import com.zionhuang.music.lyrics.LyricsEntry.Companion.HEAD_LYRICS_ENTRY
import com.zionhuang.music.lyrics.LyricsUtils.findCurrentLineIndex
import com.zionhuang.music.lyrics.LyricsUtils.parseLyrics
import com.zionhuang.music.ui.component.shimmer.ShimmerHost
import com.zionhuang.music.ui.component.shimmer.TextPlaceholder
import com.zionhuang.music.ui.menu.LyricsMenu
import com.zionhuang.music.ui.screens.settings.PlayerTextAlignment
import com.zionhuang.music.ui.utils.fadingEdge
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val density = LocalDensity.current

    val playerTextAlignment by rememberEnumPreference(PlayerTextAlignmentKey, PlayerTextAlignment.CENTER)
    var lyricsLanguage by rememberEnumPreference(LyricsLanguageKey, LyricsLanguage.ORIGINAL)
    var showLanguageMenu by rememberSaveable { mutableStateOf(false) }

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val translating by playerConnection.translating.collectAsState()
    val lyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val lyrics = remember(lyricsEntity, translating) {
        if (translating) null
        else lyricsEntity?.lyrics
    }

    val songDuration = remember(mediaMetadata) {
        val durationInSec = mediaMetadata?.duration ?: -1
        if (durationInSec > 0) durationInSec * 1000L else 180_000L
    }

    val lines = remember(lyrics, songDuration) {
        if (lyrics == null || lyrics == LYRICS_NOT_FOUND) emptyList()
        else if (lyrics.contains("[")) {
            val parsed = parseLyrics(lyrics)
            if (parsed.isNotEmpty()) listOf(HEAD_LYRICS_ENTRY) + parsed
            else createProportionalLines(lyrics, songDuration)
        } else {
            createProportionalLines(lyrics, songDuration)
        }
    }
    val isSynced = remember(lines) {
        lines.isNotEmpty()
    }

    var currentLineIndex by remember {
        mutableIntStateOf(-1)
    }
    // Because LaunchedEffect has delay, which leads to inconsistent with current line color and scroll animation,
    // we use deferredCurrentLineIndex when user is scrolling
    var deferredCurrentLineIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var lastPreviewTime by rememberSaveable {
        mutableLongStateOf(0L)
    }
    var isSeeking by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(lyrics, lines) {
        if (lines.isEmpty()) {
            currentLineIndex = -1
            return@LaunchedEffect
        }
        while (isActive) {
            delay(50)
            val sliderPosition = sliderPositionProvider()
            isSeeking = sliderPosition != null
            val currentPos = sliderPosition ?: playerConnection.player.currentPosition
            currentLineIndex = findCurrentLineIndex(lines, currentPos)
        }
    }

    LaunchedEffect(isSeeking, lastPreviewTime) {
        if (isSeeking) {
            lastPreviewTime = 0L
        } else if (lastPreviewTime != 0L) {
            delay(LyricsPreviewTime)
            lastPreviewTime = 0L
        }
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentLineIndex, lastPreviewTime) {
        if (!isSynced || lines.isEmpty()) return@LaunchedEffect
        if (currentLineIndex in lines.indices) {
            deferredCurrentLineIndex = currentLineIndex
            if (lastPreviewTime == 0L) {
                if (isSeeking) {
                    lazyListState.scrollToItem(currentLineIndex, with(density) { 36.dp.toPx().toInt() })
                } else {
                    lazyListState.animateScrollToItem(currentLineIndex, with(density) { 36.dp.toPx().toInt() })
                }
            }
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 12.dp)
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.Top)
                .add(WindowInsets(top = maxHeight / 2, bottom = maxHeight / 2))
                .asPaddingValues(),
            modifier = Modifier
                .fadingEdge(vertical = 64.dp)
                .nestedScroll(remember {
                    object : NestedScrollConnection {
                        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                            lastPreviewTime = System.currentTimeMillis()
                            return super.onPostScroll(consumed, available, source)
                        }

                        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                            lastPreviewTime = System.currentTimeMillis()
                            return super.onPostFling(consumed, available)
                        }
                    }
                })
        ) {
            val displayedCurrentLineIndex = if (isSeeking) deferredCurrentLineIndex else currentLineIndex

            if (lyrics == null || translating) {
                item {
                    ShimmerHost {
                        repeat(10) {
                            Box(
                                contentAlignment = when (playerTextAlignment) {
                                    PlayerTextAlignment.SIDED -> Alignment.CenterStart
                                    PlayerTextAlignment.CENTER -> Alignment.Center
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                TextPlaceholder()
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = lines
                ) { index, item ->
                    Text(
                        text = item.text,
                        fontSize = 20.sp,
                        color = if (index == displayedCurrentLineIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        textAlign = when (playerTextAlignment) {
                            PlayerTextAlignment.SIDED -> TextAlign.Start
                            PlayerTextAlignment.CENTER -> TextAlign.Center
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isSynced) {
                                playerConnection.player.seekTo(item.time)
                                lastPreviewTime = 0L
                            }
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .alpha(if (!isSynced || index == displayedCurrentLineIndex) 1f else 0.5f)
                    )
                }
            }
        }

        if (lyrics == LYRICS_NOT_FOUND) {
            Text(
                text = stringResource(R.string.lyrics_not_found),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = when (playerTextAlignment) {
                    PlayerTextAlignment.SIDED -> TextAlign.Start
                    PlayerTextAlignment.CENTER -> TextAlign.Center
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .alpha(0.5f)
            )
        }

        mediaMetadata?.let { mediaMetadata ->
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp)
            ) {
                Box {
                    IconButton(
                        onClick = { showLanguageMenu = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.translate),
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(
                                alpha = if (lyricsLanguage != LyricsLanguage.ORIGINAL) 1f else 0.3f
                            )
                        )
                    }
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        LyricsLanguage.entries.forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = language.displayName,
                                        fontWeight = if (language == lyricsLanguage) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    lyricsLanguage = language
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        menuState.show {
                            LyricsMenu(
                                lyricsProvider = { lyricsEntity },
                                mediaMetadataProvider = { mediaMetadata },
                                onDismiss = menuState::dismiss
                            )
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_horiz),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

const val animateScrollDuration = 300L
val LyricsPreviewTime = 4.seconds

private fun createProportionalLines(lyrics: String, durationMs: Long): List<LyricsEntry> {
    val rawLines = lyrics.lines().filter { it.isNotBlank() }
    if (rawLines.isEmpty()) return emptyList()
    val step = durationMs.toFloat() / rawLines.size
    return listOf(HEAD_LYRICS_ENTRY) + rawLines.mapIndexed { index, text ->
        LyricsEntry((index * step).toLong(), text)
    }
}
