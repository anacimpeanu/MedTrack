package com.medtrack.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class AdviceResponse(
    val slip: AdviceSlip
)

data class AdviceSlip(
    val advice: String
)

data class QuoteResponse(
    val q: String,
    val a: String
)

interface AdviceApi {
    @GET("advice")
    suspend fun getAdvice(): AdviceResponse
}

interface QuoteApi {
    @GET("api/random")
    suspend fun getQuote(): List<QuoteResponse>
}

object HealthRemoteDataSource {
    private val adviceApi: AdviceApi = Retrofit.Builder()
        .baseUrl("https://api.adviceslip.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AdviceApi::class.java)

    private val quoteApi: QuoteApi = Retrofit.Builder()
        .baseUrl("https://zenquotes.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(QuoteApi::class.java)

    suspend fun getHealthTip(): String {
        return adviceApi.getAdvice().slip.advice
    }

    suspend fun getMotivationQuote(): String {
        val quote = quoteApi.getQuote().firstOrNull()
        return if (quote == null) {
            "Small steps every day."
        } else {
            "\"${quote.q}\" — ${quote.a}"
        }
    }
}