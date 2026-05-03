package com.zionhuang.music.models

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val updateUrl: String,
    val releaseNotes: String
)
