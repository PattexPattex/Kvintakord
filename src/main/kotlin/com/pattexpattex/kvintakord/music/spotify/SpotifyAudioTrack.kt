package com.pattexpattex.kvintakord.music.spotify

import com.adamratzman.spotify.models.Track
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class SpotifyAudioTrack(
    private val baseAudioTrack: YoutubeAudioTrack,
    val backingTrack: Track
) : YoutubeAudioTrack(
    baseAudioTrack.info,
    baseAudioTrack.sourceManager as? YoutubeAudioSourceManager
) {
    override fun makeShallowClone(): AudioTrack {
        return SpotifyAudioTrack(baseAudioTrack, backingTrack)
    }
}