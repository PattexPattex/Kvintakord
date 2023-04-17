package com.pattexpattex.kvintakord.music.player

import com.pattexpattex.kvintakord.app.SLF4J
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import tornadofx.*
import java.lang.RuntimeException
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean

//@Suppress("UNUSED")
class MusicManager(private val playerManager: PlayerManager) : AudioEventAdapter() {
    private val _queue: Deque<AudioTrack> = LinkedBlockingDeque()
    private val queueUpdateListeners = CopyOnWriteArrayList<(List<AudioTrack>) -> Unit>()
    private val retried = AtomicBoolean(false)
    private val lock = Any()
    private val _currentTrackProp = ReadOnlyObjectWrapper<AudioTrack?>()
    private var _currentTrack by _currentTrackProp
    val loopMode = objectProperty(LoopMode.OFF)
    private var _loopMode by loopMode
    val shuffleMode = objectProperty(ShuffleMode.OFF)
    private var _shuffleMode by shuffleMode

    val queue: MutableList<AudioTrack> get() = ArrayList(_queue)
    val currentTrack: ObservableValue<AudioTrack?> get() = _currentTrackProp

    init {
        update()
    }

    fun addQueueListener(block: (List<AudioTrack>) -> Unit) { queueUpdateListeners.add(block) }
    fun removeQueueListener(block: (List<AudioTrack>) -> Unit) { queueUpdateListeners.remove(block) }
    private fun update() = queueUpdateListeners.forEach { it(getQueue()) }

    fun addToQueue(audioTrack: AudioTrack) { addTrack(audioTrack); nextTrack(true) }
    fun addToQueueFirst(audioTrack: AudioTrack) { addTrackFirst(audioTrack); nextTrack(true) }
    fun playNow(audioTrack: AudioTrack) { addTrackFirst(audioTrack); skipTrack() }
    fun addToQueue(c: Collection<AudioTrack>) { addTracks(c); nextTrack(true) }
    fun removeFromQueue(audioTrack: AudioTrack) { removeTrack(audioTrack); update() }
    fun removeFromQueue(c: Collection<AudioTrack>) { c.forEach(::removeTrack); update() }
    fun setLoop(loopMode: LoopMode) { _loopMode = loopMode }
    fun incLoop() { setLoop(_loopMode.next()) }
    fun setShuffle(shuffleMode: ShuffleMode) { _shuffleMode = shuffleMode }
    fun incShuffle() { setShuffle(_shuffleMode.next()) }

    fun skipTrack() {
        if (_loopMode == LoopMode.ALL) {
            _currentTrack?.apply { addTrack(makeClone()) }
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

        synchronized(lock) {
            if (_loopMode == LoopMode.ALL) {
                _currentTrack?.apply { addTrack(makeClone()) }
                addTracks(drainQueue(pos))
            } else {
                drainQueue(pos)
            }
        }

        nextTrack(false)
    }

    fun moveTrack(from: Int, to: Int) {
        if (from !in 0 until _queue.size || to !in 0 .. _queue.size) {
            return
        }

        synchronized(lock) {
            removeTrack(from)?.let { addTrack(to, it) }
        }

        update()
    }

    fun restartTrackProgress() {
        _currentTrack?.position = 0
    }

    fun stop() {
        _currentTrack = null
        emptyQueue()
        update()
    }

    @JvmName("getQueueFunction") private fun getQueue() = _queue.toMutableList()
    private fun setQueue(c: Collection<AudioTrack>) = synchronized(lock) { _queue.clear(); _queue.addAll(c) }
    private fun emptyQueue() = _queue.clear()
    private fun drainQueue() = drainQueue(Int.MAX_VALUE)
    private fun drainQueue(max: Int) = ArrayList<AudioTrack>().also { (_queue as LinkedBlockingDeque).drainTo(it, max) }
    private fun getTrack(index: Int) = getQueue().getOrNull(index)
    private fun addTrack(audioTrack: AudioTrack) = _queue.addLast(audioTrack)
    private fun addTrackFirst(audioTrack: AudioTrack) = _queue.addFirst(audioTrack)
    private fun addTrack(index: Int, audioTrack: AudioTrack) = setQueue(getQueue().apply { add(index, audioTrack) })
    private fun addTracks(audioTracks: Collection<AudioTrack>) = _queue.addAll(audioTracks)
    private fun removeTrack(index: Int) = getQueue().getOrNull(index).also { _queue.remove(it) }
    private fun removeTrack(audioTrack: AudioTrack?) = _queue.remove(audioTrack)

    private fun nextTrack(noInterrupt: Boolean) {
        if (_shuffleMode.enabled && _queue.size > 1) {
            val random = SecureRandom()
            val list = getQueue()
            //val temp = list.removeLast()
            val next = list[random.nextInt(list.size)]

            //list.add(list.size, temp)
            removeTrack(next)

            if (!playerManager.playTrack(ensureNotDuplicate(next), noInterrupt)) {
                setQueue(list)
                update()
            } else {
                runLater { playerManager.paused.value = false }
            }
        } else {
            val next = _queue.pollFirst() ?: return playerManager.stop()

            if (!playerManager.playTrack(ensureNotDuplicate(next), noInterrupt)) {
                addTrackFirst(next)
                update()
            } else {
                runLater { playerManager.paused.value = false }
            }
        }
    }

    private fun ensureNotDuplicate(audioTrack: AudioTrack): AudioTrack {
        return if ((audioTrack as InternalAudioTrack).activeExecutor.state != AudioTrackState.INACTIVE) {
            audioTrack.makeClone()
        } else {
            audioTrack
        }
    }

    override fun onPlayerPause(player: AudioPlayer?) {
        super.onPlayerPause(player)
    }

    override fun onPlayerResume(player: AudioPlayer?) {
        super.onPlayerResume(player)
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
        runLater { _currentTrack = track }
        update()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        runLater { _currentTrack = null }

        if (endReason == AudioTrackEndReason.LOAD_FAILED && retried.compareAndSet(false, true)) {
            runCatching { Thread.sleep(1000) }

            addToQueueFirst(track.makeClone())
            return
        } else {
            retried.set(false)
        }

        if (endReason.mayStartNext) {
            when (_loopMode) {
                LoopMode.SINGLE -> addToQueueFirst(track.makeClone())
                LoopMode.ALL -> addToQueue(track.makeClone())
                else -> nextTrack(true)
            }
        }
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) {
        if (_queue.isEmpty()) {
            stop() //TODO error handling
        }
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long, stackTrace: Array<StackTraceElement>) {
        SLF4J(this::class).error("got stuck", RuntimeException().apply { setStackTrace(stackTrace) })
        nextTrack(false) // TODO error handling
    }
}
