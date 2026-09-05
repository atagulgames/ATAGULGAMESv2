package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object HarmonicAudioEngine {

    var isSoundEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                pauseBgm()
            } else {
                resumeBgm()
            }
        }

    private var appContext: Context? = null
    private var bgmPlayer: MediaPlayer? = null
    private var activeSfxPlayer: MediaPlayer? = null
    private var savedBgmPosition: Int = 0
    private val audioScope = CoroutineScope(Dispatchers.Default)

    private var currentBgmVolume = 0.70f
    private const val NORMAL_BGM_VOLUME = 0.70f
    private const val DUCKED_BGM_VOLUME = 0.15f
    private var duckJob: Job? = null

    private val pentatonicScale = floatArrayOf(
        261.63f, // C4
        293.66f, // D4
        329.63f, // E4
        392.00f, // G4
        440.00f, // A4
        523.25f, // C5
        587.33f, // D5
        659.25f, // E5
        783.99f, // G5
        880.00f  // A5
    )

    fun init(context: Context) {
        appContext = context.applicationContext
        startBgm()
    }

    /**
     * Starts continuous ambient background music.
     */
    fun startBgm() {
        if (!isSoundEnabled) return
        val ctx = appContext ?: return
        try {
            val resId = ctx.resources.getIdentifier("bgm_game_music", "raw", ctx.packageName)
            if (resId != 0) {
                if (bgmPlayer == null) {
                    bgmPlayer = MediaPlayer.create(ctx, resId)?.apply {
                        isLooping = true
                        currentBgmVolume = NORMAL_BGM_VOLUME
                        setVolume(currentBgmVolume, currentBgmVolume)
                        if (savedBgmPosition > 0) {
                            seekTo(savedBgmPosition)
                        }
                        start()
                    }
                } else if (bgmPlayer?.isPlaying == false) {
                    if (savedBgmPosition > 0) {
                        try { bgmPlayer?.seekTo(savedBgmPosition) } catch (_: Exception) {}
                    }
                    bgmPlayer?.start()
                }
            }
        } catch (_: Exception) {
            // Audio setup fallback
        }
    }

    fun pauseBgm() {
        try {
            if (bgmPlayer?.isPlaying == true) {
                savedBgmPosition = bgmPlayer?.currentPosition ?: 0
                bgmPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resumeBgm() {
        if (!isSoundEnabled) return
        try {
            if (bgmPlayer == null) {
                startBgm()
            } else if (bgmPlayer?.isPlaying == false) {
                applyBgmVolume(NORMAL_BGM_VOLUME)
                bgmPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    /**
     * Smoothly ducks the background music volume (e.g. from 0.70f to 0.15f)
     * so that foreground sound effects (win fanfare, next level voice) stand out with crystal clarity.
     */
    fun duckBgm(targetVolume: Float = DUCKED_BGM_VOLUME, durationMs: Long = 200L) {
        if (!isSoundEnabled || bgmPlayer?.isPlaying != true) return
        duckJob?.cancel()
        duckJob = audioScope.launch {
            val steps = 10
            val startVol = currentBgmVolume
            val stepDelay = (durationMs / steps).coerceAtLeast(10L)
            for (i in 1..steps) {
                val vol = startVol + (targetVolume - startVol) * (i.toFloat() / steps)
                applyBgmVolume(vol)
                delay(stepDelay)
            }
            applyBgmVolume(targetVolume)
        }
    }

    /**
     * Smoothly restores the background music volume back to normal.
     */
    fun restoreBgm(targetVolume: Float = NORMAL_BGM_VOLUME, durationMs: Long = 350L) {
        if (!isSoundEnabled || bgmPlayer?.isPlaying != true) return
        duckJob?.cancel()
        duckJob = audioScope.launch {
            val steps = 15
            val startVol = currentBgmVolume
            val stepDelay = (durationMs / steps).coerceAtLeast(10L)
            for (i in 1..steps) {
                val vol = startVol + (targetVolume - startVol) * (i.toFloat() / steps)
                applyBgmVolume(vol)
                delay(stepDelay)
            }
            applyBgmVolume(targetVolume)
        }
    }

    private fun applyBgmVolume(volume: Float) {
        currentBgmVolume = volume.coerceIn(0f, 1f)
        try {
            bgmPlayer?.setVolume(currentBgmVolume, currentBgmVolume)
        } catch (_: Exception) {}
    }

    /**
     * Plays an intervening SFX with intelligent audio ducking:
     * 1. Automatically ducks background music down.
     * 2. Plays the requested SFX.
     * 3. When SFX finishes, smoothly restores background music to full volume.
     */
    private fun playInterveningSfx(
        resName: String,
        sfxDurationMs: Long = 1200L,
        fallbackTone: () -> Unit
    ) {
        if (!isSoundEnabled) return
        val ctx = appContext ?: return

        // Duck background music for user requested ducking behavior
        duckBgm(DUCKED_BGM_VOLUME, durationMs = 150L)

        try {
            val resId = ctx.resources.getIdentifier(resName, "raw", ctx.packageName)
            if (resId != 0) {
                try {
                    activeSfxPlayer?.stop()
                    activeSfxPlayer?.release()
                } catch (_: Exception) {}

                val player = MediaPlayer.create(ctx, resId)
                if (player != null) {
                    activeSfxPlayer = player
                    player.setVolume(0.95f, 0.95f)
                    player.setOnCompletionListener { mp ->
                        try {
                            mp.release()
                        } catch (_: Exception) {}
                        activeSfxPlayer = null
                        // SFX finished: Restore background music volume back to normal
                        restoreBgm(NORMAL_BGM_VOLUME)
                    }
                    player.start()
                    return
                }
            }
        } catch (_: Exception) {}

        // Fallback procedural sound + scheduled restore
        fallbackTone()
        audioScope.launch {
            delay(sfxDurationMs)
            restoreBgm(NORMAL_BGM_VOLUME)
        }
    }

    /**
     * Plays Next Level voice and sound effect cleanly.
     * Background music volume is ducked while voice plays, then restored.
     */
    fun playNextLevel() {
        playInterveningSfx("sfx_next_level", sfxDurationMs = 1400L) {
            audioScope.launch {
                playSynthTone(523.25f, 120, 0.60f)
                delay(90)
                playSynthTone(659.25f, 120, 0.65f)
                delay(90)
                playSynthTone(783.99f, 260, 0.70f)
            }
        }
    }

    /**
     * Plays Win / Victory audio fanfare.
     * Background music volume is ducked, fanfare plays, then BGM smoothly returns.
     */
    fun playVictoryCascade() {
        playInterveningSfx("sfx_win", sfxDurationMs = 2200L) {
            audioScope.launch {
                playSynthTone(392.00f, 110, 0.55f)
                delay(90)
                playSynthTone(523.25f, 110, 0.60f)
                delay(90)
                playSynthTone(659.25f, 130, 0.65f)
                delay(110)
                playSynthTone(783.99f, 180, 0.70f)
                delay(120)
                playSynthTone(1046.50f, 400, 0.75f)
            }
        }
    }

    /**
     * Plays Collision Error / Red Line buzz sound.
     */
    fun playCollisionBuzz() {
        playInterveningSfx("sfx_error", sfxDurationMs = 600L) {
            audioScope.launch {
                playSynthTone(130.81f, 160, 0.55f)
            }
        }
    }

    fun playBrokenRedLine() {
        playInterveningSfx("sfx_broken_line", sfxDurationMs = 700L) {
            audioScope.launch {
                playSynthTone(349.23f, 90, 0.55f)
                delay(70)
                playSynthTone(174.61f, 130, 0.50f)
            }
        }
    }

    fun playDrillBeam() {
        playBrokenRedLine()
    }

    /**
     * Plays Atagul Games Intro fanfare chime.
     */
    fun playIntroJingle() {
        if (!isSoundEnabled) return
        audioScope.launch {
            // Elegant chord progression for intro video sequence
            playSynthTone(261.63f, 200, 0.50f) // C4
            delay(140)
            playSynthTone(329.63f, 220, 0.55f) // E4
            delay(150)
            playSynthTone(392.00f, 260, 0.60f) // G4
            delay(160)
            playSynthTone(523.25f, 700, 0.70f) // C5
        }
    }

    /**
     * Plays melodic harmonic tone when a node is connected during drawing.
     */
    fun playNodeTone(sequenceIndex: Int) {
        if (!isSoundEnabled) return
        val freq = pentatonicScale[sequenceIndex % pentatonicScale.size]
        audioScope.launch {
            playSynthTone(freq, durationMs = 120, volume = 0.55f)
        }
    }

    private fun playSynthTone(freq: Float, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = 1.0 - (i.toDouble() / numSamples)
            val sample = sin(2 * PI * freq * t) * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playPcm(buffer, sampleRate)
    }

    private fun playPcm(buffer: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
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

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (_: Exception) {}
                }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
        } catch (_: Exception) {}
    }
}
