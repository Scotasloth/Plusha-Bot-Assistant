package com.example.plushabot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.plushabot.ui.theme.PlushaBotTheme
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.RecognizerIntent
import java.util.Locale
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.ActivityNotFoundException

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech  // Declare the TextToSpeech object
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var startButton: Button
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        startButton = findViewById(R.id.startButton)
        textView = findViewById(R.id.textView)

        // Initialize the speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Set up listener for speech recognition results
        val listener = object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.get(0) ?: "No speech detected"
                textView.text = spokenText
                processCommand(spokenText)
            }

            override fun onError(error: Int) {
                Toast.makeText(applicationContext, "Error occurred: $error", Toast.LENGTH_SHORT).show()
            }

            // Implement other methods (empty implementations)
            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
        }

        speechRecognizer.setRecognitionListener(listener)

        // Set up button click to start listening
        startButton.setOnClickListener {
            startListening()
        }
    }

    // Start speech recognition
    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())  // Optional: Set language to device's default locale
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")  // Optional: Prompt text
        }

        try {
            speechRecognizer.startListening(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(applicationContext, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    // Process the voice command
    private fun processCommand(command: String) {
        if (command.contains("weather", ignoreCase = true)) {
            textView.text = getString(R.string.weather_fetching)
            // Here you can fetch data from a weather API or provide a mock response
            speak("The weather is sunny today with 25°C.")
        } else if (command.contains("joke", ignoreCase = true)) {
            textView.text = getString(R.string.joke_fetching)
            // Provide a mock joke
            speak("Why don't skeletons fight each other? They don't have the guts.")
        } else {
            speak("Sorry, I didn't understand that.")
        }
    }

    // Function to speak a response
    private fun speak(text: String) {
        val tts = android.speech.tts.TextToSpeech(this) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }
}