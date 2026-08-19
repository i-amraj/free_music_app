package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null,
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val deviceMake: String? = null,
        val deviceModel: String? = null,
        val osName: String? = null,
        val osVersion: String? = null,
        val gl: String,
        val hl: String,
        val visitorData: String? = null,
    )

    @Serializable
    data class ThirdParty(
        val embedUrl: String,
    )
}
