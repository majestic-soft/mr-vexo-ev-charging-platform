package com.example.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Enterprise Audio & Notification Sound Manager for ULTIMATE EV Admin
 * Provides synthesized notification chimes, alert beeps, security alarms,
 * and standard Android ringtone/tone generator fallbacks.
 */
object SoundManager {

  private const val TAG = "SoundManager"
  private val soundScope = CoroutineScope(Dispatchers.Default)

  private var toneGenerator: ToneGenerator? = null

  init {
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
    } catch (e: Exception) {
      Log.w(TAG, "ToneGenerator initialization fallback: ${e.message}")
    }
  }

  /**
   * Plays the primary notification sound based on configured style
   */
  fun playNotificationSound(context: Context?, style: String = "High-Tech Chime") {
    soundScope.launch {
      try {
        when (style) {
          "Alert Beep" -> playDualBeep()
          "Subtle Pulse" -> playSubtlePulse()
          "Sonic Alarm" -> playAlarmChime()
          else -> playHarmonicChime() // Default "High-Tech Chime"
        }
      } catch (e: Exception) {
        // Fallback to system notification tone
        playSystemDefaultNotification(context)
      }
    }
  }

  /**
   * Plays positive confirmation chime for approvals, settlements & saves
   */
  fun playSuccessSound() {
    soundScope.launch {
      try {
        playAscendingVictoryChime()
      } catch (e: Exception) {
        try {
          toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (ignored: Exception) {}
      }
    }
  }

  /**
   * Plays alert sound for disputes, ticket escalations, or station outages
   */
  fun playWarningAlert() {
    soundScope.launch {
      try {
        playWarningDoublePulse()
      } catch (e: Exception) {
        try {
          toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        } catch (ignored: Exception) {}
      }
    }
  }

  /**
   * Plays cyber security alarm for unauthorized access or tamper attempts
   */
  fun playSecurityAlarmSound() {
    soundScope.launch {
      try {
        playCyberSecurityAlarm()
      } catch (e: Exception) {
        try {
          toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 350)
        } catch (ignored: Exception) {}
      }
    }
  }

  /**
   * Plays subtle acoustic feedback for PIN keypad / unlock
   */
  fun playKeypadClick() {
    soundScope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
      } catch (ignored: Exception) {}
    }
  }

  /**
   * Plays futuristic unlock chime
   */
  fun playUnlockChime() {
    soundScope.launch {
      try {
        synthesizeSineTones(
          listOf(
            ToneData(freq = 523.25, durationMs = 60, amplitude = 0.5f), // C5
            ToneData(freq = 659.25, durationMs = 60, amplitude = 0.6f), // E5
            ToneData(freq = 783.99, durationMs = 90, amplitude = 0.7f), // G5
            ToneData(freq = 1046.50, durationMs = 150, amplitude = 0.8f) // C6
          )
        )
      } catch (e: Exception) {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
      }
    }
  }

  // --- Synthesized PCM Audio Chimes (Self-contained, 100% reliable) ---

  private fun playHarmonicChime() {
    // 2-tone bright corporate notification chime (G5 -> C6)
    synthesizeSineTones(
      listOf(
        ToneData(freq = 783.99, durationMs = 70, amplitude = 0.65f),  // G5
        ToneData(freq = 1046.50, durationMs = 220, amplitude = 0.85f) // C6
      )
    )
  }

  private fun playDualBeep() {
    // Crisp tech double-beep
    synthesizeSineTones(
      listOf(
        ToneData(freq = 1200.0, durationMs = 45, amplitude = 0.7f),
        ToneData(freq = 0.0, durationMs = 30, amplitude = 0.0f), // silence gap
        ToneData(freq = 1200.0, durationMs = 55, amplitude = 0.75f)
      )
    )
  }

  private fun playSubtlePulse() {
    // Warm low-frequency soft pulse
    synthesizeSineTones(
      listOf(
        ToneData(freq = 440.0, durationMs = 120, amplitude = 0.5f)
      )
    )
  }

  private fun playAlarmChime() {
    // Alert chime (High-Low-High)
    synthesizeSineTones(
      listOf(
        ToneData(freq = 880.0, durationMs = 80, amplitude = 0.8f),
        ToneData(freq = 660.0, durationMs = 80, amplitude = 0.7f),
        ToneData(freq = 880.0, durationMs = 140, amplitude = 0.85f)
      )
    )
  }

  private fun playAscendingVictoryChime() {
    // Major chord arpeggio for positive feedback (C5 -> E5 -> G5)
    synthesizeSineTones(
      listOf(
        ToneData(freq = 523.25, durationMs = 50, amplitude = 0.6f),
        ToneData(freq = 659.25, durationMs = 50, amplitude = 0.65f),
        ToneData(freq = 783.99, durationMs = 140, amplitude = 0.8f)
      )
    )
  }

  private fun playWarningDoublePulse() {
    synthesizeSineTones(
      listOf(
        ToneData(freq = 493.88, durationMs = 90, amplitude = 0.75f), // B4
        ToneData(freq = 0.0, durationMs = 40, amplitude = 0.0f),
        ToneData(freq = 493.88, durationMs = 120, amplitude = 0.8f)
      )
    )
  }

  private fun playCyberSecurityAlarm() {
    // Threat alarm frequency sweep
    synthesizeSineTones(
      listOf(
        ToneData(freq = 987.77, durationMs = 80, amplitude = 0.85f),
        ToneData(freq = 1318.51, durationMs = 80, amplitude = 0.9f),
        ToneData(freq = 987.77, durationMs = 80, amplitude = 0.85f),
        ToneData(freq = 1318.51, durationMs = 120, amplitude = 0.95f)
      )
    )
  }

  private fun playSystemDefaultNotification(context: Context?) {
    if (context == null) return
    try {
      val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
      ringtone?.play()
    } catch (e: Exception) {
      Log.e(TAG, "Failed playing default ringtone: ${e.message}")
    }
  }

  private data class ToneData(val freq: Double, val durationMs: Int, val amplitude: Float)

  /**
   * Generates pure 16-bit PCM sinusoidal tones on-the-fly and plays via AudioTrack.
   * Runs natively on all Android versions without external audio assets.
   */
  private fun synthesizeSineTones(tones: List<ToneData>) {
    val sampleRate = 44100
    val totalSamples = tones.sumOf { (it.durationMs * sampleRate) / 1000 }
    if (totalSamples <= 0) return

    val pcmData = ShortArray(totalSamples)
    var sampleIndex = 0

    tones.forEach { tone ->
      val toneSamples = (tone.durationMs * sampleRate) / 1000
      val angularFreq = 2.0 * Math.PI * tone.freq / sampleRate
      val maxAmplitude = (Short.MAX_VALUE * tone.amplitude).toInt()

      for (i in 0 until toneSamples) {
        if (sampleIndex >= totalSamples) break
        if (tone.freq <= 0.0) {
          pcmData[sampleIndex++] = 0
        } else {
          // Add smooth envelope fade-in/fade-out to eliminate acoustic click
          val fadeWindow = (toneSamples * 0.1).toInt().coerceAtLeast(1)
          val envelope = when {
            i < fadeWindow -> (i.toDouble() / fadeWindow)
            i > toneSamples - fadeWindow -> ((toneSamples - i).toDouble() / fadeWindow)
            else -> 1.0
          }
          val sampleValue = (sin(i * angularFreq) * maxAmplitude * envelope).toInt()
          pcmData[sampleIndex++] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
      }
    }

    try {
      val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      ).coerceAtLeast(totalSamples * 2)

      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
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
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      audioTrack.write(pcmData, 0, pcmData.size)
      audioTrack.play()
    } catch (e: Exception) {
      Log.w(TAG, "AudioTrack synthesis exception: ${e.message}")
    }
  }
}
