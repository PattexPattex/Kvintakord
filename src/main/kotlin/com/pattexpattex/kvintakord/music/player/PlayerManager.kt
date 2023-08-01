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
        audioPlayerManager.configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_BE
    }

    val audioPlayer = AudioPlayerAdapter.wrap(audioPlayerManager.createPlayer())
    private val format = audioPlayerManager.configuration.outputFormat
    private val stream = AudioPlayerInputStream.createStream(audioPlayer, format, 0, true)
    val audioDispatcher: AudioDispatcher

    init {
        loadConfiguration()

        mixerManager.setup(stream.format)
        audioDispatcher = AudioDispatcher(mixerManager, stream)

        Executors.scheduledExecutor.scheduleAtFixedRate({
            runLater { audioPlayer.playingTrack?.updatePosition() }
        }, 0, 1000, TimeUnit.MILLISECONDS)

        audioPlayer.addListener(queueManager.getListener())
    }

    private fun loadConfiguration() {
        with(config) {
            audioPlayer.volume = int("vol", 100)
            audioPlayer.isPaused = boolean("pause", false)
            queueManager.loop = LoopMode.fromString(string("loop", "off"))
            queueManager.shuffle = ShuffleMode.fromBoolean(boolean("shuffle", false))
        }

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

    /*fun search(query: String, source: String? = null): CompletableFuture<Pair<String, List<AudioTrackAdapter>>> {
        val future = CompletableFuture<Pair<String, List<AudioTrackAdapter>>>()
        val formattedQuery = formatQuery(query, source)

        if (formattedQuery.isEmpty()) {
            future.completeExceptionally(IllegalArgumentException())
            return future
        }

        audioPlayerManager.loadItem(formattedQuery, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                future.complete(query to listOf(AudioTrackAdapter.wrap(TrackMetadata.buildFor(track))!!))
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                future.complete(playlist.name to playlist.tracks.map(TrackMetadata::buildFor).mapNotNull(AudioTrackAdapter::wrap))
            }

            override fun noMatches() {
                future.completeExceptionally(NullPointerException("No results for $query"))
            }

            override fun loadFailed(exception: FriendlyException) {
                future.completeExceptionally(exception)
            }
        })

        return future
    }*/

    fun togglePaused(): Boolean {
        audioPlayer.isPaused = !audioPlayer.isPaused
        return audioPlayer.isPaused
    }

    fun stop() {
        audioPlayer.stopTrack()
        queueManager.stop()
        //dispatcher.stop()
        audioPlayer.isPaused = true
    }

    fun close() {
        audioPlayer.destroy()
        audioDispatcher.destroy()
    }
}