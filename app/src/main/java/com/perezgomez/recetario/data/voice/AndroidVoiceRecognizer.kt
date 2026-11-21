package com.perezgomez.recetario.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.perezgomez.recetario.domain.voice.VoiceRecognitionState
import com.perezgomez.recetario.domain.voice.VoiceRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AndroidVoiceRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
) : VoiceRecognizer {

    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Inactivo)
    override val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = VoiceRecognitionState.Escuchando
        }

        override fun onBeginningOfSpeech() {
            _state.value = VoiceRecognitionState.Escuchando
        }

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() = Unit

        override fun onError(error: Int) {
            val mensaje = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Error de grabación de audio."
                SpeechRecognizer.ERROR_CLIENT -> "Error en el cliente de voz."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de micrófono no concedido."
                SpeechRecognizer.ERROR_NETWORK -> "Error de red al procesar voz."
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera de red agotado."
                SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ninguna palabra. Intenta de nuevo."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor de voz está ocupado."
                SpeechRecognizer.ERROR_SERVER -> "Error en el servidor de reconocimiento."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó sonido. Intenta de nuevo."
                else -> "Error en el reconocimiento de voz ($error)."
            }
            _state.value = VoiceRecognitionState.Error(mensaje)
            destruirRecognizer()
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val texto = matches?.firstOrNull()?.trim().orEmpty()
            if (texto.isNotEmpty()) {
                _state.value = VoiceRecognitionState.Resultado(texto)
            } else {
                _state.value = VoiceRecognitionState.Inactivo
            }
            destruirRecognizer()
        }

        override fun onPartialResults(partialResults: Bundle?) = Unit

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    override fun iniciar(idioma: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = VoiceRecognitionState.Error("El servicio de reconocimiento de voz no está disponible en este dispositivo.")
            return
        }

        mainHandler.post {
            try {
                destruirRecognizer()

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(listener)
                }
                speechRecognizer = recognizer

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, idioma)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                _state.value = VoiceRecognitionState.Escuchando
                recognizer.startListening(intent)
            } catch (e: Exception) {
                _state.value = VoiceRecognitionState.Error(e.message ?: "No se pudo iniciar el dictado por voz.")
                destruirRecognizer()
            }
        }
    }

    override fun detener() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
        }
    }

    override fun limpiar() {
        mainHandler.post {
            destruirRecognizer()
            _state.value = VoiceRecognitionState.Inactivo
        }
    }

    private fun destruirRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {} finally {
            speechRecognizer = null
        }
    }
}
