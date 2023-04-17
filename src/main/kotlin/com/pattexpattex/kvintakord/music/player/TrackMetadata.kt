package com.pattexpattex.kvintakord.music.player

import com.adamratzman.spotify.models.Track
import com.pattexpattex.kvintakord.music.spotify.SpotifyAudioTrack
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class TrackMetadata(
    val name: String,
    val author: String,
    val uri: String,
    val image: String?,
    val album: String? = null,
    val authorUrl: String? = null,
    val isSpotify: Boolean = false
) {
    constructor(track: AudioTrack): this(
        track.info.title,
        track.info.author,
        track.info.uri,
        if (track is YoutubeAudioTrack) YT_IMAGE_URL.format(track.identifier) else null,
        null,
        null,
        false
    ) {

    }

    constructor(track: Track): this(
        track.name,
        track.artists[0].name,
        track.externalUrls.spotify!!,
        track.album.images.maxWithOrNull(Comparator.comparingInt { it.width ?: 0 })?.url,
        track.album.name,
        track.artists[0].externalUrls.spotify!!,
        true
    )

    companion object {
        private const val YT_IMAGE_URL = "https://img.youtube.com/vi/%s/mqdefault.jpg"

        fun buildFor(track: AudioTrack): AudioTrack {
            if (track is SpotifyAudioTrack) {
                return track
            }
            if (track.getUserData<TrackMetadata>() != null) {
                return track
            }

            track.userData = TrackMetadata(track)

            return track
        }

        fun buildForSpotify(track: SpotifyAudioTrack, backingTrack: Track) {
            track.userData = TrackMetadata(backingTrack)
        }
    }
}