package com.zionhuang.music.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.zionhuang.music.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int,
    val route: String,
) {
    object Home : Screens(R.string.home, R.drawable.home, "home")
    object Explore : Screens(R.string.explore, R.drawable.explore, "explore")
    object Downloaded : Screens(R.string.downloaded, R.drawable.download, "downloaded")
    object Songs : Screens(R.string.offline, R.drawable.music_note, "songs")
    object Library : Screens(R.string.library, R.drawable.library_music, "library")

    object Artists : Screens(R.string.artists, R.drawable.artist, "artists")
    object Albums : Screens(R.string.albums, R.drawable.album, "albums")
    object Playlists : Screens(R.string.playlists, R.drawable.queue_music, "playlists")

    companion object {
        val MainScreens = listOf(Home, Explore, Downloaded, Library)
    }
}
