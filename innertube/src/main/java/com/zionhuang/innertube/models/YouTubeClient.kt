package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeClient(
    val clientName: String,
    val clientVersion: String,
    val api_key: String,
    val userAgent: String,
    val deviceMake: String? = null,
    val deviceModel: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
    val referer: String? = null,
) {
    fun toContext(locale: YouTubeLocale, visitorData: String?) = Context(
        client = Context.Client(
            clientName = clientName,
            clientVersion = clientVersion,
            deviceMake = deviceMake,
            deviceModel = deviceModel,
            osName = osName,
            osVersion = osVersion,
            gl = locale.gl,
            hl = locale.hl,
            visitorData = visitorData
        )
    )

    companion object {
        private const val REFERER_YOUTUBE_MUSIC = "https://music.youtube.com/"

        private const val USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        private const val USER_AGENT_ANDROID = "com.google.android.youtube/21.26.364 (Linux; U; Android 11) gzip"
        private const val USER_AGENT_IOS = "com.google.ios.youtube/21.26.4 (iPhone16,2; U; CPU iOS 18_3_2 like Mac OS X;)"

        val ANDROID_MUSIC = YouTubeClient(
            clientName = "ANDROID_MUSIC",
            clientVersion = "7.29.51",
            api_key = "AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI",
            userAgent = USER_AGENT_ANDROID
        )

        val ANDROID = YouTubeClient(
            clientName = "ANDROID",
            clientVersion = "21.26.364",
            api_key = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w",
            userAgent = USER_AGENT_ANDROID,
            osName = "Android",
            osVersion = "11",
        )

        val WEB = YouTubeClient(
            clientName = "WEB",
            clientVersion = "2.20241126.01.00",
            api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX3",
            userAgent = USER_AGENT_WEB
        )

        val WEB_REMIX = YouTubeClient(
            clientName = "WEB_REMIX",
            clientVersion = "1.20241113.01.00",
            api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30",
            userAgent = USER_AGENT_WEB,
            referer = REFERER_YOUTUBE_MUSIC
        )

        val TVHTML5 = YouTubeClient(
            clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
            clientVersion = "2.0",
            api_key = "AIzaSyDCU8hByM-4DrUqRUYnGn-3llEO78bcxq8",
            userAgent = "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
        )

        val IOS = YouTubeClient(
            clientName = "IOS",
            clientVersion = "20.01.1",
            api_key = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc",
            userAgent = "com.google.ios.youtube/20.01.1 (iPhone16,2; U; CPU iOS 18_2_1 like Mac OS X;)",
            deviceMake = "Apple",
            deviceModel = "iPhone16,2",
            osName = "iPhone",
            osVersion = "18.2.1.22C161",
        )

        val ANDROID_VR_NO_AUTH = YouTubeClient(
            clientName = "ANDROID_VR",
            clientVersion = "1.57.19",
            api_key = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w",
            userAgent = "com.google.android.apps.youtube.vr.oculus/1.57.19 (Linux; U; Android 12; en_US; Oculus Quest 2; Build/SQ3A.220605.009.A1; Cronet/116.0.5845.240)",
            deviceMake = "Oculus",
            deviceModel = "Oculus Quest 2",
            osName = "Android",
            osVersion = "12",
        )

        val ANDROID_TESTSUITE = YouTubeClient(
            clientName = "ANDROID_TESTSUITE",
            clientVersion = "1.9",
            api_key = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w",
            userAgent = "com.google.android.youtube/1.9 (Linux; U; Android 11)",
            osName = "Android",
            osVersion = "11",
        )

        val WEB_EMBEDDED_PLAYER = YouTubeClient(
            clientName = "WEB_EMBEDDED_PLAYER",
            clientVersion = "1.20241126.01.00",
            api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX3",
            userAgent = USER_AGENT_WEB,
            referer = "https://www.youtube.com/"
        )
    }
}
