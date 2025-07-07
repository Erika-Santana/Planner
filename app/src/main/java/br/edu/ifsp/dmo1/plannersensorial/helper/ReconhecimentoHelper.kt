package br.edu.ifsp.dmo1.plannersensorial.helper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class ReconhecimentoHelper(
    private val context: Context,
    private val callback: Callback
) {

    interface Callback {
        fun onReconhecimentoFinalizado(texto: String?)
        fun onErroReconhecimento(mensagem: String)
    }

    private val recognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    fun iniciarReconhecimento(idioma: String = "pt-BR") {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, idioma)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale agora...")
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val texto = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                callback.onReconhecimentoFinalizado(texto)
            }

            override fun onError(error: Int) {
                callback.onErroReconhecimento("Erro $error")
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
    }

    fun liberar() {
        recognizer.destroy()
    }
}
