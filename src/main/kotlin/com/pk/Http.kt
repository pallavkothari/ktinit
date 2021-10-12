package com.pk

import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * singleton
 */
object Http {
    private val client = OkHttpClient()

    /**
     * helper to execute and unpack the okhttp response
     */
    fun exec(req: Request): MyHttpResponse {
        val call = client.newCall(req)
        val response = call.execute()
        response.use { r ->
            return MyHttpResponse(
                r.isSuccessful,
                r.code,
                r.body.use { body ->
                    body?.string() ?: ""
                }
            )
        }
    }
}

data class MyHttpResponse(val successful: Boolean, val code: Int, val body: String)
