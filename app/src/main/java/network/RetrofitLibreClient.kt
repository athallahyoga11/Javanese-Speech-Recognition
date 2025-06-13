package network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitLibreClient {
    private const val BASE_URL = "https://de4a-2404-c0-307a-2543-8427-e57e-bef6-3d27.ngrok-free.app/" // untuk Android emulator

    val apiService: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Waktu tunggu koneksi
            .readTimeout(60, TimeUnit.SECONDS)    // Waktu tunggu membaca response
            .writeTimeout(60, TimeUnit.SECONDS)   // Waktu tunggu mengirim data
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
