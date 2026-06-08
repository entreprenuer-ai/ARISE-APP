package com.example.core.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AriseAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var loopJob: Job? = null
    private var volumeJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: android.media.AudioFocusRequest? = null
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        Log.d(TAG, "Audio focus changed: $focusChange")
    }

    companion object {
        private const val TAG = "AriseAlarmService"
        const val ACTION_START_ALARM = "com.example.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.ACTION_STOP_ALARM"

        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        const val EXTRA_SOUND_PATH = "EXTRA_SOUND_PATH"
        const val EXTRA_SOUND_START_MS = "EXTRA_SOUND_START_MS"
        const val EXTRA_SOUND_END_MS = "EXTRA_SOUND_END_MS"
        const val EXTRA_GRADUAL_VOLUME = "EXTRA_GRADUAL_VOLUME"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AriseAlarmService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand with action: $action")

        when (action) {
            ACTION_START_ALARM -> {
                val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
                val soundPath = intent.getStringExtra(EXTRA_SOUND_PATH)
                val startSec = intent.getIntExtra(EXTRA_SOUND_START_MS, 0)
                val endSec = intent.getIntExtra(EXTRA_SOUND_END_MS, 30)
                val gradual = intent.getBooleanExtra(EXTRA_GRADUAL_VOLUME, false)

                Log.d(TAG, "Starting alarm playback - ID: $alarmId, Path: $soundPath, Range: $startSec - $endSec Secs, Crescendo: $gradual")
                startAlarmPlayback(soundPath, startSec, endSec, gradual)
            }
            ACTION_STOP_ALARM -> {
                Log.d(TAG, "Stopping alarm service playback")
                stopAlarmPlayback()
                stopSelf()
            }
            else -> {
                Log.w(TAG, "Unknown action: $action")
            }
        }

        return START_NOT_STICKY
    }

    private fun startAlarmPlayback(soundPath: String?, startSec: Int, endSec: Int, gradual: Boolean) {
        stopAlarmPlayback()
        requestFocus()

        val context = applicationContext
        val player = MediaPlayer()
        mediaPlayer = player

        try {
            // Set up audio attributes for Alarm stream
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            // Try to set data source
            val hasSoundPath = !soundPath.isNullOrEmpty()
            if (hasSoundPath) {
                try {
                    player.setDataSource(context, Uri.parse(soundPath))
                    Log.d(TAG, "Set data source to custom path: $soundPath")
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed setting local custom file content path, falling back to default alarm ringtone", ex)
                    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    player.setDataSource(context, defaultUri)
                }
            } else {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                player.setDataSource(context, defaultUri)
                Log.d(TAG, "Set data source to system default URI: $defaultUri")
            }

            player.prepare()

            // Calculate milliseconds
            val startMs = (startSec * 1000).coerceAtLeast(0)
            val endMs = (endSec * 1000).coerceAtLeast(startMs + 2000)

            player.seekTo(startMs)
            
            // Set initial volume
            if (gradual) {
                player.setVolume(0.1f, 0.1f)
                startVolumeCrescendo(player)
            } else {
                player.setVolume(1.0f, 1.0f)
            }

            player.start()
            Log.d(TAG, "MediaPlayer started successfully")

            // Monitor clip range and loop back
            loopJob = serviceScope.launch {
                while (mediaPlayer == player) {
                    try {
                        if (player.isPlaying) {
                            val currentPos = player.currentPosition
                            if (currentPos >= endMs || currentPos < startMs) {
                                Log.d(TAG, "Loop limits hit ($currentPos >= $endMs). Re-seeking to $startMs")
                                player.seekTo(startMs)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in loopJob monitor loop", e)
                    }
                    delay(250)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error seeking or playing MediaPlayer, trying notification fallback stream", e)
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val fallbackPlayer = MediaPlayer.create(context, fallbackUri)
                fallbackPlayer?.isLooping = true
                fallbackPlayer?.start()
                mediaPlayer = fallbackPlayer
            } catch (ex: Exception) {
                Log.e(TAG, "Failed notification stream fallback", ex)
            }
        }
    }

    private fun startVolumeCrescendo(player: MediaPlayer) {
        volumeJob?.cancel()
        volumeJob = serviceScope.launch {
            var volume = 0.1f
            while (volume < 1.0f && mediaPlayer == player) {
                delay(3000) // Increase volume every 3 seconds
                volume += 0.1f
                if (volume > 1.0f) volume = 1.0f
                Log.d(TAG, "Ramping up crescendo volume to: $volume")
                try {
                    player.setVolume(volume, volume)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun stopAlarmPlayback() {
        loopJob?.cancel()
        loopJob = null
        volumeJob?.cancel()
        volumeJob = null

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        }
        mediaPlayer = null
        abandonFocus()
    }

    private fun requestFocus() {
        try {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                audioFocusRequest = focusRequest
                val result = audioManager?.requestAudioFocus(focusRequest)
                Log.d(TAG, "Exclusive audio focus requested (Oreo+). Result: $result")
            } else {
                @Suppress("DEPRECATION")
                val result = audioManager?.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
                Log.d(TAG, "Exclusive audio focus requested (Pre-Oreo). Result: $result")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting exclusive audio focus", e)
        }
    }

    private fun abandonFocus() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                audioFocusRequest?.let {
                    audioManager?.abandonAudioFocusRequest(it)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(audioFocusChangeListener)
            }
            Log.d(TAG, "Audio focus abandoned successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error abandoning audio focus", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "AriseAlarmService destroyed")
        stopAlarmPlayback()
        super.onDestroy()
    }
}
