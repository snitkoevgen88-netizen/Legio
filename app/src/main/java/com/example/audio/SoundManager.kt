package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural low-latency Audio Synthesizer for retro Roman sound effects:
 * War horn (Cornu), Gladius clash, Marching drums, Loot coins, Victory fanfare.
 */
object SoundManager {
    var isSoundEnabled: Boolean = true
    var isHapticEnabled: Boolean = true

    private val audioScope = CoroutineScope(Dispatchers.Default)

    fun playWarHorn() {
        if (!isSoundEnabled) return
        audioScope.launch {
            try {
                // Roman Cornu sound: Brass-like dual tone rising
                val sampleRate = 22050
                val durationMs = 600
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val freq1 = 220.0 + (if (progress > 0.4) 110.0 else 0.0) // A3 -> E4
                    val freq2 = freq1 * 1.5 // 5th harmonic
                    val env = when {
                        progress < 0.1 -> progress / 0.1
                        progress > 0.8 -> (1.0 - progress) / 0.2
                        else -> 1.0
                    }
                    val sample = (sin(2.0 * Math.PI * freq1 * t) * 0.7 + sin(2.0 * Math.PI * freq2 * t) * 0.3) * env
                    buffer[i] = (sample * Short.MAX_VALUE * 0.7).toInt().toShort()
                }
                playRawBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                Log.d("SoundManager", "Audio synthesis error: ${e.message}")
            }
        }
    }

    fun playSwordClash() {
        if (!isSoundEnabled) return
        audioScope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 250
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val noise = (Math.random() * 2.0 - 1.0) * 0.4
                    val metallicRing = sin(2.0 * Math.PI * 1840.0 * t) * 0.6
                    val env = (1.0 - progress) * (1.0 - progress)
                    val sample = (noise + metallicRing) * env
                    buffer[i] = (sample * Short.MAX_VALUE * 0.6).toInt().toShort()
                }
                playRawBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                Log.d("SoundManager", "Audio synthesis error: ${e.message}")
            }
        }
    }

    fun playCoins() {
        if (!isSoundEnabled) return
        audioScope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 350
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val tone1 = sin(2.0 * Math.PI * 2637.0 * t) // E7
                    val tone2 = if (progress > 0.3) sin(2.0 * Math.PI * 3135.0 * t) else 0.0 // G7
                    val env = (1.0 - progress)
                    val sample = (tone1 * 0.5 + tone2 * 0.5) * env
                    buffer[i] = (sample * Short.MAX_VALUE * 0.5).toInt().toShort()
                }
                playRawBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                Log.d("SoundManager", "Audio synthesis error: ${e.message}")
            }
        }
    }

    fun playDrumBeat() {
        if (!isSoundEnabled) return
        audioScope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 300
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val freq = 120.0 * (1.0 - progress * 0.5)
                    val tone = sin(2.0 * Math.PI * freq * t)
                    val noise = (Math.random() * 2.0 - 1.0) * 0.15
                    val env = (1.0 - progress) * (1.0 - progress)
                    val sample = (tone + noise) * env
                    buffer[i] = (sample * Short.MAX_VALUE * 0.8).toInt().toShort()
                }
                playRawBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                Log.d("SoundManager", "Audio synthesis error: ${e.message}")
            }
        }
    }

    fun playTriumphFanfare() {
        if (!isSoundEnabled) return
        audioScope.launch {
            try {
                val sampleRate = 22050
                val notes = listOf(261.63, 329.63, 392.00, 523.25) // C4, E4, G4, C5
                val noteDurationMs = 150
                val totalSamples = (sampleRate * (noteDurationMs * 4 / 1000.0)).toInt()
                val buffer = ShortArray(totalSamples)

                var sampleIndex = 0
                for (note in notes) {
                    val noteSamples = (sampleRate * (noteDurationMs / 1000.0)).toInt()
                    for (i in 0 until noteSamples) {
                        if (sampleIndex >= totalSamples) break
                        val t = i.toDouble() / sampleRate
                        val progress = i.toDouble() / noteSamples
                        val env = when {
                            progress < 0.1 -> progress / 0.1
                            progress > 0.8 -> (1.0 - progress) / 0.2
                            else -> 1.0
                        }
                        val sample = (sin(2.0 * Math.PI * note * t) * 0.7 + sin(2.0 * Math.PI * (note * 2) * t) * 0.3) * env
                        buffer[sampleIndex++] = (sample * Short.MAX_VALUE * 0.6).toInt().toShort()
                    }
                }
                playRawBuffer(buffer, sampleRate)
            } catch (e: Exception) {
                Log.d("SoundManager", "Audio synthesis error: ${e.message}")
            }
        }
    }

    fun playVictoryFanfare() = playTriumphFanfare()
    fun playGladiusClash() = playSwordClash()
    fun playMarchDrums() = playDrumBeat()

    private fun playRawBuffer(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.setNotificationMarkerPosition(buffer.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack) {
                track.release()
            }
            override fun onPeriodicNotification(track: AudioTrack) {}
        })
    }
}
