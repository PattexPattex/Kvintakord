package com.pattexpattex.kvintakord.music.player

import com.pattexpattex.kvintakord.music.adapter.AudioPlayerAdapter
import com.pattexpattex.kvintakord.music.audio.AudioDispatcher
import com.pattexpattex.kvintakord.music.audio.MixerManager
import com.pattexpattex.kvintakord.music.spotify.SpotifyApiManager
import com.pattexpattex.kvintakord.music.spotify.SpotifyAudioSourceManager
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import tornadofx.Controller
import tornadofx.onChange
import tornadofx.runLater
import java.util.concurrent.TimeUnit

class PlayerManager : Controller() {
    val audioPlayerManager = DefaultAudioPlayerManager()
    private val queueManager by inject<QueueManager>()
    private val mixerManager by inject<MixerManager>()

    init {
        audioPlayerManager.registerSourceManager(SpotifyAudioSourceManager(find<SpotifyApiManager>(), YoutubeAudioSourceManager()))
        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
        AudioSourceManagers.registerLocalSource(audioPlayerManager)
        audioPlayerManager.configuration.outputFormat = OUTPUT_FORMAT
    }

    val audioPlayer = AudioPlayerAdapter.wrap(audioPlayerManager.createPlayer())
    private val format = audioPlayerManager.configuration.outputFormat
    private val stream = AudioPlayerInputStream.createStream(audioPlayer, format, 0, true)
    private val audioDispatcher: AudioDispatcher = AudioDispatcher(mixerManager, stream)

    init {
        loadSavedState()
        registerStateListeners()

        Executors.scheduledExecutor.scheduleAtFixedRate({
            runLater { audioPlayer.playingTrack?.updatePosition() }
        }, 0, 1000, TimeUnit.MILLISECONDS)

        audioPlayer.addListener(queueManager.getListener())
    }

    private fun loadSavedState() {
        with(config) {
            audioPlayer.volume = int("vol", 100)
            audioPlayer.isPaused = boolean("pause", false)
            queueManager.loop = LoopMode.fromString(string("loop", "off"))
            queueManager.shuffle = ShuffleMode.fromBoolean(boolean("shuffle", false))
        }
    }

    private fun registerStateListeners() {
        audioPlayer.volumeProperty.onChange { save("vol", it) }
        audioPlayer.isPausedProperty.onChange { save("pause", it) }
        queueManager.loopProperty.onChange { save("loop", it) }
        queueManager.shuffleProperty.onChange { save("shuffle", it) }
    }

    private fun <T> save(key: String, value: T) {
        with(config) {
            set(key to value)
            save()
        }
    }

    fun togglePaused(): Boolean {
        audioPlayer.isPaused = !audioPlayer.isPaused
        return audioPlayer.isPaused
    }

    fun stop() {
        audioPlayer.stopTrack()
        queueManager.stop()
        audioPlayer.isPaused = true
    }

    fun destroy() {
        audioPlayer.destroy()
        audioDispatcher.destroy()
    }

    companion object {
        val OUTPUT_FORMAT = StandardAudioDataFormats.COMMON_PCM_S16_BE
    }
}