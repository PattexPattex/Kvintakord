package com.pattexpattex.kvintakord.music.spotify

import com.adamratzman.spotify.models.Track
import com.pattexpattex.kvintakord.music.player.TrackMetadata
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack

class SpotifyAudioTrack(baseAudioTrack: YoutubeAudioTrack?, backingTrack: Track) : YoutubeAudioTrack(baseAudioTrack?.info, baseAudioTrack?.sourceManager as? YoutubeAudioSourceManager) {
    init {
        TrackMetadata.buildForSpotify(this, backingTrack)
    }
}