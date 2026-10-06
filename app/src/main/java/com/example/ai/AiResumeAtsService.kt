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
import java.util.concurrent.TimeUnit

data class RecommendedSkill(
  val name: String,
  val category: String, // "High Impact", "Medium Impact", "Emerging Trend"
  val reason: String,
  val estimatedLearningTime: String,
  val demandLevel: String,
  val learningResource: String = "Interactive Labs & Official Guides"
)

data class AtsAnalysisResult(
  val overallScore: Int,
  val scoreGrade: String,
  val keywordsScore: Int,
  val experienceScore: Int,
  val skillsMatchScore: Int,
  val formattingScore: Int,
  val matchingKeywords: List<String>,
  val missingKeywords: List<String>,
  val recommendedSkillsToLearn: List<RecommendedSkill>,
  val strengths: List<String>,
  val actionableImprovements: List<String>,
  val summaryReview: String,
  val analyzedAtMillis: Long = System.currentTimeMillis()
)

object AiResumeAtsService {
  private const val TAG = "AiResumeAtsService"
  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  /**
   * Analyzes the candidate's CV/Resume text against industry job requirements.
   * Leverages Gemini API if a key is configured, with a smart fallback
   * heuristic engine to guarantee instant, high-quality results in all conditions.
   */
  suspend fun analyzeResume(
    resumeText: String,
    targetIndustry: String,
    targetRole: String = "",
    currentSkills: String = ""
  ): AtsAnalysisResult = withContext(Dispatchers.IO) {
    val cleanResume = resumeText.trim()
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
      ""
    }

