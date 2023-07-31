package com.pattexpattex.kvintakord.music.player

import com.pattexpattex.kvintakord.music.adapter.AudioTrackAdapter
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import tornadofx.*
import java.security.SecureRandom
import java.util.concurrent.LinkedBlockingDeque

class QueueManager(private val playerManager: PlayerManager) : AudioEventAdapter() {
	private val lock = Any()

	val loopProperty = objectProperty<LoopMode>()
	var loop: LoopMode by loopProperty

	val shuffleProperty = objectProperty<ShuffleMode>()
	var shuffle: ShuffleMode by shuffleProperty
	
	private val actualQueue = LinkedBlockingDeque<AudioTrackAdapter>()
	private val _queue = observableListOf<AudioTrackAdapter>()
	val queue = _queue.asUnmodifiable()

	fun addToQueue(audioTrack: AudioTrackAdapter) {
		addTrack(audioTrack)
		nextTrack(true)
	}

	fun addToQueue(c: Collection<AudioTrackAdapter>) {
		addTracks(c)
		nextTrack(true)
	}

	fun addToQueueFirst(audioTrack: AudioTrackAdapter) {
		addTrackFirst(audioTrack)
		nextTrack(true)
	}

	fun playNow(audioTrack: AudioTrackAdapter) {
		addTrackFirst(audioTrack)
		skipTrack()
	}

	fun removeFromQueue(audioTrack: AudioTrackAdapter) {
		removeTrack(audioTrack)
		updatePublicQueue()
	}

	fun removeFromQueue(c: Collection<AudioTrackAdapter>) {
		c.forEach(::removeTrack)
		updatePublicQueue()
	}

	fun skipTrack() {
		if (loop == LoopMode.ALL) {
			playerManager.audioPlayer.playingTrack?.let { addTrack(AudioTrackAdapter.clone(it)) }
		}

		nextTrack(false)
	}

	fun skipTrack(pos: Int) {
		if (pos !in 0 until _queue.size) {
			return
		}

		if (pos == 0) {
			return skipTrack()
		}

		if (loop == LoopMode.ALL) {
			playerManager.audioPlayer.playingTrack?.let { addTrack(AudioTrackAdapter.clone(it)) }
			addTracks(drainQueue(pos))
		} else {
			drainQueue(pos)
		}

		nextTrack(false)
	}

	fun moveTrack(from: Int, to: Int) {
		if (from !in 0 until _queue.size || to !in 0 .. _queue.size) {
			return
		}

		removeTrack(from)?.let { addTrack(to, it) }
		updatePublicQueue()
	}

	fun stop() {
		emptyQueue()
		updatePublicQueue()
	}

	private fun updatePublicQueue() {
		_queue.setAll(actualQueue)
	}

	private fun copyQueue() = actualQueue.toMutableList()
	
	private fun setQueue(c: Collection<AudioTrackAdapter>) = synchronized(lock) {
		actualQueue.clear()
		actualQueue.addAll(c)
	}
	
	private fun emptyQueue() = actualQueue.clear()
	private fun drainQueue() = drainQueue(Int.MAX_VALUE)
	private fun drainQueue(max: Int) = ArrayList<AudioTrackAdapter>().also { actualQueue.drainTo(it, max) }
	
	private fun getTrack(index: Int) = queue.getOrNull(index)

	private fun addTrack(audioTrack: AudioTrackAdapter) = actualQueue.addLast(audioTrack)
	private fun addTrackFirst(audioTrack: AudioTrackAdapter) = actualQueue.addFirst(audioTrack)
	private fun addTrack(index: Int, audioTrack: AudioTrackAdapter) = setQueue(copyQueue().apply { add(index, audioTrack) })
	private fun addTracks(c: Collection<AudioTrackAdapter>) = actualQueue.addAll(c)
	private fun addTracks(index: Int, c: Collection<AudioTrackAdapter>) = setQueue(copyQueue().apply { addAll(index, c) })
	
	private fun removeTrack(index: Int) = queue.getOrNull(index).also(::removeTrack)
	private fun removeTrack(audioTrack: AudioTrackAdapter?) = actualQueue.remove(audioTrack)

	private fun nextTrack(noInterrupt: Boolean) {
		val next = if (shuffle.enabled && actualQueue.size > 1) {
			val random = SecureRandom()
			val list = copyQueue()
			list[random.nextInt(list.lastIndex)]
		} else {
			actualQueue.peekFirst() ?: return playerManager.stop()
		}

		if (playerManager.audioPlayer.startTrack(ensureNotDuplicate(next), noInterrupt)) {
			removeTrack(next)
			runLater { playerManager.audioPlayer.isPaused = false }
		}

		updatePublicQueue()
	}

	private fun ensureNotDuplicate(audioTrack: AudioTrackAdapter): AudioTrackAdapter {
		return if (audioTrack.state != AudioTrackState.INACTIVE) {
			AudioTrackAdapter.clone(audioTrack)
		} else {
			audioTrack
		}
	}

	override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
		updatePublicQueue()
	}

	@Volatile private var retried = false

	override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
		if (endReason == AudioTrackEndReason.LOAD_FAILED && !retried) {
			retried = true
			addToQueueFirst(AudioTrackAdapter.clone(track))
			return
		} else {
			retried = false
		}

		if (endReason.mayStartNext) {
			when (loop) {
				LoopMode.SINGLE -> addToQueueFirst(AudioTrackAdapter.clone(track))
				LoopMode.ALL -> addToQueue(AudioTrackAdapter.clone(track))
				else -> nextTrack(true)
			}
		}

		updatePublicQueue()
	}

	override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) {
		updatePublicQueue() //TODO error handling
	}

	override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long, stackTrace: Array<StackTraceElement>) {
		nextTrack(false) // TODO error handling
	}
}