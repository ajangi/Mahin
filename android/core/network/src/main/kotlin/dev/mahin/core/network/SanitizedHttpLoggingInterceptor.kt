package dev.mahin.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Logs method + path only. Never attach HttpLoggingInterceptor at BODY/HEADERS
 * in any build type — reproductive payloads must not enter logcat.
 */
class SanitizedHttpLoggingInterceptor(
    private val sink: (String) -> Unit = {},
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        sink("${request.method} $path")
        return chain.proceed(request)
    }
}

object HttpLoggingPolicy {
    fun rejectBodyLoggingInterceptor(className: String) {
        require(!className.contains("okhttp3.logging.HttpLoggingInterceptor")) {
            "HTTP body/header logging interceptors are forbidden"
        }
    }
}
