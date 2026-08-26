package com.zionhuang.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.pages.ExplorePage
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.NavigationTitle
import com.zionhuang.music.ui.component.YouTubeGridItem
import com.zionhuang.music.ui.component.YouTubeListItem
import com.zionhuang.music.ui.component.shimmer.ListItemPlaceHolder
import com.zionhuang.music.ui.menu.YouTubeSongMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val playerConnection = LocalPlayerConnection.current
    val menuState = LocalMenuState.current
    var explorePage by remember { mutableStateOf<ExplorePage?>(null) }
    var top10Songs by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            YouTube.explore().onSuccess { page ->
                explorePage = page
            }
            YouTube.search("Trending Top Songs", YouTube.SearchFilter.FILTER_SONG).onSuccess { result ->
                top10Songs = result.items.filterIsInstance<SongItem>().take(10)
            }
            isLoading = false
        }
    }

    LazyColumn(
        contentPadding = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            .asPaddingValues(),
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Title
        item {
            NavigationTitle(
                title = stringResource(R.string.explore),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 2x2 Action Cards (New Releases, Charts, Moods & Genres, Playlists)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExploreActionCard(
                        title = stringResource(R.string.new_releases),
                        iconId = R.drawable.music_note,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("new_release") }
                    )
                    ExploreActionCard(
                        title = stringResource(R.string.charts),
                        iconId = R.drawable.trending_up,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("mood_and_genres") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExploreActionCard(
                        title = stringResource(R.string.mood_and_genres),
                        iconId = R.drawable.mood,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("mood_and_genres") }
                    )
                    ExploreActionCard(
                        title = stringResource(R.string.playlists),
                        iconId = R.drawable.queue_music,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("playlists") }
                    )
                }
            }
        }

        // Most Trending Top 10 Songs Section
        item {
            NavigationTitle(
                title = "Most Trending Top 10",
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (isLoading && top10Songs.isEmpty()) {
            items(5) {
                ListItemPlaceHolder(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        } else {
            itemsIndexed(
                items = top10Songs,
                key = { _, item -> item.id }
            ) { index, song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank Badge #1 to #10
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (index < 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .width(40.dp)
                            .padding(start = 16.dp)
                    )

                    YouTubeListItem(
                        item = song,
                        isActive = song.id == playerConnection?.service?.currentMediaMetadata?.collectAsState()?.value?.id,
                        isPlaying = playerConnection?.isPlaying?.collectAsState()?.value == true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                playerConnection?.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                            }
                    )
                }
            }
        }

        // New Albums & Singles Section
        explorePage?.newReleaseAlbums?.takeIf { it.isNotEmpty() }?.let { albums ->
            item {
                NavigationTitle(
                    title = stringResource(R.string.new_release_albums),
                    onClick = { navController.navigate("new_release") },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(albums) { album ->
                        YouTubeGridItem(
                            item = album,
                            fillMaxWidth = false,
                            modifier = Modifier
                                .width(140.dp)
                                .clickable {
                                    navController.navigate("album/${album.id}")
                                }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ExploreActionCard(
    title: String,
    iconId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
