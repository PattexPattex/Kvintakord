package com.pattexpattex.kvintakord.music.adapter

import com.pattexpattex.kvintakord.app.toReadOnlyProperty
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.adapter.JavaBeanLongPropertyBuilder
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder
import javafx.beans.property.adapter.ReadOnlyJavaBeanObjectPropertyBuilder
import tornadofx.getValue

class AudioTrackAdapter private constructor(track: InternalAudioTrack) : InternalAudioTrack by track {
	private val _stateProperty = ReadOnlyJavaBeanObjectPropertyBuilder.create<AudioTrackState>().bean(track).name("state").build()
	private val _positionProperty = JavaBeanLongPropertyBuilder.create().bean(track).name("position").build()
	private val _userDataProperty = JavaBeanObjectPropertyBuilder.create().bean(track).name("userData").build()

	val stateProperty: ReadOnlyObjectProperty<AudioTrackState> = _stateProperty
	val positionProperty: LongProperty = _positionProperty
	val userDataProperty: ObjectProperty<Any> = _userDataProperty
	val infoProperty = track.info.toReadOnlyProperty()
	val clientInfoProperty = ClientAudioTrackInfo.create(track).toReadOnlyProperty()
	val durationProperty = track.duration.toReadOnlyProperty()
	val identifierProperty = track.identifier.toReadOnlyProperty()
	val isSeekableProperty = track.isSeekable.toReadOnlyProperty()
	val sourceManagerProperty = track.sourceManager.toReadOnlyProperty()

	override fun setPosition(position: Long) = _positionProperty.set(position)
	override fun setUserData(userData: Any?) = _userDataProperty.set(userData)
	val clientInfo: ClientAudioTrackInfo by clientInfoProperty

	fun clone() = wrap(makeClone())!!

	fun updatePosition() { _positionProperty.fireValueChangedEvent() }

	companion object {
		fun wrap(track: AudioTrack?) = when (track) {
			is AudioTrackAdapter -> track
			else -> track?.let { AudioTrackAdapter(it as InternalAudioTrack) }
		}
		fun clone(track: AudioTrack) = wrap(track.makeClone())!!
	}
}