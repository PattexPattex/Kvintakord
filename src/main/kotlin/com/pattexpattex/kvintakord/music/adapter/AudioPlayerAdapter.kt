package com.pattexpattex.kvintakord.music.adapter

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.adapter.JavaBeanBooleanPropertyBuilder
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder
import javafx.beans.property.adapter.ReadOnlyJavaBeanObjectPropertyBuilder
import javafx.beans.value.ObservableValue
import tornadofx.runLater

class AudioPlayerAdapter private constructor(player: AudioPlayer) : AudioPlayer by player {
	init {
		player.addListener { runLater(_playingTrackProperty::fireValueChangedEvent) }
	}

	private val _playingTrackProperty = ReadOnlyJavaBeanObjectPropertyBuilder.create<AudioTrack>().bean(player).name("playingTrack").build()
	private val _isPausedProperty = JavaBeanBooleanPropertyBuilder.create().bean(player).name("paused").build()
	private val _volumeProperty = JavaBeanIntegerPropertyBuilder.create().bean(player).name("volume").build()

	val playingTrackProperty: ObservableValue<AudioTrackAdapter> = _playingTrackProperty.map { AudioTrackAdapter.wrap(it) }
	val isPausedProperty: BooleanProperty = _isPausedProperty
	val volumeProperty: IntegerProperty = _volumeProperty

	override fun setPaused(value: Boolean) = _isPausedProperty.set(value)
	override fun setVolume(volume: Int) = _volumeProperty.set(volume)
	override fun getPlayingTrack(): AudioTrackAdapter? = playingTrackProperty.value

	companion object {
		fun wrap(player: AudioPlayer) = when (player) {
			is AudioPlayerAdapter -> player
			else -> AudioPlayerAdapter(player)
		}
	}
}
