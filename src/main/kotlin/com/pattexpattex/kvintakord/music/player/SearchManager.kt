package com.pattexpattex.kvintakord.music.player

import com.pattexpattex.kvintakord.music.adapter.AudioTrackAdapter
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import tornadofx.*
import java.util.regex.Pattern

class SearchManager : Controller() {
	private val playerManager by inject<PlayerManager>()

	var inputField by singleAssign<TextField>()
	var sourceToggleGroup by singleAssign<ToggleGroup>()
	val currentResults = observableListOf<AudioTrackAdapter>()
	val currentResultsQueryProperty = stringProperty()

	fun searchAndDisplay() {
		val query = inputField.text ?: return
		val source = sourceToggleGroup.selectedValueProperty<String>().get()

		currentResultsQueryProperty.set(query)
		playerManager.audioPlayerManager.loadItem(formatQuery(query, source), LoadHandler())
	}

	private fun formatQuery(query: String, source: String?) = if (URI_PATTERN.matcher(query).matches()) {
		query
	} else {
		"${source ?: "ytsearch:"}$query"
	}

	private inner class LoadHandler : AudioLoadResultHandler {
		override fun trackLoaded(track: AudioTrack) {
			playlistLoaded(BasicAudioPlaylist(currentResultsQueryProperty.get(), listOf(track), null, false))
		}

		override fun playlistLoaded(playlist: AudioPlaylist) {
			runLater {
				currentResults.setAll(
					playlist.tracks
						.map(TrackMetadata::buildFor)
						.mapNotNull(AudioTrackAdapter::wrap)
				)
			}
		}

		override fun noMatches() { TODO("Not yet implemented") }
		override fun loadFailed(exception: FriendlyException) { TODO("Not yet implemented") }
	}

	companion object {
		private val URI_PATTERN = Pattern.compile("((\\w+://)[-a-zA-Z0-9:@;?&=/%+.*!'(),\$_{}^~\\[\\]`#|]+)")
		val SOURCES = mapOf(
			"Youtube" to "ytsearch:",
			"Youtube Music" to "ytmsearch:",
			"Spotify" to "spsearch:",
			"SoundCloud" to "scsearch:"
		)
	}
}