package com.example.data.model

enum class AiEngine(
    val id: String,
    val displayName: String,
    val provider: String,
    val modelTag: String,
    val badgeColorHex: Long,
    val description: String
) {
    GEMINI(
        id = "gemini",
        displayName = "Gemini 2.5 Flash",
        provider = "Google AI",
        modelTag = "gemini-2.5-flash",
        badgeColorHex = 0xFF0284C7,
        description = "High-speed multimodal medical synthesizer with real-time knowledge"
    ),
    CHATGPT(
        id = "chatgpt",
        displayName = "ChatGPT-4o",
        provider = "OpenAI",
        modelTag = "gpt-4o",
        badgeColorHex = 0xFF10A37F,
        description = "Clinical differential diagnosis, case simulations & exam vignettes"
    ),
    CLAUDE(
        id = "claude",
        displayName = "Claude 3.5 Sonnet",
        provider = "Anthropic",
        modelTag = "claude-3-5-sonnet",
        badgeColorHex = 0xFFD97706,
        description = "Academic textbook prose, clinical guideline citations & publication tone"
    ),
    PERPLEXITY(
        id = "perplexity",
        displayName = "Perplexity AI",
        provider = "Perplexity",
        modelTag = "sonar-pro",
        badgeColorHex = 0xFF2563EB,
        description = "Live PubMed citation search, evidence grading & guideline verification"
    ),
    DEEPSEEK(
        id = "deepseek",
        displayName = "DeepSeek R1",
        provider = "DeepSeek",
        modelTag = "deepseek-reasoner",
        badgeColorHex = 0xFF7C3AED,
        description = "Step-by-step diagnostic chain-of-thought & surgical decision matrices"
    );

    companion object {
        fun fromId(id: String): AiEngine {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GEMINI
        }
    }
}
