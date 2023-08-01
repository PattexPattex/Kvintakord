package com.pattexpattex.kvintakord.music.adapter

import com.pattexpattex.kvintakord.music.spotify.SpotifyAudioTrack
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class ClientAudioTrackInfo(
    val title: String,
    val author: String,
    val length: Long,
    val identifier: String,
    val isStream: Boolean,
    val uri: String,
    val authorUrl: String?,
    val album: String?,
    val albumUrl: String?,
    val imageUrl: String?
) {
    companion object {
        fun create(track: AudioTrack): ClientAudioTrackInfo {
            val imageUrl = when (track) {
                is SpotifyAudioTrack -> track.backingTrack.album.images.firstOrNull()?.url
                is YoutubeAudioTrack -> YT_IMAGE_URL.format(track.identifier)
                else -> null
            }

            val (album, albumUrl) = when (track) {
                is SpotifyAudioTrack -> track.backingTrack.album.name to track.backingTrack.album.externalUrls.spotify
                else -> null to null
            }

            val authorUrl = when (track) {
                is SpotifyAudioTrack -> with(track.backingTrack) { artists[0].externalUrls.spotify }
                else -> null
            }

            val title = when (track) {
                is SpotifyAudioTrack -> track.backingTrack.name
                else -> track.info.title
            }

            val author = when (track) {
                is SpotifyAudioTrack -> track.backingTrack.artists[0].name
                else -> track.info.author
            }

            val identifier = when (track) {
                is SpotifyAudioTrack -> track.backingTrack.id
                else -> track.info.identifier
            }

            val uri = when (track) {
                is SpotifyAudioTrack -> track.backingTrack.externalUrls.spotify ?: track.info.uri
                else -> track.info.uri
            }

            return ClientAudioTrackInfo(
                title,
                author,
                track.info.length,
                identifier,
                track.info.isStream,
                uri,
                authorUrl,
                album,
                albumUrl,
                imageUrl
            )
        }

        const val YT_IMAGE_URL = "https://img.youtube.com/vi/%s/mqdefault.jpg"
    }
}
