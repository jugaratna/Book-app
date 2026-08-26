package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class MedicalVoiceDictationManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText

    private val _rmsDbLevel = MutableStateFlow(0f)
    val rmsDbLevel: StateFlow<Float> = _rmsDbLevel

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var accumulatedBuffer = StringBuilder()

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorMessage.value = "Speech recognition is not available on this device."
            return
        }

        stopListening() // Reset any active session

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _errorMessage.value = null
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    _rmsDbLevel.value = (rmsdB + 2f).coerceIn(0f, 10f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _rmsDbLevel.value = 0f
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        SpeechRecognizer.ERROR_CLIENT -> "Client speech error."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio recording permission missing."
                        SpeechRecognizer.ERROR_NETWORK -> "Network connection required for speech service."
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out."
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech engine busy."
                        SpeechRecognizer.ERROR_SERVER -> "Speech server error."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
                        else -> "Speech recognition error ($error)"
                    }
                    if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        _errorMessage.value = message
                    }
                    _isListening.value = false
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val rawSpeech = matches[0]
                        val formattedSpeech = formatMedicalSpeechPunctuation(rawSpeech)
                        
                        if (accumulatedBuffer.isNotEmpty()) {
                            accumulatedBuffer.append(" ")
                        }
                        accumulatedBuffer.append(formattedSpeech)
                        _spokenText.value = accumulatedBuffer.toString()
                    }
                    _isListening.value = false
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val currentPartial = formatMedicalSpeechPunctuation(matches[0])
                        val preview = if (accumulatedBuffer.isNotEmpty()) {
                            "${accumulatedBuffer} $currentPartial"
                        } else {
                            currentPartial
                        }
                        _spokenText.value = preview
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _errorMessage.value = "Failed to initialize dictation: ${e.message}"
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore clean teardown error
        } finally {
            speechRecognizer = null
            _isListening.value = false
            _rmsDbLevel.value = 0f
        }
    }

    fun clearBuffer() {
        accumulatedBuffer.clear()
        _spokenText.value = ""
        _errorMessage.value = null
    }

    fun setInitialText(text: String) {
        accumulatedBuffer = StringBuilder(text)
        _spokenText.value = text
    }

    companion object {
        fun formatMedicalSpeechPunctuation(raw: String): String {
            var formatted = raw
            // Punctuation commands
            formatted = formatted.replace(Regex("(?i)\\b(period|full stop)\\b"), ".")
            formatted = formatted.replace(Regex("(?i)\\bcomma\\b"), ",")
            formatted = formatted.replace(Regex("(?i)\\bcolon\\b"), ":")
            formatted = formatted.replace(Regex("(?i)\\bsemi colon\\b"), ";")
            formatted = formatted.replace(Regex("(?i)\\bquestion mark\\b"), "?")
            formatted = formatted.replace(Regex("(?i)\\bexclamation mark\\b"), "!")
            formatted = formatted.replace(Regex("(?i)\\bnew line\\b"), "\n")
            formatted = formatted.replace(Regex("(?i)\\bnew paragraph\\b"), "\n\n")
            formatted = formatted.replace(Regex("(?i)\\bbullet point\\b"), "\n- ")
            formatted = formatted.replace(Regex("(?i)\\bnumbered point\\b"), "\n1. ")

            // Clinical formatting keywords
            formatted = formatted.replace(Regex("(?i)\\bclinical pearl\\b"), "\n\n> [!NOTE]\n> **CLINICAL PEARL:** ")
            formatted = formatted.replace(Regex("(?i)\\bred flag\\b"), "\n\n> [!WARNING]\n> **RED FLAG:** ")
            formatted = formatted.replace(Regex("(?i)\\bvital signs\\b"), "\n**Vital Signs:** ")
            formatted = formatted.replace(Regex("(?i)\\bpatient reports\\b"), "Patient reports ")
            formatted = formatted.replace(Regex("(?i)\\bdifferential diagnosis\\b"), "\n**Differential Diagnosis:**\n1. ")
            formatted = formatted.replace(Regex("(?i)\\bphysical examination\\b"), "\n### Physical Examination:\n- ")

            return formatted
        }
    }
}
