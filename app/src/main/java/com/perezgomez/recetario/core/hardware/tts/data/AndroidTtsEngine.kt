package com.perezgomez.recetario.core.hardware.tts.data

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.perezgomez.recetario.core.hardware.tts.domain.TtsEngine
import com.perezgomez.recetario.core.hardware.tts.domain.TtsEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación nativa de [TtsEngine] utilizando [android.speech.tts.TextToSpeech].
 * Administra el ciclo de vida del sintetizador, la configuración en idioma español,
 * la propagación de eventos mediante [UtteranceProgressListener] y la liberación de recursos.
 */
@Singleton
class AndroidTtsEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : TtsEngine, TextToSpeech.OnInitListener {

    private val tag = "RecetarioTTS"

    private var tts: TextToSpeech? = null
    private var inicializado = false
    private var listener: TtsEventListener? = null

    // Almacena una petición pendiente de locución si se invocó hablar() antes de que concluya OnInit
    private var peticionPendiente: Pair<String, String>? = null

    init {
        inicializar()
    }

    private fun inicializar() {
        if (tts == null) {
            Log.i(tag, "Instanciando TextToSpeech nativo de Android...")
            tts = TextToSpeech(context, this)
        }
    }

    override fun setListener(listener: TtsEventListener?) {
        this.listener = listener
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsInstance = tts
            if (ttsInstance != null) {
                Log.i(tag, "TextToSpeech inicializado con éxito. Evaluando idiomas disponibles...")
                // Intentar configurar español (España / México / genérico)
                var resultadoIdioma = ttsInstance.setLanguage(Locale("es", "ES"))
                if (resultadoIdioma == TextToSpeech.LANG_MISSING_DATA || resultadoIdioma == TextToSpeech.LANG_NOT_SUPPORTED) {
                    resultadoIdioma = ttsInstance.setLanguage(Locale("es", "MX"))
                }
                if (resultadoIdioma == TextToSpeech.LANG_MISSING_DATA || resultadoIdioma == TextToSpeech.LANG_NOT_SUPPORTED) {
                    resultadoIdioma = ttsInstance.setLanguage(Locale("es"))
                }

                if (resultadoIdioma == TextToSpeech.LANG_MISSING_DATA || resultadoIdioma == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(tag, "Idioma español NO disponible en este dispositivo (código $resultadoIdioma).")
                    listener?.onError("El idioma español no está disponible en el sintetizador de voz de este dispositivo.")
                    inicializado = false
                    return
                }

                Log.i(tag, "Idioma español configurado correctamente: ${ttsInstance.language}")
                configurarListenerProgreso(ttsInstance)
                inicializado = true

                // Si había una petición previa en espera de inicialización, procesarla
                peticionPendiente?.let { (texto, utteranceId) ->
                    peticionPendiente = null
                    hablar(texto, utteranceId)
                }
            }
        } else {
            Log.e(tag, "Fallo en onInit() de TextToSpeech (status: $status)")
            listener?.onError("Error al inicializar el servicio TextToSpeech de Android.")
            inicializado = false
        }
    }

    private fun configurarListenerProgreso(ttsInstance: TextToSpeech) {
        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.i(tag, "TTS onStart locución: utteranceId='$utteranceId'")
                listener?.onInicio()
            }

            override fun onDone(utteranceId: String?) {
                Log.i(tag, "TTS onDone locución: utteranceId='$utteranceId'")
                listener?.onFin()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(tag, "TTS onError: utteranceId='$utteranceId'")
                listener?.onError("Error durante la síntesis de voz.")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(tag, "TTS onError con código $errorCode: utteranceId='$utteranceId'")
                listener?.onError("Error de síntesis de voz (código $errorCode).")
            }
        })
    }

    override fun hablar(texto: String, utteranceId: String) {
        Log.i(tag, "Solicitud hablar(id='$utteranceId'): \"$texto\"")
        if (!inicializado) {
            Log.w(tag, "TTS no está inicializado todavía; encolando petición...")
            peticionPendiente = Pair(texto, utteranceId)
            inicializar()
            return
        }

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val res = tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        Log.i(tag, "tts.speak() retorno: $res (0=SUCCESS, -1=ERROR)")
    }

    override fun detener() {
        Log.i(tag, "tts.detener() invocado")
        peticionPendiente = null
        tts?.stop()
    }

    override fun liberar() {
        Log.i(tag, "tts.liberar() invocado")
        peticionPendiente = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        inicializado = false
    }
}
