package com.pattexpattex.kvintakord.music.audio

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import tornadofx.onChange
import tornadofx.runLater
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.SourceDataLine

class AudioDispatcher(
    private val mixerManager: MixerManager,
    private val stream: AudioInputStream
) {
    private val atomicLine = AtomicReference<SourceDataLine>()
    private val playbackThread = createPlaybackThread()
    private val stuckThread = createStuckThread()
    private val lastWrite = AtomicLong()
    @Volatile private var running: Boolean = true

    init {
        atomicLine.set(mixerManager.line)
        mixerManager.lineProperty.onChange { atomicLine.set(it) }
        playbackThread.start()
        stuckThread.start()
    }

    fun destroy() {
        running = false
        stuckThread.interrupt()
    }

    private fun createPlaybackThread() = Thread {
        var size: Int
        val buf = ByteArray(StandardAudioDataFormats.COMMON_PCM_S16_BE.maximumChunkSize())

        while (running) {
            size = stream.read(buf)

            if (size >= 0) {
                write(buf, size)
            }
        }
    }.apply {
        name = "AudioPlaybackThread"
        isDaemon = true
    }

    private fun write(buf: ByteArray, size: Int) {
        ensureLineAvailable()
        markWriteTime()
        val line = atomicLine.get()
        line.write(buf, 0, size)
    }

    private fun markWriteTime() {
        lastWrite.set(currentTime())
    }

    private fun currentTime() = System.currentTimeMillis()

    private fun ensureLineAvailable() {
        while (atomicLine.get() == null) {
            Thread.onSpinWait()
        }
    }

    private fun createStuckThread() = Thread {
        while (running) {
            runCatching { Thread.sleep(1000) }

            if (lastWrite.get() + 1000 < currentTime()) {
                runLater { mixerManager.useFallback() }
            }
        }
    }.apply {
        name = "AudioStuckCheckerThread"
        isDaemon = true
    }
}