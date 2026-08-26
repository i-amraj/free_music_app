package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.R
import com.zionhuang.music.ui.component.NavigationTitle

enum class LibraryTab {
    PLAYLISTS, SONGS, ALBUMS, ARTISTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(LibraryTab.PLAYLISTS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    .asPaddingValues()
            )
    ) {
        NavigationTitle(
            title = stringResource(R.string.library),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Filter chips row matching YT Music design
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            items(LibraryTab.values()) { tab ->
                val label = when (tab) {
                    LibraryTab.PLAYLISTS -> stringResource(R.string.playlists)
                    LibraryTab.SONGS -> stringResource(R.string.songs)
                    LibraryTab.ALBUMS -> stringResource(R.string.albums)
                    LibraryTab.ARTISTS -> stringResource(R.string.artists)
                }
                val isSelected = tab == selectedTab

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                LibraryTab.PLAYLISTS -> LibraryPlaylistsScreen(navController)
                LibraryTab.SONGS -> LibrarySongsScreen(navController)
                LibraryTab.ALBUMS -> LibraryAlbumsScreen(navController)
                LibraryTab.ARTISTS -> LibraryArtistsScreen(navController)
            }
        }
    }
}