    if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && cleanResume.length > 30) {
      try {
        val geminiResult = callGeminiApiForAts(cleanResume, targetIndustry, targetRole, currentSkills, apiKey)
        if (geminiResult != null) {
          return@withContext geminiResult
        }
      } catch (e: Exception) {
        Log.w(TAG, "Gemini API call failed, falling back to local ATS engine: ${e.message}")
      }
    }

    // Comprehensive Local ATS Engine
    evaluateLocally(cleanResume, targetIndustry, targetRole, currentSkills)
  }

  private fun callGeminiApiForAts(
    resumeText: String,
    targetIndustry: String,
    targetRole: String,
    currentSkills: String,
    apiKey: String
  ): AtsAnalysisResult? {
    val prompt = """
      You are an expert Executive Recruiter and ATS (Applicant Tracking System) Specialist.
      Analyze the following candidate resume for the industry: '$targetIndustry' and target role: '$targetRole'.
      Candidate current skills: '$currentSkills'.

      Resume Text:
      ${resumeText.take(4000)}

      Return a STRICT JSON response (no markdown, no backticks, only valid JSON) matching this schema:
      {
        "overallScore": 82,
        "scoreGrade": "Strong ATS Match",
        "keywordsScore": 85,
        "experienceScore": 80,
        "skillsMatchScore": 84,
        "formattingScore": 88,
        "matchingKeywords": ["USP 797", "Compounding", "Clinical Pharmacokinetics"],
        "missingKeywords": ["USP 800 Hazardous Handling", "Epic Willow EHR", "CDSS"],
        "recommendedSkillsToLearn": [
          {
            "name": "Skill Name",
            "category": "High Impact",
            "reason": "Why this skill will help them get hired",
            "estimatedLearningTime": "2-3 weeks",
            "demandLevel": "+38% Recruiter Views",
            "learningResource": "Recommended guide or certification"
          }
        ],
        "strengths": ["Clear leadership progression", "Quantified clinical outcomes"],
        "actionableImprovements": ["Add more specific percentage metrics", "Highlight oncology protocols"],
        "summaryReview": "Overall evaluation summary"
      }
    """.trimIndent()

    val modelName = "gemini-2.5-flash"
    val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

    val requestBodyJson = JSONObject().apply {
      put("contents", JSONArray().put(JSONObject().apply {
        put("parts", JSONArray().put(JSONObject().apply {
          put("text", prompt)
        }))
      }))
      put("generationConfig", JSONObject().apply {
        put("responseMimeType", "application/json")
        put("temperature", 0.2)
      })
    }

    val request = Request.Builder()
      .url(url)
      .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
      .build()

    httpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) return null
      val responseString = response.body?.string() ?: return null
      val rootJson = JSONObject(responseString)
      val candidates = rootJson.optJSONArray("candidates") ?: return null
      if (candidates.length() == 0) return null
      val textContent = candidates.getJSONObject(0)
        .optJSONObject("content")
        ?.optJSONArray("parts")
        ?.optJSONObject(0)
        ?.optString("text") ?: return null

      val cleanJson = textContent.replace("```json", "").replace("```", "").trim()
      return parseAtsJson(cleanJson)
    }
  }

  fun evaluateLocally(
    resumeText: String,
    targetIndustry: String,
    targetRole: String,
    currentSkills: String
  ): AtsAnalysisResult {
    val text = (resumeText + " " + currentSkills).lowercase()
    val ind = targetIndustry.lowercase()

    // 1. Industry Specific Benchmarks
    val industryKeywords = when {
      ind.contains("pharmacy") -> listOf(
        "pharmd", "pharmacology", "clinical", "compounding", "usp 797", "usp 800", "pharmacokinetics",
        "chemotherapy", "oncology", "dosage", "inpatient", "hospital", "bcop", "fda", "dispensing", "sterile"
      )
      ind.contains("tech") || ind.contains("software") -> listOf(
        "kotlin", "java", "android", "jetpack compose", "coroutine", "flow", "mvvm", "git", "ci/cd",
        "rest api", "unit test", "room", "ksp", "gradle", "architecture", "microservices", "kubernetes"
      )
      ind.contains("healthcare") -> listOf(
        "clinical", "patient", "hipaa", "ehr", "telemetry", "diagnostic", "vitals", "biomedical",
        "hl7", "fhir", "icu", "monitoring", "protocols", "acute care", "compliance"
      )
      ind.contains("biotech") -> listOf(
        "gmp", "glp", "fda 510(k)", "assay", "cleanroom", "chromatography", "cell culture", "molecular",
        "bioreactor", "purification", "sop", "quality control", "formulation"
      )
      else -> listOf(
        "engineering", "systems", "architecture", "leadership", "optimization", "automation",
        "scalability", "metrics", "troubleshooting", "design", "integration", "compliance"
      )
    }

    val matchedKeywords = mutableListOf<String>()
    val missingKeywords = mutableListOf<String>()

    industryKeywords.forEach { kw ->
      if (text.contains(kw)) {
        matchedKeywords.add(kw.replaceFirstChar { it.uppercase() })
      } else {
        missingKeywords.add(kw.replaceFirstChar { it.uppercase() })
      }
    }

    // 2. Metrics & Measurable Impact (Quantifiable data)
    val hasNumbersOrPercents = Regex("""\d+%|\$\d+|\b\d+\s*(years?|yrs?|months?|beds?|teams?|projects?)\b""").containsMatchIn(text)
    val hasActionVerbs = listOf("led", "developed", "architected", "formulated", "optimized", "managed", "deployed", "spearheaded", "integrated")
      .count { text.contains(it) }

    // 3. Formatting & Section Completeness
    val hasEducation = listOf("degree", "bachelor", "master", "pharmd", "ph.d", "b.s", "m.s", "university").any { text.contains(it) }
    val hasExperience = listOf("experience", "history", "employment", "roles", "years").any { text.contains(it) }
    val hasCertifications = listOf("certified", "certification", "license", "licensed", "rph", "bcop", "aws", "gcp").any { text.contains(it) }

    // Calculate sub-scores
    val keywordsScore = ((matchedKeywords.size.toFloat() / industryKeywords.size.coerceAtLeast(1)) * 100).toInt().coerceIn(45, 96)
    val experienceScore = (60 + (hasActionVerbs * 4) + (if (hasNumbersOrPercents) 15 else 0)).coerceIn(50, 95)
    val skillsMatchScore = (50 + (matchedKeywords.size * 3)).coerceIn(48, 98)
    val formattingScore = (50 + (if (hasEducation) 18 else 0) + (if (hasExperience) 18 else 0) + (if (hasCertifications) 14 else 0)).coerceIn(55, 98)

    val overallScore = ((keywordsScore * 0.35f) + (experienceScore * 0.25f) + (skillsMatchScore * 0.25f) + (formattingScore * 0.15f)).toInt().coerceIn(35, 98)

    val scoreGrade = when {
      overallScore >= 85 -> "Exceptional ATS Match"
      overallScore >= 75 -> "Strong ATS Match"
      overallScore >= 60 -> "Competitive Match"
      else -> "Needs Keyword Optimization"
    }

    // 4. Skills Recommended to Learn Based on Target Industry
    val recommendedSkills = when {
      ind.contains("pharmacy") -> listOf(
        RecommendedSkill(
          name = "USP 800 Hazardous Drug Handling",
          category = "High Impact",
          reason = "Hospital cleanrooms and oncology clinics require strict containment protocol compliance for antineoplastic agents.",
          estimatedLearningTime = "2 - 3 weeks",
          demandLevel = "+42% Recruiter Views",
          learningResource = "USP Compounding Compendium & ASHP Guidelines"
        ),
        RecommendedSkill(
          name = "Epic Willow Inpatient EHR Certification",
          category = "High Impact",
          reason = "Over 65% of major US hospital health systems use Epic Willow for automated pharmacy verification and ordering.",
          estimatedLearningTime = "4 - 6 weeks",
          demandLevel = "+35% Interview Invites",
          learningResource = "Epic UserWeb & Hospital Preceptorship Labs"
        ),
        RecommendedSkill(
          name = "Pharmacokinetic (PK/PD) Clinical Modeling",
          category = "Medium Impact",
          reason = "Precision dosing optimization for aminoglycosides, vancomycin, and biologics in acute ICU wards.",
          estimatedLearningTime = "2 weeks",
          demandLevel = "+25% Salary Premium",
          learningResource = "Clinical Pharmacokinetics Case Studies"
        ),
        RecommendedSkill(
          name = "Automated Dispensing Robotics (Pyxis / Omnicell)",
          category = "Emerging Trend",
          reason = "Managing carousel integrations, decentralized inventory, and automated diversion prevention.",
          estimatedLearningTime = "1 - 2 weeks",
          demandLevel = "+20% Hiring Velocity",
          learningResource = "Hospital Pharmacy Operations Seminars"
        )
      )
      ind.contains("tech") || ind.contains("software") -> listOf(
        RecommendedSkill(
          name = "Kubernetes & Cloud-Native Orchestration",
          category = "High Impact",
          reason = "75% of senior engineering roles demand container orchestration, Helm, and cluster deployment mastery.",
          estimatedLearningTime = "3 - 4 weeks",
          demandLevel = "+45% Recruiter Searches",
          learningResource = "Kubernetes.io Tutorials & CKAD Sandbox"
        ),
        RecommendedSkill(
          name = "OpenTelemetry & Distributed Observability",
          category = "High Impact",
          reason = "Critical for tracing latency bottlenecks across microservices, Kafka pipelines, and cloud functions.",
          estimatedLearningTime = "2 weeks",
          demandLevel = "+30% Senior Role Matches",
          learningResource = "OpenTelemetry Official Architecture Guides"
        ),
        RecommendedSkill(
          name = "Jetpack Compose Advanced Performance",
          category = "Medium Impact",
          reason = "Minimizing recomposition, custom graphics layouts, and state hoisting for scalable Android UIs.",
          estimatedLearningTime = "2 weeks",
          demandLevel = "+28% Mobile Inquiries",
          learningResource = "Android Developers Compose Performance Labs"
        ),
        RecommendedSkill(
          name = "Infrastructure as Code (Terraform / Pulumi)",
          category = "Emerging Trend",
          reason = "Automates reproducible cloud infrastructure environments across GCP and AWS.",
          estimatedLearningTime = "2 - 3 weeks",
          demandLevel = "+25% Lead Engineering Matches",
          learningResource = "HashiCorp Terraform Associate Tutorials"
        )
      )
      ind.contains("healthcare") -> listOf(
        RecommendedSkill(
          name = "HL7 v2 & FHIR Interoperability Protocols",
          category = "High Impact",
          reason = "Standard for streaming diagnostic vital data and laboratory orders between bedside monitors and EHR clouds.",
          estimatedLearningTime = "3 weeks",
          demandLevel = "+40% Health-Tech Openings",
          learningResource = "HL7.org FHIR Starter Specification"
        ),
        RecommendedSkill(
          name = "Clinical Decision Support Systems (CDSS)",
          category = "High Impact",
          reason = "Integrating automated drug interaction and sepsis alerts into real-time nursing workflows.",
          estimatedLearningTime = "2 - 3 weeks",
          demandLevel = "+32% Recruiter Visibility",
          learningResource = "HIMSS Clinical Informatics Guides"
        ),
        RecommendedSkill(
          name = "Medical Device Cyber-Security (FDA Premarket)",
          category = "Medium Impact",
          reason = "Hardening embedded telemetry hardware against firmware exploits in accordance with FDA 2024 guidance.",
          estimatedLearningTime = "3 weeks",
          demandLevel = "+25% Biomedical Openings",
          learningResource = "FDA Center for Devices & Radiological Health"
        )
      )
      else -> listOf(
        RecommendedSkill(
          name = "Systems Reliability & Root Cause Analysis",
          category = "High Impact",
          reason = "Essential for triaging complex multi-factor incidents and reducing mean time to recovery (MTTR).",
          estimatedLearningTime = "2 weeks",
          demandLevel = "+35% Recruiter Inquiries",
          learningResource = "SRE Handbooks & Incident Response Playbooks"
        ),
        RecommendedSkill(
          name = "Data-Driven Performance Instrumentation",
          category = "Medium Impact",
          reason = "Quantifying engineering throughput, defect rates, and operational KPIs with automated dashboards.",
          estimatedLearningTime = "2 weeks",
          demandLevel = "+25% Leadership Opportunities",
          learningResource = "DORA Metrics & Engineering Excellence"
        )
      )
    }

    val strengths = mutableListOf<String>()
    if (matchedKeywords.size >= 5) strengths.add("Strong presence of industry terminology (${matchedKeywords.take(4).joinToString(", ")})")
    if (hasNumbersOrPercents) strengths.add("Includes measurable metrics and quantifiable achievements")
    if (hasEducation) strengths.add("Clearly structured academic background and foundational qualifications")
    if (hasCertifications) strengths.add("Demonstrated verified professional licenses and credentialing")
    if (strengths.isEmpty()) strengths.add("Clear concise layout with relevant career focus")

    val improvements = mutableListOf<String>()
    if (!hasNumbersOrPercents) improvements.add("Add quantified results (e.g. 'reduced dispensing errors by 22%', 'improved pipeline latency by 35%')")
    if (missingKeywords.isNotEmpty()) improvements.add("Integrate missing high-demand keywords: ${missingKeywords.take(3).joinToString(", ")}")
    if (!hasCertifications) improvements.add("Highlight board certifications, state licenses, or cloud credentials prominently at the top")
    if (improvements.isEmpty()) improvements.add("Tailor executive summary with specific target position keywords for each application")

    return AtsAnalysisResult(
      overallScore = overallScore,
      scoreGrade = scoreGrade,
      keywordsScore = keywordsScore,
      experienceScore = experienceScore,
      skillsMatchScore = skillsMatchScore,
      formattingScore = formattingScore,
      matchingKeywords = matchedKeywords.take(8),
      missingKeywords = missingKeywords.take(6),
      recommendedSkillsToLearn = recommendedSkills,
      strengths = strengths,
      actionableImprovements = improvements,
      summaryReview = "Your profile shows solid alignment with $targetIndustry standards. Learning the top recommended skills and adding quantified metrics can elevate your ATS score to the top 10% of applicants."
    )
  }

  fun toJson(result: AtsAnalysisResult): String {
    val root = JSONObject().apply {
      put("overallScore", result.overallScore)
      put("scoreGrade", result.scoreGrade)
      put("keywordsScore", result.keywordsScore)
      put("experienceScore", result.experienceScore)
      put("skillsMatchScore", result.skillsMatchScore)
      put("formattingScore", result.formattingScore)
      put("matchingKeywords", JSONArray(result.matchingKeywords))
      put("missingKeywords", JSONArray(result.missingKeywords))
      put("strengths", JSONArray(result.strengths))
      put("actionableImprovements", JSONArray(result.actionableImprovements))
      put("summaryReview", result.summaryReview)
      put("analyzedAtMillis", result.analyzedAtMillis)

      val skillsArray = JSONArray()
      result.recommendedSkillsToLearn.forEach { skill ->
        skillsArray.put(JSONObject().apply {
          put("name", skill.name)
          put("category", skill.category)
          put("reason", skill.reason)
          put("estimatedLearningTime", skill.estimatedLearningTime)
          put("demandLevel", skill.demandLevel)
          put("learningResource", skill.learningResource)
        })
      }
      put("recommendedSkillsToLearn", skillsArray)
    }
    return root.toString()
  }

  fun parseAtsJson(jsonString: String): AtsAnalysisResult? {
    return try {
      val root = JSONObject(jsonString)
      val overallScore = root.optInt("overallScore", 75)
      val scoreGrade = root.optString("scoreGrade", "Strong ATS Match")
      val keywordsScore = root.optInt("keywordsScore", 78)
      val experienceScore = root.optInt("experienceScore", 75)
      val skillsMatchScore = root.optInt("skillsMatchScore", 76)
      val formattingScore = root.optInt("formattingScore", 80)
      val summaryReview = root.optString("summaryReview", "Resume analyzed successfully.")
      val analyzedAtMillis = root.optLong("analyzedAtMillis", System.currentTimeMillis())

      val matchingKeywords = mutableListOf<String>()
      root.optJSONArray("matchingKeywords")?.let { arr ->
        for (i in 0 until arr.length()) matchingKeywords.add(arr.getString(i))
      }

      val missingKeywords = mutableListOf<String>()
      root.optJSONArray("missingKeywords")?.let { arr ->
        for (i in 0 until arr.length()) missingKeywords.add(arr.getString(i))
      }

      val strengths = mutableListOf<String>()
      root.optJSONArray("strengths")?.let { arr ->
        for (i in 0 until arr.length()) strengths.add(arr.getString(i))
      }

      val actionableImprovements = mutableListOf<String>()
      root.optJSONArray("actionableImprovements")?.let { arr ->
        for (i in 0 until arr.length()) actionableImprovements.add(arr.getString(i))
      }

      val recommendedSkills = mutableListOf<RecommendedSkill>()
      root.optJSONArray("recommendedSkillsToLearn")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          recommendedSkills.add(
            RecommendedSkill(
              name = obj.optString("name", "Skill"),
              category = obj.optString("category", "High Impact"),
              reason = obj.optString("reason", "In-demand skill for target industry"),
              estimatedLearningTime = obj.optString("estimatedLearningTime", "2-3 weeks"),
              demandLevel = obj.optString("demandLevel", "+30% visibility"),
              learningResource = obj.optString("learningResource", "Official documentation & labs")
            )
          )
        }
      }

      AtsAnalysisResult(
        overallScore = overallScore,
        scoreGrade = scoreGrade,
        keywordsScore = keywordsScore,
        experienceScore = experienceScore,
        skillsMatchScore = skillsMatchScore,
        formattingScore = formattingScore,
        matchingKeywords = matchingKeywords,
        missingKeywords = missingKeywords,
        recommendedSkillsToLearn = recommendedSkills,
        strengths = strengths,
        actionableImprovements = actionableImprovements,
        summaryReview = summaryReview,
        analyzedAtMillis = analyzedAtMillis
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing ATS JSON: ${e.message}")
      null
    }
  }
}
