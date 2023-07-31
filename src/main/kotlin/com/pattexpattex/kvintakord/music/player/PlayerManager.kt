package com.pattexpattex.kvintakord.music.player

import com.pattexpattex.kvintakord.app.ObjectPropertyWrapper
import com.pattexpattex.kvintakord.app.views.DefaultView
import com.pattexpattex.kvintakord.music.audio.AudioDispatcher
import com.pattexpattex.kvintakord.music.spotify.SpotifyAudioSourceManager
import com.pattexpattex.kvintakord.music.spotify.SpotifyApiManager
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import tornadofx.Controller
import tornadofx.runLater
import java.util.concurrent.TimeUnit

class PlayerManager : Controller() {
    val audioPlayerManager = DefaultAudioPlayerManager()
    val queueManager = QueueManager(this)

    init {
        audioPlayerManager.registerSourceManager(SpotifyAudioSourceManager(find<SpotifyApiManager>(), YoutubeAudioSourceManager()))
        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
        AudioSourceManagers.registerLocalSource(audioPlayerManager)
        audioPlayerManager.configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_BE
    }

    val audioPlayer = AudioPlayerAdapter.wrap(audioPlayerManager.createPlayer())
    private val format = audioPlayerManager.configuration.outputFormat
    private val stream = AudioPlayerInputStream.createStream(audioPlayer, format, 0, true)
    val audioDispatcher = AudioDispatcher(config.string("mixer", ""), stream)

    init {
        loadConfiguration()
        audioPlayer.addListener(queueManager)
        audioDispatcher.addListener { saveConfiguration("mixer", it.getMixer()?.mixerInfo?.name) }
        Executors.scheduledExecutor.scheduleAtFixedRate({
            runLater { audioPlayer.playingTrack?.updatePosition() }
        }, 0, 1000, TimeUnit.MILLISECONDS)
    }

    private fun loadConfiguration() {
        with(config) {
            audioPlayer.volume = int("vol", 100)
            audioPlayer.isPaused = boolean("pause", false)
            queueManager.loop = LoopMode.fromString(string("loop", "off"))
            queueManager.shuffle = ShuffleMode.fromBoolean(boolean("shuffle", false))
        }

        audioPlayer.volumeProperty.addListener { _, _, vol -> saveConfiguration("vol", vol) }
        audioPlayer.isPausedProperty.addListener { _, _, pause -> saveConfiguration("pause", pause) }
        queueManager.loopProperty.addListener { _, _, loop -> saveConfiguration("loop", loop) }
        queueManager.shuffleProperty.addListener { _, _, shuffle -> saveConfiguration("shuffle", shuffle) }
    }

    private fun <T> saveConfiguration(key: String, value: T) {
        with(config) {
            set(key to value)
            save()
        }
    }

    fun search(query: String, source: String? = null): CompletableFuture<Pair<String, List<AudioTrack>>> {
        val future = CompletableFuture<Pair<String, List<AudioTrack>>>()
        val formattedQuery = formatQuery(query, source)

        if (formattedQuery.isEmpty()) {
            future.completeExceptionally(IllegalArgumentException())
            return future
        }

        manager.loadItem(formattedQuery, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                future.complete(query to listOf(TrackMetadata.buildFor(track)))
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                future.complete(playlist.name to playlist.tracks.map(TrackMetadata::buildFor))
            }

            override fun noMatches() {
                future.completeExceptionally(NullPointerException("No results for $query"))
            }

            override fun loadFailed(exception: FriendlyException) {
                future.completeExceptionally(exception)
            }
        })

        return future
    }

    fun playTrack(track: AudioTrack, noInterrupt: Boolean = true) = player.startTrack(track, noInterrupt)

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
        audioDispatcher.close()
    }

    private fun formatQuery(query: String, source: String?): String {
        return if (URI_PATTERN.matcher(query).matches()) {
            query
        } else {
            "${searchableSources.getOrDefault(source, "ytsearch:")}$query"
        }
    }

    companion object {
        private val URI_PATTERN = Pattern.compile("((\\w+://)[-a-zA-Z0-9:@;?&=/%+.*!'(),\$_{}^~\\[\\]`#|]+)")
    }
}