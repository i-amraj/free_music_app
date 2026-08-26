package com.zionhuang.music.utils

import android.icu.text.Transliterator
import android.util.LruCache
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.zionhuang.music.constants.LyricsLanguage
import com.zionhuang.music.db.entities.LyricsEntity
import com.zionhuang.music.lyrics.LyricsUtils
import kotlinx.coroutines.tasks.await

object TranslationHelper {
    private const val MAX_CACHE_SIZE = 20
    private val cache = LruCache<String, LyricsEntity>(MAX_CACHE_SIZE)

    // Devanagari -> Latin, then strip combining diacritics for cleaner Hinglish. Offline (ICU, API 24+).
    private val toLatin: Transliterator by lazy {
        Transliterator.getInstance("Devanagari-Latin; nfd; [:nonspacing mark:] remove; nfc")
    }

    suspend fun translate(lyrics: LyricsEntity, target: LyricsLanguage): LyricsEntity {
        if (target == LyricsLanguage.ORIGINAL) return lyrics
        val cacheKey = "${lyrics.id}:${target.name}"
        cache[cacheKey]?.let { return it }
        val result = if (target == LyricsLanguage.HINGLISH) {
            transliterate(lyrics)
        } else {
            translateWithMlKit(lyrics, target)
        }
        cache.put(cacheKey, result)
        return result
    }

    private fun transliterate(lyrics: LyricsEntity): LyricsEntity {
        val newLyrics = if (lyrics.lyrics.startsWith("[")) {
            LyricsUtils.parseLyrics(lyrics.lyrics).joinToString(separator = "\n") {
                "[%02d:%02d.%03d]${toLatin.transliterate(it.text)}".format(
                    it.time / 60000, (it.time / 1000) % 60, it.time % 1000
                )
            }
        } else {
            lyrics.lyrics.lines().joinToString(separator = "\n") { toLatin.transliterate(it) }
        }
        return lyrics.copy(lyrics = newLyrics)
    }

    private suspend fun translateWithMlKit(lyrics: LyricsEntity, target: LyricsLanguage): LyricsEntity {
        val targetLanguage = when (target) {
            LyricsLanguage.ENGLISH -> TranslateLanguage.ENGLISH
            LyricsLanguage.HINDI -> TranslateLanguage.HINDI
            LyricsLanguage.URDU -> TranslateLanguage.URDU
            else -> return lyrics
        }
        val isSynced = lyrics.lyrics.startsWith("[")
        val sourceLanguage = TranslateLanguage.fromLanguageTag(
            LanguageIdentification.getClient().identifyLanguage(
                lyrics.lyrics.lines().joinToString(separator = "\n") {
                    it.replace("\\[\\d{2}:\\d{2}.\\d{2,3}\\] *".toRegex(), "")
                }
            ).await()
        )
        if (sourceLanguage == null || sourceLanguage == targetLanguage) return lyrics
        val translator = Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
        )
        translator.downloadModelIfNeeded(
            DownloadConditions.Builder().requireWifi().build()
        ).await()
        val newLyrics = if (isSynced) {
            LyricsUtils.parseLyrics(lyrics.lyrics).map {
                it.copy(text = translator.translate(it.text).await())
            }.joinToString(separator = "\n") {
                "[%02d:%02d.%03d]${it.text}".format(
                    it.time / 60000, (it.time / 1000) % 60, it.time % 1000
                )
            }
        } else {
            lyrics.lyrics.lines()
                .map { translator.translate(it).await() }
                .joinToString(separator = "\n")
        }
        return lyrics.copy(lyrics = newLyrics)
    }

    suspend fun clearModels() {
        val modelManager = RemoteModelManager.getInstance()
        val downloadedModels = modelManager.getDownloadedModels(TranslateRemoteModel::class.java).await()
        downloadedModels.forEach {
            modelManager.deleteDownloadedModel(it).await()
        }
    }
}
