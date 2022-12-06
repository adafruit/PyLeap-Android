package com.adafruit.pyleap.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.lang.reflect.Type

/**
 * Created by Antonio García (antonio@openroad.es)
 */

interface NetworkService {

    // region API
    @GET("/pyleap.github.io/pyleapProjects.json")
    suspend fun getAllProjects(): ProjectsDataFeed


    @Streaming // allows streaming data directly to filesystem without holding all contents in ram
    @GET
    suspend fun getUrl(@Url url: Uri): ResponseBody

    // endregion

    // region Singleton
    companion object {
        private const val baseUrl = "https://adafruit.github.io"

        var networkService: NetworkService? = null
        fun getInstance(context: Context): NetworkService {
            if (networkService == null) {
                //val gsonConverterFactory = GsonConverterFactory.create()
                val gsonConverterFactory = createGsonConverter(
                    ProjectsDataFeed::class.java, ProjectsFeedJsonDeserializer()
                )!!
                val okHttpClient = createOkHttpClient(
                    context = context, cacheSize = 5 * 1024 * 1024, maxAgeSeconds = 10
                )

                networkService =
                    Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(gsonConverterFactory)
                        .client(okHttpClient).build().create(NetworkService::class.java)
            }
            return networkService!!
        }

        private fun createGsonConverter(type: Type, typeAdapter: Any): Converter.Factory? {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(type, typeAdapter)
            val gson = gsonBuilder.create()
            return GsonConverterFactory.create(gson)
        }

        private fun isOnline(context: Context): Boolean {
            // from: https://stackoverflow.com/questions/51141970/check-internet-connectivity-android-in-kotlin
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }

            return false
        }

        @Suppress("SameParameterValue")
        private fun createOkHttpClient(
            context: Context, cacheSize: Long, maxAgeSeconds: Long
        ): OkHttpClient {
            // based on: https://bapspatil.medium.com/caching-with-retrofit-store-responses-offline-71439ed32fda

            return OkHttpClient.Builder()
                // Specify the cache we created earlier.
                .cache(Cache(context.cacheDir, cacheSize))
                // Add an Interceptor to the OkHttpClient.
                .addInterceptor { chain ->

                    // Get the request from the chain.
                    var request = chain.request()

                    /*
                    *  Leveraging the advantage of using Kotlin,
                    *  we initialize the request and change its header depending on whether
                    *  the device is connected to Internet or not.
                    */
                    request = if (isOnline(context))
                    /*
                    *  If there is Internet, get the cache that was stored [maxAgeSeconds] seconds ago.
                    *  If the cache is older than [maxAgeSeconds] seconds, then discard it,
                    *  and indicate an error in fetching the response.
                    *  The 'max-age' attribute is responsible for this behavior.
                    */ request.newBuilder().header(
                        "Cache-Control", "public, max-age=$maxAgeSeconds"
                    ).build()
                    else
                    /*
                    *  If there is no Internet, get the cache that was stored
                    *  If the cache is older than 7 days, then discard it,
                    *  and indicate an error in fetching the response.
                    *  The 'max-stale' attribute is responsible for this behavior.
                    *  The 'only-if-cached' attribute indicates to not retrieve new data; fetch the cache only instead.
                    */ request.newBuilder().header(
                        "Cache-Control", "public, only-if-cached"
                    ).build()
                    // End of if-else statement

                    // Add the modified request to the chain.
                    chain.proceed(request)
                }.build()
        }

    }
    // endregion
}

// region Download Files
sealed class ResponseBodyDownloadStatus {
    data class Progress(val value: Float) : ResponseBodyDownloadStatus()
    data class Finished(val file: File) : ResponseBodyDownloadStatus()
}

// based on : https://stackoverflow.com/a/65588935/2019574
fun ResponseBody.downloadToFileWithProgress(
    directory: File,
    filename: String,
): Flow<ResponseBodyDownloadStatus> = flow {
    emit(ResponseBodyDownloadStatus.Progress(0f))

    // flag to delete file if download errors or is cancelled
    var deleteFile = true
    val file = File(directory, "${filename}.${contentType()?.subtype}")

    try {
        byteStream().use { inputStream ->
            file.outputStream().use { outputStream ->
                val totalBytes = contentLength()
                val data = ByteArray(8_192)
                var progressBytes = 0L

                while (true) {
                    val bytes = inputStream.read(data)

                    if (bytes == -1) {
                        break
                    }

                    outputStream.write(data, 0, bytes)
                    progressBytes += bytes

                    val progress = progressBytes / totalBytes.toFloat()
                    emit(ResponseBodyDownloadStatus.Progress(value = progress))
                }

                when {
                    progressBytes < totalBytes -> throw Exception("Downloaded file missing bytes")
                    progressBytes > totalBytes -> throw Exception("Downloaded file bigger than expected")
                    else -> deleteFile = false
                }
            }
        }

        emit(ResponseBodyDownloadStatus.Finished(file))
    } finally {
        // check if download was successful
        if (deleteFile) {
            file.delete()
        }
    }
}.flowOn(Dispatchers.IO).distinctUntilChanged()


// endregion

