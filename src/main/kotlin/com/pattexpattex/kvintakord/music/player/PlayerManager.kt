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
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.*
import java.lang.NullPointerException
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

class PlayerManager : Controller() {
    private val manager = DefaultAudioPlayerManager()
    val musicManager = MusicManager(this)
    init {
        manager.registerSourceManager(SpotifyAudioSourceManager(find<SpotifyApiManager>(), YoutubeAudioSourceManager()))
        AudioSourceManagers.registerRemoteSources(manager)
        AudioSourceManagers.registerLocalSource(manager)
        manager.configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_BE
    }

    private val player = manager.createPlayer()
    private val format = manager.configuration.outputFormat
    private val stream = AudioPlayerInputStream.createStream(player, format, 0, true)
    val audioDispatcher = AudioDispatcher(config.string("mixer", ""), stream)

    val volume = ObjectPropertyWrapper(player::getVolume, player::setVolume)
    private var _volume by volume
    val paused = ObjectPropertyWrapper(player::isPaused, player::setPaused)
    private var _paused by paused
    val searchableSources = mapOf("Youtube" to "ytsearch:", "Youtube Music" to "ytmsearch:", "Spotify" to "spsearch:", "SoundCloud" to "scsearch:")

    init {
        loadConfiguration()
        player.addListener(musicManager)
        audioDispatcher.addListener { saveConfiguration("mixer", it.getMixer()?.mixerInfo?.name) }
    }

    private fun loadConfiguration() {
        with(config) {
            _volume = int("vol", 100)
            _paused = boolean("pause", false)
            musicManager.loopMode.set(LoopMode.fromString(string("loop", "off")))
            musicManager.shuffleMode.set(ShuffleMode.fromBoolean(boolean("shuffle", false)))
        }

        volume.addListener { _, _, vol -> saveConfiguration("vol", vol) }
        paused.addListener { _, _, pause -> saveConfiguration("pause", pause) }
        musicManager.loopMode.addListener { _, _, loop -> saveConfiguration("loop", loop) }
        musicManager.shuffleMode.addListener { _, _, shuffle -> saveConfiguration("shuffle", shuffle) }
    }

    private fun <T> saveConfiguration(key: String, value: T) {
        with(config) {
            set(key to value)
            save()
        }
    }

    fun play(query: String, source: String? = null) {
        manager.loadItem(formatQuery(query, source), object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                musicManager.addToQueue(TrackMetadata.buildFor(track))
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                if (playlist.isSearchResult) {
                    musicManager.addToQueue(TrackMetadata.buildFor(playlist.selectedTrack ?: playlist.tracks[0]))
                } else {
                    musicManager.addToQueue(playlist.tracks.map(TrackMetadata::buildFor))
                }
            }

            override fun noMatches() {
                runLater { find<DefaultView>().root.apply {
                    alert(Alert.AlertType.INFORMATION, buttons = arrayOf(ButtonType.CLOSE), header = "No results for \"$query\"")
                } }

            }

            override fun loadFailed(exception: FriendlyException) {
                runLater { find<DefaultView>().root.apply {
                    alert(Alert.AlertType.WARNING, content = exception.message, buttons = arrayOf(ButtonType.CLOSE), title = "Hmmm...", header = "Hmmm...")
                } }
            }
        })
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
        _paused = !_paused
        return _paused
    }

    fun stop() {
        player.stopTrack()
        musicManager.stop()
        //dispatcher.stop()
        _paused = true
    }

    fun close() {
        player.destroy()
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