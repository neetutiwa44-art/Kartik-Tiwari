package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// API Request/Response Models for Moshi
data class Part(val text: String)
data class Content(val parts: List<Part>)
data class GenerateContentRequest(val contents: List<Content>)

data class PartResponse(val text: String?)
data class ContentResponse(val parts: List<PartResponse>)
data class Candidate(val content: ContentResponse?)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    /**
     * Calls Gemini API to get a response. Falls back gracefully to high-quality procedural local content
     * in case of missing keys, errors, or timeouts.
     */
    suspend fun getAiResponse(prompt: String, fallbackText: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER") || apiKey.trim().length < 10) {
            Log.w(TAG, "Gemini API key is not configured, running in local fallback mode")
            return@withContext runLocalFallback(prompt, fallbackText)
        }

        try {
            val requestBody = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                )
            )
            val response = api.generateContent(apiKey, requestBody)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                runLocalFallback(prompt, fallbackText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Gemini API: ${e.message}", e)
            runLocalFallback(prompt, fallbackText)
        }
    }

    private fun runLocalFallback(prompt: String, defaultFallback: String): String {
        // Return structured responses matching the prompts if we can detect the keywords
        return when {
            prompt.contains("TOSS_SIMULATION") || prompt.contains("toss") -> {
                val teams = extractTeamsFromPrompt(prompt)
                val team1 = teams.first
                val team2 = teams.second
                val winner = if (Math.random() > 0.5) team1 else team2
                "★ [AI टॉस विश्लेषक] ★\n\n\"सिक्का हवा में तैरता हुआ नीचे गिरा... और ये हेड्स (Heads) है! **$winner** ने आज का बेहद महत्वपूर्ण टॉस जीत लिया है! इस सूखी, धूप से सिकी हुई पिच पर घास नाम मात्र की भी नहीं है। फैसला बिल्कुल साफ है - कप्तान ने मुस्कुराते हुए बल्लेबाजी का फैसला किया है! कप्तान **पहले बल्लेबाजी करेंगे (BAT first)** ताकि बड़ा स्कोर बनाकर गेंदबाजों पर दबाव बना सकें।\""
            }
            prompt.contains("SIX_RUNS") || prompt.contains("6 runs") || prompt.contains("six") -> {
                val commentary = listOf(
                    "अविश्वसनीय शॉट! बल्लेबाज ने क्रीज से बाहर निकलकर गेंद की पिच को पकड़ा और बेहद खूबसूरती से दर्शकों के बीच पहुंचा दिया! गगनचुंबी छक्का!",
                    "धमाका! ये गेंद सीधे स्लॉट में थी और बल्लेबाज ने उसे मैदान के बाहर भेज दिया! गेंदबाज बिल्कुल हैरान नजर आ रहे हैं! लाजवाब टाइमिंग और बेमिसाल ताकत!",
                    "कमाल का शॉट! डीप मिड-विकेट के ऊपर से बेहतरीन स्लॉग स्वीप! फील्डर को हिलने तक का मौका नहीं मिला। शानदार छक्का!",
                    "कलाईयों का जादुई इस्तेमाल! सिर्फ हल्का सा फ्लिक किया और गेंद हवा में तैरती हुई फाइन लेग सीमा रेखा के पार छह रनों के लिए चली गई!"
                )
                "🎙️ [हिन्दी कमेंट्री]\n\"${commentary.random()}\""
            }
            prompt.contains("FOUR_RUNS") || prompt.contains("4 runs") || prompt.contains("four") -> {
                val commentary = listOf(
                    "शानदार शॉट! कवर्स के बीच बने गैप में से गेंद को निकाला। फील्डर्स पीछे भागे लेकिन कोई मौका नहीं था, गेंद सीमा रेखा पार कर गई। चार रन!",
                    "शॉर्ट पिच गेंद और बेहतरीन पुल शॉट! बल्लेबाज के हाथों की रफ्तार लाजवाब थी, स्क्वायर लेग की दिशा में शानदार चौका!",
                    "क्लासिक सीधा ड्राइव! गेंदबाज के ठीक बगल से निकलती हुई गेंद, मीठी गूंज के साथ सीधे बाउंड्री पार कर गई!",
                    "गेंद बल्ले का बाहरी किनारा लेकर स्लिप के बगल से निकल गई और गोली की रफ्तार से बाउंड्री के बाहर चार रनों के लिए चली गई!"
                )
                "🎙️ [हिन्दी कमेंट्री]\n\"${commentary.random()}\""
            }
            prompt.contains("WICKET_OUT") || prompt.contains("wicket") || prompt.contains("out") -> {
                val commentary = listOf(
                    "आउट! एक शानदार कैच! बल्लेबाज ने बड़ा शॉट खेलने की कोशिश की, लेकिन गेंद हवा में बहुत ऊंची चली गई और मिड-ऑन पर फील्डर ने बिना कोई गलती किए कैच लपक लिया!",
                    "क्लीन बोल्ड! बिल्कुल सटीक यॉर्कर गेंद! बल्लेबाज ने बल्ला घुमाया लेकिन गेंद पैर के पास से होती हुई सीधे मिडिल स्टंप उड़ा गई! कमाल की गेंदबाजी!",
                    "रन आउट! क्या भयानक तालमेल की कमी! फील्डर का सीधा थ्रो नॉन-स्ट्राइकर एंड पर लगा और बल्लेबाज क्रीज से कोसों दूर थे! एक बड़ा विकेट गिरता हुआ!",
                    "हवा में गेंद... और डीप लेग पर अद्भुत डाइविंग कैच! फील्डर ने बहुत लंबी दूरी तय की, घुटनों के बल फिसलते हुए कैच को सुरक्षित लपका!"
                )
                "🎙️ [हिन्दी कमेंट्री]\n\"${commentary.random()}\""
            }
            else -> defaultFallback
        }
    }

    private fun extractTeamsFromPrompt(prompt: String): Pair<String, String> {
        val default = Pair("Red Team", "Blue Team")
        try {
            // Find "Team A:" and "Team B:" in prompt if possible
            val aIndex = prompt.indexOf("Team A:")
            val bIndex = prompt.indexOf("Team B:")
            if (aIndex != -1 && bIndex != -1) {
                var team1 = prompt.substring(aIndex + 7).substringBefore("\n").trim()
                var team2 = prompt.substring(bIndex + 7).substringBefore("\n").trim()
                if (team1.contains("\"")) team1 = team1.replace("\"", "")
                if (team2.contains("\"")) team2 = team2.replace("\"", "")
                return Pair(team1, team2)
            }
        } catch (e: Exception) {
            // fallback
        }
        return default
    }
}
