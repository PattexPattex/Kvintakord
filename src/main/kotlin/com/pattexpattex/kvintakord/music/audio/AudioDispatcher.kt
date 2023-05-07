package com.pattexpattex.kvintakord.music.audio

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.sound.sampled.*

class AudioDispatcher(
    selectedMixer: String,
    private val stream: AudioInputStream
) {
    private val mixer: AtomicReference<Mixer?> = AtomicReference()
    private val line: AtomicReference<SourceDataLine?> = AtomicReference()
    @Volatile private var runThreads = true
    private val audioPlaybackThread: Thread
    private val playbackStuckThread: Thread
    private val writeStartTime: AtomicLong = AtomicLong()
    @Volatile private lateinit var prevMixers: List<Mixer>
    private val updateListeners = arrayListOf<(AudioDispatcher) -> Unit>()

    init {
        setMixer(selectedMixer)

        audioPlaybackThread = Thread {
            var size: Int
            val buf = ByteArray(StandardAudioDataFormats.COMMON_PCM_S16_BE.maximumChunkSize())

            while (runThreads) {
                size = stream.read(buf)

                if (size >= 0) {
                    writeToLine(buf, size)
                }
            }
        }.apply {
            name = "AudioDispatcherThread"
            isDaemon = true
            start()
        }

        playbackStuckThread = Thread {
            while (runThreads) {
                prevMixers = getMixers()

                try {
                    Thread.sleep(250)
                } catch (ignore: InterruptedException) {}

                val mixers = getMixers()

                if (writeStartTime.get() + 250 < System.currentTimeMillis()) {
                    setMixer(getFallbackMixer())
                } else if (!mixers.containsAll(prevMixers) || !prevMixers.containsAll(mixers)) {
                    notifyListeners()
                }
            }
        }.apply {
            name = "AudioStuckCheckerThread"
            isDaemon = true
            start()
        }
    }

    private fun writeToLine(buf: ByteArray, size: Int) {
        var localLine = line.get()
        while (localLine == null) {
            Thread.onSpinWait()
            localLine = line.get()
        }

        writeStartTime.set(System.currentTimeMillis())
        localLine.write(buf, 0, size)
    }

    fun addListener(function: (AudioDispatcher) -> Unit) {
        updateListeners.add(function)
    }

    fun getMixer(): Mixer? = mixer.get()

    fun setMixer(name: String) {
        setMixer(getMixer(name) ?: getFallbackMixer())
    }

    fun getMixers(): List<Mixer> {
        val lineInfo = Line.Info(SourceDataLine::class.java)

        return AudioSystem.getMixerInfo()
            .mapNotNull {
                try {
                    AudioSystem.getMixer(it)
                } catch (ignored: IllegalArgumentException) {
                    null
                }
            }
            .filter { it.isLineSupported(lineInfo) }
    }

    private fun getFallbackMixer(): Mixer? {
        return getMixers().firstOrNull()
    }


    private fun getMixer(name: String?): Mixer? {
        return getMixers().firstOrNull { it.mixerInfo.name == name }
    }

    private fun setMixer(mixer: Mixer?) {
        if (mixer == this.mixer.get()) {
            return
        }

        this.mixer.set(mixer)
        val oldLine = line.getAndSet(null)
        line.set(getLine())
        closeLine(oldLine)
        notifyListeners()
    }

    private fun getLine(): SourceDataLine {
        val line = line.get()

        return if (line == null || !line.isOpen) {
            val mixer = mixer.get()!!

            val info = DataLine.Info(SourceDataLine::class.java, stream.format)
            val newLine = mixer.getLine(info) as SourceDataLine

            newLine.open(stream.format)
            newLine.start()
            newLine
        } else {
            line
        }
    }

    private fun closeLine(line: Line?) {
        line?.close()
    }

    fun close() {
        closeLine(line.get())
        mixer.set(null)
        line.set(null)
        runThreads = false
        stream.close()
    }

    private fun notifyListeners() {
        updateListeners.forEach { it(this) }
    }
}