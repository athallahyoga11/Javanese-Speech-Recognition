package utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.*

class AudioRecorder(private val outputFile: File) {
    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    fun startRecording() {
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        recorder?.startRecording()
        isRecording = true

        recordingThread = Thread {
            writeAudioDataToFile()
        }.also { it.start() }
    }

    fun stopRecording() {
        isRecording = false
        Thread.sleep(200) // opsional, agar write selesai

        recorder?.stop()
        recorder?.release()
        recorder = null

        val rawFile = getTempRawFile()
        if (rawFile.exists() && rawFile.length() > 0) {
            rawToWave(rawFile, outputFile)
        } else {
            println("⚠️ temp.raw not found or empty. Skipping WAV conversion.")
        }
    }


    private fun getTempRawFile(): File = File(outputFile.parent, "temp.raw")

    private fun writeAudioDataToFile() {
        val data = ByteArray(bufferSize)
        val rawFile = getTempRawFile()
        FileOutputStream(rawFile).use { os ->
            while (isRecording) {
                val read = recorder?.read(data, 0, data.size) ?: 0
                if (read > 0) os.write(data, 0, read)
            }
        }
    }

    private fun rawToWave(rawFile: File, wavFile: File) {
        val rawData = rawFile.readBytes()
        val totalDataLen = rawData.size + 36
        val byteRate = sampleRate * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        writeInt(header, 4, totalDataLen)
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        writeInt(header, 16, 16)
        writeShort(header, 20, 1.toShort())
        writeShort(header, 22, 1.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, 2.toShort())
        writeShort(header, 34, 16.toShort())
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        writeInt(header, 40, rawData.size)

        FileOutputStream(wavFile).use { out ->
            out.write(header)
            out.write(rawData)
        }

        rawFile.delete() // Hapus temp file
    }

    private fun writeInt(header: ByteArray, offset: Int, value: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = (value shr 8 and 0xff).toByte()
        header[offset + 2] = (value shr 16 and 0xff).toByte()
        header[offset + 3] = (value shr 24 and 0xff).toByte()
    }

    private fun writeShort(header: ByteArray, offset: Int, value: Short) {
        header[offset] = (value.toInt() and 0xff).toByte()
        header[offset + 1] = (value.toInt() shr 8 and 0xff).toByte()
    }
}
