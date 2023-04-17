package com.pattexpattex.kvintakord.music.spotify

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import java.io.DataInput
import java.io.DataOutput
import java.io.UnsupportedEncodingException
import java.lang.RuntimeException

class SpotifyAudioSourceManager(
    private val spotifyApiManager: SpotifyApiManager,
    private val youtubeAudioSourceManager: YoutubeAudioSourceManager
) : AudioSourceManager {
    private val searchUtil = SpotifySearchManager(spotifyApiManager, youtubeAudioSourceManager)

    override fun getSourceName() = "spotify"

    override fun shutdown() { spotifyApiManager.shutdown() }

    override fun loadItem(manager: AudioPlayerManager, reference: AudioReference): AudioItem? {
        return try {
            loadItemOnce(manager, reference)
        } catch (e: FriendlyException) {
            if (HttpClientTools.isRetriableNetworkException(e)) {
                loadItemOnce(manager, reference)
            } else {
                throw e
            }
        }
    }

    private fun loadItemOnce(manager: AudioPlayerManager, reference: AudioReference): AudioItem? {
        if (!spotifyApiManager.isEnabled()) {
            return null
        }

        val identifier = reference.identifier

        if (identifier.startsWith(SEARCH_PREFIX)) {
            return searchUtil.getSearch(manager, identifier.substring(SEARCH_PREFIX.length).trim())
        }

        val matcher = SpotifyApiManager.URL_PATTERN.matcher(identifier)
        if (!matcher.find()) {
            return null
        }

        return when (matcher.group(2)) {
            "track" -> { searchUtil.getTrack(manager, identifier) }
            "playlist" -> { searchUtil.getPlaylist(manager, identifier) }
            "album" -> { searchUtil.getAlbum(manager, identifier) }
            "artist" -> { searchUtil.getArtistTracks(manager, identifier) }
            else -> throw RuntimeException("Unsupported URL")
        }
    }

    override fun isTrackEncodable(track: AudioTrack) = false
    override fun encodeTrack(track: AudioTrack, output: DataOutput) { throw UnsupportedEncodingException() }
    override fun decodeTrack(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack { throw UnsupportedEncodingException() }

    companion object {
        private const val SEARCH_PREFIX = "spsearch:"
    }
}