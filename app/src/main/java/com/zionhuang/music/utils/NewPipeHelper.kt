package com.zionhuang.music.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException

object NewPipeDownloader : Downloader() {
    private val client = OkHttpClient.Builder().build()

    @Throws(IOException::class)
    override fun execute(request: Request): Response {
        val httpUrl = request.url()
        val builder = okhttp3.Request.Builder().url(httpUrl)
        request.headers().forEach { (key, values) ->
            values.forEach { value ->
                builder.addHeader(key, value)
            }
        }
        val data = request.dataToSend()
        if (data != null) {
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = data.toRequestBody(mediaType)
            builder.post(body)
        }
        val response = client.newCall(builder.build()).execute()
        val responseBody = response.body?.string() ?: ""
        val responseHeaders = response.headers.toMultimap()
        return Response(response.code, response.message, responseHeaders, responseBody, request.url())
    }
}

object NewPipeHelper {
    private var isInitialized = false

    fun init() {
        if (!isInitialized) {
            try {
                NewPipe.init(NewPipeDownloader)
                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAudioStreamUrl(videoId: String): String? {
        init()
        return try {
            val extractor = ServiceList.YouTube.getStreamExtractor("https://www.youtube.com/watch?v=$videoId")
            extractor.fetchPage()
            val audioStream = extractor.audioStreams
                ?.filter { !it.url.isNullOrEmpty() }
                ?.maxByOrNull { it.averageBitrate }
            audioStream?.url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
