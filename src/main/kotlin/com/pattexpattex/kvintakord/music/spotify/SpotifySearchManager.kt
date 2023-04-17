package com.pattexpattex.kvintakord.music.spotify

import com.adamratzman.spotify.models.Track
import com.pattexpattex.kvintakord.app.SLF4J
import com.pattexpattex.kvintakord.app.logTime
import com.pattexpattex.kvintakord.music.player.Executors
import com.pattexpattex.kvintakord.music.player.toCompletableFuture
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.track.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.RejectedExecutionException

class SpotifySearchManager(
    private val spotifyApiManager: SpotifyApiManager,
    private val youtubeAudioSourceManager: YoutubeAudioSourceManager
) {
    fun getSearch(manager: AudioPlayerManager, query: String): AudioItem? {
        val result = spotifyApiManager.search(query)

        if (rejected(result)) {
            return AudioReference.NO_TRACK
        }

        return with(rethrow(result)!!) {
            if (it.size == 0) {
                AudioReference.NO_TRACK
            }

            BasicAudioPlaylist("Search results for: $query", searchForBackingTracks(manager, it.items), null, true)
        }
    }

    fun getTrack(manager: AudioPlayerManager, url: String): AudioItem? {
        val result = spotifyApiManager.getTrack(url)

        if (rejected(result)) {
            return AudioReference.NO_TRACK
        }

        return with(rethrow(result) ?: return AudioReference.NO_TRACK) {
            searchForBackingTrack(manager, it)
        }
    }

    fun getPlaylist(manager: AudioPlayerManager, url: String): AudioItem? {
        val result = spotifyApiManager.getPlaylist(url)

        if (rejected(result)) {
            return AudioReference.NO_TRACK
        }

        return with(rethrow(result) ?: return AudioReference.NO_TRACK) { playlist ->
            BasicAudioPlaylist("${playlist.name} by ${playlist.owner.displayName ?: "nobody"}",
                searchForBackingTracks(manager, playlist.tracks.items.mapNotNull { it.track?.asTrack }), null, false)
        }
    }

    fun getAlbum(manager: AudioPlayerManager, url: String): AudioItem? {
        val result = spotifyApiManager.getAlbum(url)

        if (rejected(result)) {
            return AudioReference.NO_TRACK
        }

        return with(rethrow(result) ?: return AudioReference.NO_TRACK) { album ->
            BasicAudioPlaylist("${album.name} by ${album.artists[0].name}",
                searchForBackingTracks(manager, album.tracks.items.map { it.toFullTrackRestAction().toCompletableFuture() }), null, false)
        }
    }

    fun getArtistTracks(manager: AudioPlayerManager, url: String): AudioItem? {
        val artistResult = spotifyApiManager.getArtist(url)

        if (rejected(artistResult)) {
            return AudioReference.NO_TRACK
        }

        val result = spotifyApiManager.getArtistTracks(url)

        if (rejected(result)) {
            return AudioReference.NO_TRACK
        }

        return with(rethrow(artistResult) ?: return AudioReference.NO_TRACK) { artist ->
            with(rethrow(result)!!) {
                BasicAudioPlaylist("Top tracks of ${artist.name}",
                    searchForBackingTracks(manager, it), null, false)
            }
        }
    }

    private fun youtubeSearch(manager: AudioPlayerManager, reference: AudioReference): AudioItem? {
        return youtubeAudioSourceManager.loadItem(manager, reference)
    }

    private fun youtubeTrackSearch(manager: AudioPlayerManager, reference: AudioReference): YoutubeAudioTrack? {
        val item = youtubeSearch(manager, reference) ?: return null

        return if (item is AudioPlaylist) {
            (item.selectedTrack ?: item.tracks[0]) as? YoutubeAudioTrack
        } else {
            item as? YoutubeAudioTrack
        }
    }

    private fun searchForBackingTrack(manager: AudioPlayerManager, track: Track): SpotifyAudioTrack? = wrapYoutubeTrack(youtubeTrackSearch(manager, buildAudioReference(track)), track)

    @JvmName("searchForBackingTracksFromAsync")
    private fun searchForBackingTracks(manager: AudioPlayerManager, tracks: List<CompletableFuture<Track?>>): List<SpotifyAudioTrack> {
        val tasks = arrayListOf<CompletableFuture<SpotifyAudioTrack?>>()

        for (track in tracks) {
            val task = track.thenApply { youtubeTrackSearch(manager, buildAudioReference(it!!)) to it }
                .thenApply { wrapYoutubeTrack(it.first, it.second) }

            tasks.add(task)
        }

        return unwrapTracks(tasks)
    }

    private fun searchForBackingTracks(manager: AudioPlayerManager, tracks: List<Track>): List<SpotifyAudioTrack> {
        val tasks = arrayListOf<CompletableFuture<SpotifyAudioTrack?>>()

        for (track in tracks) {
            val task = CompletableFuture.supplyAsync({ youtubeTrackSearch(manager, buildAudioReference(track)) }, Executors.spotifySearchExecutor)
                .thenApply { wrapYoutubeTrack(it, track) }

            tasks.add(task)
        }

        return unwrapTracks(tasks)
    }

    private fun <T> unwrapTracks(tasks: List<CompletableFuture<T?>>): List<T> {
        runCatching { CompletableFuture.allOf(*tasks.toTypedArray()).join() }
        return tasks.dropWhile(CompletableFuture<T?>::isCompletedExceptionally).mapNotNull { runCatching(it::join).getOrNull() }
    }

    private fun wrapYoutubeTrack(youtubeAudioTrack: YoutubeAudioTrack?, spotifyTrack: Track): SpotifyAudioTrack? {
        return SpotifyAudioTrack(youtubeAudioTrack ?: return null, spotifyTrack)
    }

    private fun buildAudioReference(track: Track) = AudioReference("ytsearch:%s %s".format(track.name, track.artists[0].name), null)

    private fun rejected(result: Result<*>) = result.exceptionOrNull() is RejectedExecutionException

    private fun <T> rethrow(result: Result<T>): T? {
        result.exceptionOrNull()?.let { throw RuntimeException(it) }
        return result.getOrNull()
    }

    private fun <T, R> with(obj: T, block: (T) -> R): R = block(obj)
}