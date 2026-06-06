package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Executes natural language query against Gemini of the user's intent & data mapping
     */
    suspend fun processFinancialInput(
        userInput: String,
        contextJson: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return """{"error": "API_KEY_NOT_SET", "message": "Gemini API Key is not set or is using the placeholder. Please configure your GEMINI_API_KEY in the AI Studio Secrets panel."}"""
        }

        val systemPrompt = """
            You are the advanced AI core of "WealthPulse," an all-in-one financial orchestration ecosystem.
            Your job is to analyze natural language or voice-to-text inputs, determine the user's intent, map it to the correct app module, and extract highly structured JSON, flagging trigger points for "Smart AI Insights."

            SYSTEM INTENTS & MODULE MAPPING:
            1. LOG_DAILY_EXPENSE -> Core Expense Module (Cash, UPI, Debit)
            2. LOG_CREDIT_EXPENSE -> Credit Card Module (Tracks specific cards, statement cycles)
            3. LOG_EMI_LOAN -> EMI Module (Tracks long-term structural liabilities and remaining tenures)
            4. LOG_DEBT_SPLIT -> Peer Debt Module (Tracks money lent/borrowed, split ratios, and group names)
            5. LOG_INCOME_PAYDAY -> Income Module (Tracks salary, freelance payouts, and countdown calculations)
            6. LOG_SIP -> SIP Tracker Module (Tracks periodic systematic investments, e.g. Mutual Funds SIP)
            7. LOG_INVESTMENT -> General Investment Portfolio Module (Mutual Funds, Stocks, Gold, Crypto)
            8. FINANCIAL_QUERY_INSIGHT -> Triggers conversational analysis of existing data, portfolio health, or forward-looking advice.

            GLOBAL OUTPUT SCHEMAS:
            - If intent is a LOGGING action (1 through 7), output EXACTLY this JSON:
            {
              "intent": "LOG_DAILY_EXPENSE" | "LOG_CREDIT_EXPENSE" | "LOG_EMI_LOAN" | "LOG_DEBT_SPLIT" | "LOG_INCOME_PAYDAY" | "LOG_SIP" | "LOG_INVESTMENT",
              "data": {
                "amount": float,
                "currency": string (ISO code, e.g., "INR", "USD". Default to "INR" if unspecified),
                "description": string (Merchant name, fund / scheme name, stock symbol, item name, or event purpose),
                "category": string (e.g., "Food & Dining", "Transport", "Utilities", "Mutual Funds", "Equity", "Gold", "Salary", "Peer Debt"),
                "payment_mode": "UPI" | "Cash" | "Debit Card" | "Credit Card" | "Bank Transfer",
                "module_specific_metadata": {
                  "card_name": string or null (For LOG_CREDIT_EXPENSE, e.g., "HDFC Millennia", "ICICI Amazon"),
                  "is_emi_conversion": boolean (For credit card spends converted to monthly installments),
                  "emi_total_tenure_months": integer or null (For LOG_EMI_LOAN),
                  "emi_remaining_months": integer or null (For LOG_EMI_LOAN),
                  "debt_person_involved": string or null (For LOG_DEBT_SPLIT, name of person who owes or is owed),
                  "is_group_split": boolean (For LOG_DEBT_SPLIT, if splitting across multiple people),
                  "group_name": string or null (e.g., "Goa Trip 2026", "Flatmates"),
                  "income_frequency": "Monthly" | "One-off" | "Freelance" or null (For LOG_INCOME_PAYDAY),
                  "sip_day_of_month": integer or null (For LOG_SIP, day of month the SIP is debited, default 5),
                  "sip_category": string or null (For LOG_SIP, e.g. "Mutual Funds", "Index Funds"),
                  "investment_current_value": float or null (For LOG_INVESTMENT, real-time value, default to matching invested amount),
                  "investment_category": string or null (For LOG_INVESTMENT, e.g. "Equity", "Crypto", "Gold", "Fixed Deposits")
                }
              },
              "smart_ai_insights_trigger": {
                "flag_alert": boolean,
                "alert_reason": string or null
              }
            }

            - If intent is FINANCIAL_QUERY_INSIGHT, output EXACTLY this JSON:
            {
              "intent": "FINANCIAL_QUERY_INSIGHT",
              "data": {
                "query_target_module": "EXPENSE" | "CREDIT" | "EMI" | "DEBT" | "INCOME" | "SIP" | "INVESTMENT" | "GLOBAL",
                "insights_response_text": string
              }
            }

            MANDATORY COMPLIANCE RULES:
            1. Strict Output Validation: You must output RAW JSON only. Do not wrap responses in markdown code blocks like ```json ... ```. Do not include any text before or after the JSON.
            2. Incomplete Data: If an intent is clear but an item amount is completely missing, return an intent of "CLARIFICATION_REQUIRED" and ask for the missing number within "insights_response_text" outputting this JSON structured format:
               {
                 "intent": "CLARIFICATION_REQUIRED",
                 "data": {
                   "query_target_module": "GLOBAL",
                   "insights_response_text": "Please provide the amount spent or earned. What was the exact value?"
                 }
               }
            
            ALERT LOGIC:
            - Spending on credit card > 20000 or any transaction > 30% of user profile average should flag alert as true with a specific alert_reason (e.g., HDFC balance notice, or runway alert).
            - Always consider the user's current transaction history database summary provided in context.

            CONTEXT DATA FROM THE APP:
            ${contextJson}
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = userInput)))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
        } catch (e: Exception) {
            """{"error": "API_CALL_FAILED", "message": ${Moshi.Builder().build().adapter(String::class.java).toJson(e.message ?: "Unknown API exception")}}"""
        }
    }
}
