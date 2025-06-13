package network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class TranscriptionResponse(val transcription: String)
data class TranslateResponse(val translatedText: String)

interface ApiService {
    @Multipart
    @POST("transcribe/")
    suspend fun transcribeAudio(@Part audio: MultipartBody.Part): Response<TranscriptionResponse>

    // Endpoint translate, hanya kirim "q" karena FastAPI hanya butuh itu
    @FormUrlEncoded
    @POST("translate/")
    suspend fun translateText(
        @Field("q") text: String
    ): Response<TranslateResponse>
}
