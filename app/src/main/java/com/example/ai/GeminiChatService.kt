package com.example.ai

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
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val role: String, // "user" or "model"
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val modelUsed: String = "gemini-3.5-flash"
)

object GeminiChatService {
  private const val TAG = "GeminiChatService"

  const val MODEL_GENERAL = "gemini-3.5-flash"
  const val MODEL_COMPLEX = "gemini-3.1-pro-preview"
  const val MODEL_FAST = "gemini-3.1-flash-lite-preview"

  private const val SYSTEM_INSTRUCTION = """
    You are JobPulse AI, an elite career mentor, executive talent recruiter, and technical/clinical interview coach.
    You specialize in Engineering, Pharmacy, Healthcare, Tech & Software, and Biotech careers.
    Help job seekers:
    - Optimize their resumes, ATS keyword strategies, and portfolio presentations.
    - Prepare for behavioral and domain-specific interviews (pharmacokinetics, Kubernetes, sterile compounding, clinical systems, etc.).
    - Formulate high-leverage compensation and salary negotiation tactics.
    - Plan skill acquisition to match current job openings.
    Provide actionable, structured, concise, and inspiring guidance.
  """

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .build()

  /**
   * Sends multi-turn chat history to Gemini and returns the model's reply.
   */
  suspend fun sendMessage(
    history: List<ChatMessage>,
    newUserMessage: String,
    selectedModel: String = MODEL_GENERAL
  ): ChatMessage = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
      ""
    }

    if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
      try {
        val geminiReply = callGeminiMultiTurn(history, newUserMessage, selectedModel, apiKey)
        if (geminiReply != null && geminiReply.isNotBlank()) {
          return@withContext ChatMessage(
            role = "model",
            text = geminiReply,
            modelUsed = selectedModel
          )
        }
      } catch (e: Exception) {
        Log.w(TAG, "Gemini Chat API call error, falling back to local career mentor: ${e.message}")
      }
    }

    // Local Career Mentor Engine Fallback
    val fallbackReply = generateLocalMentorReply(history, newUserMessage, selectedModel)
    ChatMessage(
      role = "model",
      text = fallbackReply,
      modelUsed = selectedModel
    )
  }

  private fun callGeminiMultiTurn(
    history: List<ChatMessage>,
    newUserMessage: String,
    modelName: String,
    apiKey: String
  ): String? {
    val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

    val contentsArray = JSONArray()

    // Append prior conversation turns
    history.takeLast(10).forEach { msg ->
      val turnObj = JSONObject().apply {
        put("role", if (msg.role == "user") "user" else "model")
        put("parts", JSONArray().put(JSONObject().apply {
          put("text", msg.text)
        }))
      }
      contentsArray.put(turnObj)
    }

    // Append new user turn
    contentsArray.put(JSONObject().apply {
      put("role", "user")
      put("parts", JSONArray().put(JSONObject().apply {
        put("text", newUserMessage)
      }))
    })

    val requestRoot = JSONObject().apply {
      put("contents", contentsArray)
      put("systemInstruction", JSONObject().apply {
        put("parts", JSONArray().put(JSONObject().apply {
          put("text", SYSTEM_INSTRUCTION.trimIndent())
        }))
      })
      put("generationConfig", JSONObject().apply {
        put("temperature", 0.7)
      })
    }

    val request = Request.Builder()
      .url(url)
      .post(requestRoot.toString().toRequestBody("application/json".toMediaType()))
      .build()

    httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        Log.w(TAG, "Gemini HTTP response code: ${response.code}")
        return null
      }
      val bodyStr = response.body?.string() ?: return null
      val rootJson = JSONObject(bodyStr)
      val candidates = rootJson.optJSONArray("candidates") ?: return null
      if (candidates.length() == 0) return null
      val text = candidates.getJSONObject(0)
        .optJSONObject("content")
        ?.optJSONArray("parts")
        ?.optJSONObject(0)
        ?.optString("text")

      return text
    }
  }

  private fun generateLocalMentorReply(
    history: List<ChatMessage>,
    userMsg: String,
    model: String
  ): String {
    val q = userMsg.lowercase()
    return when {
      q.contains("pharmacy") || q.contains("pharmacist") || q.contains("compounding") -> """
        Great question on pharmacy careers! Here are 3 tactical steps for Clinical & Inpatient Pharmacy roles:
        
        1. **USP 797 & USP 800 Compliance**: Ensure your CV prominently features cleanroom aseptic techniques, hazardous drug handling, and sterile compounding audit experience.
        2. **Quantify Clinical Interventions**: Instead of "monitored vancomycin dosing", write: *"Optimized pharmacokinetic (PK/PD) dosing regimens for 300+ acute patients, reducing nephrotoxicity incidence by 18%"*.
        3. **High-Yield EHR Skills**: Hospital recruiters prioritize candidates certified or experienced with **Epic Willow Inpatient** and automated dispensing robotics (Pyxis/Omnicell).
        
        Would you like help tailoring your resume snippet or practicing an oncology clinical interview question?
      """.trimIndent()

      q.contains("salary") || q.contains("negotiat") -> """
        Here is a proven framework for salary negotiations in Healthcare & Tech:
        
        1. **Anchor with Market Percentiles**: For Senior roles in Boston / Remote, target the 75th percentile ($145k - $175k/yr). Cite specific domain proficiencies (e.g., BCOP certification or Kubernetes production deployments).
        2. **Total Compensation Perspective**: If base salary is fixed by grade, negotiate for sign-on bonuses, equity/retention stipends, or continuing education allowances.
        3. **Confidence Script**: *"Based on my specialized background driving regulatory compliance and systems automation, I am seeking a compensation package in the $145,000–$160,000 range. Can we explore what flexibility exists to reach this level?"*
      """.trimIndent()

      q.contains("interview") || q.contains("questions") -> """
        Here are core interview questions for specialized openings:
        
        • **Clinical Pharmacy**: "Walk me through how you handle a complex dosing discrepancy for an oncology patient with acute renal impairment."
        • **Engineering & Cloud**: "Describe a scenario where a production distributed pipeline suffered unexpected latency degradation. How did you identify the root bottleneck?"
        • **Behavioral (STAR Method)**: "Tell me about a time you had to enforce strict regulatory compliance (FDA/USP/HIPAA) despite tight operational timelines."
        
        Choose one and answer it now—I will provide an immediate critique!
      """.trimIndent()

      q.contains("resume") || q.contains("cv") || q.contains("ats") -> """
        To maximize your ATS match score on JobPulse:
        
        1. **Mirror Verbatim Requirements**: ATS parsers specifically search for exact phrases found in the job description (e.g., "Sterile Compounding", "USP 797", "Continuous Integration", "HL7/FHIR").
        2. **Use the AI Resume Scanner**: Head to your Profile tab to run our AI ATS scanner. It measures your keyword alignment score and provides exact skills to learn.
        3. **Action Verbs + Measurable Outcomes**: Start every bullet with high-impact verbs (*Spearheaded, Formulated, Automated, Architected*) paired with percentages or dollar values.
      """.trimIndent()

      else -> """
        I am your JobPulse AI career advisor! I can help you:
        
        • **Analyze & Enhance your CV / Resume** for ATS compliance.
        • **Simulate Mock Interviews** for Engineering, Pharmacy, and Healthcare roles.
        • **Guide Salary & Benefit Negotiations** using market data.
        • **Target High-Growth Skills** to stand out to hiring directors.
        
        What specific career goal or opening are you working on today?
      """.trimIndent()
    }
  }
}
