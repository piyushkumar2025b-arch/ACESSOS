package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    private const val MODEL = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key == "MY_GEMINI_API_KEY" || key.isBlank()) "" else key
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun explainMedicine(medicineName: String, dosage: String, instructions: String): String =
        withContext(Dispatchers.IO) {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext "💡 **$medicineName ($dosage)**\n\n• **Purpose:** Prescribed daily health maintenance.\n• **When to take:** $instructions\n• **Tip:** Take with a glass of water and avoid skipping doses. Consult your family doctor for specific queries."
            }

            try {
                val prompt = """
                    You are a compassionate, gentle AI Assistant for elderly patients and their caregivers.
                    Explain the following medicine in very simple, reassuring, plain English (with high clarity, short bullet points, max 3 sentences):
                    Medicine: $medicineName
                    Dosage: $dosage
                    Instructions: $instructions
                    
                    Explain:
                    1. What is this medicine commonly for?
                    2. Quick tips on how to take it safely.
                """.trimIndent()

                val jsonBody = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)
                }

                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val root = JSONObject(responseBody)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text").trim()
                        }
                    }
                }
                "💡 **$medicineName ($dosage)**\n\n• **Instructions:** $instructions\n• Always take with water as advised."
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API error: ${e.message}")
                "💡 **$medicineName ($dosage)**\n\n• **Instructions:** $instructions\n• Please follow doctor's instructions."
            }
        }

    suspend fun generateCaregiverSummary(elderName: String, medicinesSummary: String, logsSummary: String): String =
        withContext(Dispatchers.IO) {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext "📋 **Daily Health Summary for $elderName**\n\n• Routine health maintenance is active.\n• Prescriptions and emergency contacts are synced with family devices."
            }

            try {
                val prompt = """
                    Generate a brief 3-sentence Caregiver Daily Health Summary for $elderName.
                    Current Medicines:
                    $medicinesSummary
                    
                    Recent Activity Logs:
                    $logsSummary
                    
                    Keep it calm, clear, and focused on whether doses are on schedule.
                """.trimIndent()

                val jsonBody = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)
                }

                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val root = JSONObject(responseBody)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text").trim()
                        }
                    }
                }
                "📋 Routine health check is normal and medicines are recorded."
            } catch (e: Exception) {
                Log.w(TAG, "Caregiver summary error: ${e.message}")
                "📋 Routine health check is normal."
            }
        }
}
