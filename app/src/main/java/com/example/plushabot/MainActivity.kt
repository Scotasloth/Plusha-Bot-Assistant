package com.example.plushabot

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.GoogleApiAvailability
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech  // Declare the TextToSpeech object
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var startButton: Button
    private lateinit var textView: TextView

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1  // Declare the constant here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        startButton = findViewById(R.id.startButton)
        textView = findViewById(R.id.textView)

        // Check if Google Play Services are available
        if (isGooglePlayServicesAvailable(this)) {
            // Request permission if necessary
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                initializeSpeechRecognition()  // Permission is already granted, initialize immediately
            }

            // Initialize the speech recognizer and TextToSpeech engine
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts.language = Locale.US
                }
            }

            // Set up listener for speech recognition results
            val listener = object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.get(0) ?: "No speech detected"
                    textView.text = spokenText
                    processCommand(spokenText)
                }

                override fun onError(error: Int) {
                    handleSpeechError(error)
                }

                // Implement other methods as needed
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
        } else {
            Toast.makeText(applicationContext, "Google Play Services is not available", Toast.LENGTH_LONG).show()
        }
    }

    private fun startListening() {
        if (isNetworkAvailable()) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Request offline recognition if possible
            }

            try {
                Log.d("MainActivity", "Starting speech recognition")
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext, "Network is not available. Please check your connection.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processCommand(command: String) {
        Log.d("MainActivity", "Processing command: $command")
        if (command.contains("weather", ignoreCase = true)) {
            textView.text = getString(R.string.weather_fetching)
            speak("The weather is sunny today with 25°C.")
        } else if (command.contains("joke", ignoreCase = true)) {
            textView.text = getString(R.string.joke_fetching)
            speak("Why don't skeletons fight each other? They don't have the guts.")
        } else {
            speak("Sorry, I didn't understand that.")
        }
    }

    private fun initializeSpeechRecognition() {
        // Ensure the SpeechRecognizer is initialized properly
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.get(0) ?: "No speech detected"
                textView.text = spokenText
                processCommand(spokenText)
            }

            override fun onError(error: Int) {
                handleSpeechError(error)
            }

            // Implement other methods as necessary
            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
        })
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun handleSpeechError(error: Int) {
        when (error) {
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                Log.e("SpeechRecognizer", "Network timeout error")
                Toast.makeText(applicationContext, "Network timeout error. Please check your connection.", Toast.LENGTH_SHORT).show()
            }
            SpeechRecognizer.ERROR_NETWORK -> {
                Log.e("SpeechRecognizer", "Network error. No connection available.")
                Toast.makeText(applicationContext, "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show()
            }
            SpeechRecognizer.ERROR_NO_MATCH -> {
                Toast.makeText(applicationContext, "No speech matched. Please try again.", Toast.LENGTH_SHORT).show()
            }
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                Toast.makeText(applicationContext, "Speech timeout. Please speak again.", Toast.LENGTH_SHORT).show()
            }
            SpeechRecognizer.ERROR_CLIENT -> {
                Toast.makeText(applicationContext, "Client error. Please check your app settings.", Toast.LENGTH_SHORT).show()
            }
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                Toast.makeText(applicationContext, "Insufficient permissions to record audio. Please grant the necessary permissions.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(applicationContext, "Speech recognition error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Check if the network is available
    private fun isNetworkAvailable(): Boolean {
        // Get the ConnectivityManager system service
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        // Check if the cast was successful and network capabilities are available
        val activeNetwork = connectivityManager?.activeNetwork
        val networkCapabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)

        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Function to check if Google Play Services are available
    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return result == com.google.android.gms.common.ConnectionResult.SUCCESS
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, initialize speech recognition
                    initializeSpeechRecognition()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}