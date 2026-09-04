package com.zionhuang.music.playback

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.common.PlaybackException
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.constants.AudioQuality
import com.zionhuang.music.constants.AudioQualityKey
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.FormatEntity
import com.zionhuang.music.di.DownloadCache
import com.zionhuang.music.di.PlayerCache
import com.zionhuang.music.utils.enumPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadUtil @Inject constructor(
    @ApplicationContext context: Context,
    val database: MusicDatabase,
    val databaseProvider: DatabaseProvider,
    @DownloadCache val downloadCache: SimpleCache,
    @PlayerCache val playerCache: SimpleCache,
) {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    private val audioQuality by enumPreference(context, AudioQualityKey, AudioQuality.AUTO)
    private val songUrlCache = HashMap<String, Pair<String, Long>>()
    private val dataSourceFactory = ResolvingDataSource.Factory(
        CacheDataSource.Factory()
            .setCache(playerCache)
            .setUpstreamDataSourceFactory(
                OkHttpDataSource.Factory(
                    OkHttpClient.Builder()
                        .proxy(YouTube.proxy)
                        .build()
                ).apply {
                    setUserAgent("com.google.ios.youtube/20.01.1 (iPhone16,2; U; CPU iOS 18_2_1 like Mac OS X;)")
                }
            )
    ) { dataSpec ->
        val mediaId = dataSpec.key ?: error("No media id")
        val length = if (dataSpec.length >= 0) dataSpec.length else 1

        if (playerCache.isCached(mediaId, dataSpec.position, length)) {
            return@Factory dataSpec
        }

        songUrlCache[mediaId]?.takeIf { it.second > System.currentTimeMillis() }?.let {
            return@Factory dataSpec.withUri(it.first.toUri())
        }

        val playedFormat = runBlocking(Dispatchers.IO) { database.format(mediaId).first() }
        val playerResponse = runBlocking(Dispatchers.IO) {
            YouTube.player(mediaId)
        }.getOrNull()

        val format = if (playerResponse?.playabilityStatus?.status == "OK") {
            if (playedFormat != null) {
                playerResponse.streamingData?.adaptiveFormats?.find { it.itag == playedFormat.itag }
            } else {
                playerResponse.streamingData?.adaptiveFormats
                    ?.filter { it.isAudio }
                    ?.maxByOrNull {
                        it.bitrate * when (audioQuality) {
                            AudioQuality.AUTO -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                            AudioQuality.HIGH -> 1
                            AudioQuality.LOW -> -1
                        } + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0) // prefer opus stream
                    }
            }
        } else null

        // Priority 1: Use NewPipe helper for un-throttled audio stream URL
        val newPipeUrl = runBlocking(Dispatchers.IO) {
            try {
                com.zionhuang.music.utils.NewPipeHelper.getAudioStreamUrl(mediaId)
            } catch (e: Exception) {
                android.util.Log.w("DownloadUtil", "NewPipe extraction failed for $mediaId: ${e.message}")
                null
            }
        }

        val rawUrl = newPipeUrl ?: format?.url
            ?: throw PlaybackException("No audio stream URL available for download", null, PlaybackException.ERROR_CODE_REMOTE_ERROR)

        val streamUrl = if (!rawUrl.contains("&range=") && format?.contentLength != null) {
            "$rawUrl&range=0-${format.contentLength}"
        } else {
            rawUrl
        }

        if (format != null) {
            val codecsStr = if (format.mimeType.contains("codecs=")) {
                format.mimeType.split("codecs=").getOrNull(1)?.removeSurrounding("\"") ?: ""
            } else ""
            database.query {
                upsert(
                    FormatEntity(
                        id = mediaId,
                        itag = format.itag,
                        mimeType = format.mimeType.split(";")[0],
                        codecs = codecsStr,
                        bitrate = format.bitrate,
                        sampleRate = format.audioSampleRate,
                        contentLength = format.contentLength ?: 5000000L,
                        loudnessDb = playerResponse?.playerConfig?.audioConfig?.loudnessDb
                    )
                )
            }
        }

        val expiresInMs = (playerResponse?.streamingData?.expiresInSeconds?.toLong() ?: 21600L) * 1000L
        songUrlCache[mediaId] = streamUrl to (System.currentTimeMillis() + expiresInMs)
        dataSpec.withUri(streamUrl.toUri())
    }
    val downloadNotificationHelper = DownloadNotificationHelper(context, ExoDownloadService.CHANNEL_ID)
    val downloadManager: DownloadManager = DownloadManager(context, databaseProvider, downloadCache, dataSourceFactory, Executor(Runnable::run)).apply {
        maxParallelDownloads = 3
        addListener(
            ExoDownloadService.TerminalStateNotificationHelper(
                context = context,
                notificationHelper = downloadNotificationHelper,
                nextNotificationId = ExoDownloadService.NOTIFICATION_ID + 1
            )
        )
    }
    val downloads = MutableStateFlow<Map<String, Download>>(emptyMap())

    fun getDownload(songId: String?): Flow<Download?> = downloads.map { it[songId] }

    init {
        val result = mutableMapOf<String, Download>()
        val cursor = downloadManager.downloadIndex.getDownloads()
        while (cursor.moveToNext()) {
            result[cursor.download.request.id] = cursor.download
        }
        downloads.value = result
        downloadManager.addListener(
            object : DownloadManager.Listener {
                override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                    downloads.update { map ->
                        map.toMutableMap().apply {
                            set(download.request.id, download)
                        }
                    }
                }
            }
        )
    }
}