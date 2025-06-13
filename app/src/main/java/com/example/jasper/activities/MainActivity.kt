package com.example.jasper.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.jasper.R
import kotlinx.coroutines.*
import network.RetrofitClient
import network.RetrofitLibreClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import utils.AudioRecorder
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var recorder: AudioRecorder
    private lateinit var audioFile: File
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var translateBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var recordBtn: Button
    private lateinit var playBtn: Button
    private lateinit var uploadBtn: Button
    private lateinit var resultText: EditText

    private var transcribedTextJawa: String = ""
    private var translatedTextIndonesia: String = ""
    var isRecording = false
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordBtn = findViewById(R.id.btnRecord)
        playBtn = findViewById(R.id.btnPlay)
        uploadBtn = findViewById(R.id.btnUpload)
        resultText = findViewById(R.id.tvResult)
        translateBtn = findViewById(R.id.btnTranslate)
        progressBar = findViewById(R.id.progressBar)

        translateBtn.isEnabled = false
        playBtn.isEnabled = false

        audioFile = File(cacheDir, "recorded.wav")
        recorder = AudioRecorder(audioFile)

        // Tombol Rekam
        recordBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
                Toast.makeText(this, "Please allow audio recording permission first.", Toast.LENGTH_SHORT).show()
            } else {
                if (!isRecording) {
                    resultText.setText("")
                    transcribedTextJawa = ""
                    translatedTextIndonesia = ""
                    recorder.startRecording()
                    isRecording = true
                    recordBtn.text = "⏹️ Stop"
                    playBtn.isEnabled = false
                    Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
                } else {
                    recorder.stopRecording()
                    isRecording = false
                    recordBtn.text = "🎤 Rekam"
                    playBtn.isEnabled = true
                    Toast.makeText(this, "Recording stopped. Saved to:\n${audioFile.absolutePath}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Tombol Putar
        playBtn.setOnClickListener {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    start()
                }
                playBtn.text = "Pause"
                mediaPlayer?.setOnCompletionListener {
                    playBtn.text = "Play"
                    releaseMediaPlayer()
                }
            } else {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause()
                    playBtn.text = "Play"
                } else {
                    mediaPlayer?.start()
                    playBtn.text = "Pause"
                }
            }
        }

        // Tombol Upload
        uploadBtn.setOnClickListener {
            scope.launch {
                if (!audioFile.exists()) {
                    resultText.setText("No recorded audio to upload!")
                    return@launch
                }

                progressBar.visibility = ProgressBar.VISIBLE
                uploadBtn.isEnabled = false
                translateBtn.isEnabled = false
                recordBtn.isEnabled = false
                playBtn.isEnabled = false

                val reqFile = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
                val multipart = MultipartBody.Part.createFormData("file", audioFile.name, reqFile)

                try {
                    val response = RetrofitClient.apiService.transcribeAudio(multipart)
                    if (response.isSuccessful) {
                        transcribedTextJawa = response.body()?.transcription ?: ""
                        translatedTextIndonesia = ""
                        resultText.setText("$transcribedTextJawa")
                        translateBtn.isEnabled = true
                    } else {
                        resultText.setText("Failed to contact server: ${response.code()}")
                    }
                } catch (e: Exception) {
                    resultText.setText("Network error: ${e.localizedMessage}")
                } finally {
                    progressBar.visibility = ProgressBar.GONE
                    uploadBtn.isEnabled = true
                    recordBtn.isEnabled = true
                    playBtn.isEnabled = true
                }
            }
        }

        // Tombol Translate
        translateBtn.setOnClickListener {
            if (transcribedTextJawa.isEmpty()) {
                Toast.makeText(this, "No transcribed text to translate.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            scope.launch {
                progressBar.visibility = ProgressBar.VISIBLE
                translateBtn.isEnabled = false

                try {
                    val response = RetrofitLibreClient.apiService.translateText(transcribedTextJawa)
                    if (response.isSuccessful) {
                        translatedTextIndonesia = response.body()?.translatedText ?: ""

                        // Tampilkan hasil di TextView Bahasa Indonesia
                        val translatedTextView = findViewById<TextView>(R.id.tvTranslated)
                        translatedTextView.text = translatedTextIndonesia

                    } else {
                        Toast.makeText(this@MainActivity, "Failed to translate: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Translation error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    progressBar.visibility = ProgressBar.GONE
                    translateBtn.isEnabled = true
                }
            }
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        scope.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. You can record now.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied. Cannot record audio.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
