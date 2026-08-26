package com.zionhuang.music.lyrics

import android.content.Context
import android.util.LruCache
import com.zionhuang.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.utils.reportException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class LyricsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val lyricsProviders = listOf(LrcLibLyricsProvider, KuGouLyricsProvider, YouTubeSubtitleLyricsProvider, YouTubeLyricsProvider)
    private val cache = LruCache<String, List<LyricsResult>>(MAX_CACHE_SIZE)

    suspend fun getLyrics(mediaMetadata: MediaMetadata): String {
        val cached = cache.get(mediaMetadata.id)?.firstOrNull()
        if (cached != null) {
            return cached.lyrics
        }
        return coroutineScope {
            val enabledProviders = lyricsProviders.filter { it.isEnabled(context) }
            val deferred = CompletableDeferred<String>()
            val jobs = enabledProviders.map { provider ->
                launch {
                    val result = provider.getLyrics(
                        mediaMetadata.id,
                        mediaMetadata.title,
                        mediaMetadata.artists.joinToString { it.name },
                        mediaMetadata.duration
                    ).getOrNull()
                    if (!result.isNullOrBlank() && result != LYRICS_NOT_FOUND) {
                        deferred.complete(result)
                    }
                }
            }
            launch {
                jobs.forEach { it.join() }
                if (!deferred.isCompleted) {
                    deferred.complete(LYRICS_NOT_FOUND)
                }
            }
            val lyrics = deferred.await()
            jobs.forEach { it.cancel() }
            lyrics
        }
    }

    suspend fun getAllLyrics(
        mediaId: String,
        songTitle: String,
        songArtists: String,
        duration: Int,
        callback: (LyricsResult) -> Unit,
    ) {
        val cacheKey = "$songArtists-$songTitle".replace(" ", "")
        cache.get(cacheKey)?.let { results ->
            results.forEach {
                callback(it)
            }
            return
        }
        val allResult = mutableListOf<LyricsResult>()
        lyricsProviders.forEach { provider ->
            if (provider.isEnabled(context)) {
                provider.getAllLyrics(mediaId, songTitle, songArtists, duration) { lyrics ->
                    val result = LyricsResult(provider.name, lyrics)
                    allResult += result
                    callback(result)
                }
            }
        }
        cache.put(cacheKey, allResult)
    }

    companion object {
        private const val MAX_CACHE_SIZE = 3
    }
}

data class LyricsResult(
    val providerName: String,
    val lyrics: String,
)
