package com.zionhuang.music.utils

import com.zionhuang.music.models.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object Updater {
    var lastCheckTime = -1L
        private set

    suspend fun checkForUpdate(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val response = java.net.URL("https://iamraj.me/raj_music/update.json").readText()
            val json = JSONObject(response)
            
            val updateInfo = UpdateInfo(
                versionCode = json.getInt("versionCode"),
                versionName = json.getString("versionName"),
                updateUrl = json.getString("updateUrl"),
                releaseNotes = json.optString("releaseNotes", "")
            )
            
            lastCheckTime = System.currentTimeMillis()
            updateInfo
        }
    }
}
