package com.pattexpattex.kvintakord.music.audio

import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormatTools
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.ObservableList
import javafx.collections.ObservableListBase
import tornadofx.*
import javax.sound.sampled.*

class MixerManager : Controller() {
    private val format = AudioDataFormatTools.toAudioFormat(PlayerManager.OUTPUT_FORMAT)

    val availableMixers: ObservableList<String> = MixerList(getAvailableMixers().map { it.mixerInfo.name })

    val mixerProperty = stringProperty(config.string("mixer", "Primary Sound Driver"))
    var mixer: String? by mixerProperty

    private val _lineProperty = ReadOnlyObjectWrapper<SourceDataLine>(this, "line")
    val lineProperty: ReadOnlyObjectProperty<SourceDataLine> = _lineProperty.readOnlyProperty
    val line: SourceDataLine? by _lineProperty

    init {
        setMixer(mixer)

        mixerProperty.onChange {
            setMixer(it)
            config["mixer"] = it
            config.save()
        }

        createUpdateCheckerThread().start()
    }

    fun useFallback() {
        //log.info("Switching to fallback")
        mixer = getFallbackMixer().mixerInfo.name
    }

    private fun updateList() {
        //log.info("Updating available mixers")
        availableMixers.setAll(getAvailableMixers().map { it.mixerInfo.name })
    }

    @JvmName("getAvailableMixersFun")
    private fun getAvailableMixers(): List<Mixer> {
        val lineInfo = Line.Info(SourceDataLine::class.java)

        return AudioSystem.getMixerInfo()
            .mapNotNull { it.runCatching(AudioSystem::getMixer).getOrNull() }
            .filter { it.isLineSupported(lineInfo) }
    }

    private fun getMixer(name: String) = getAvailableMixers().find { it.mixerInfo.name == name } ?: getFallbackMixer()

    private fun getFallbackMixer() = getAvailableMixers().first()

    //private var oldMixer = mixer
    @JvmName("setMixerInternal")
    private fun setMixer(name: String?) {
        if (name == null) {
            return
        }

        //log.info("$oldMixer -> $name")
        //oldMixer = mixer

        val mixer = getMixer(name)
        val oldLine = line
        _lineProperty.set(createLine(mixer))
        oldLine?.close()
    }

    private fun createLine(mixer: Mixer): SourceDataLine {
        val info = DataLine.Info(SourceDataLine::class.java, format)
        val line = mixer.getLine(info) as SourceDataLine
        line.open(format)
        line.start()
        return line
    }

    @Volatile private var run = true
    fun destroy() {
        run = false
    }

    private fun createUpdateCheckerThread() = Thread {
        var prevMixers = getAvailableMixers()

        while (run) {
            runCatching { Thread.sleep(1000) }

            val newMixers = getAvailableMixers()
            if (prevMixers != newMixers) {
                runLater { updateList() }
            }
            prevMixers = newMixers
        }
    }.apply {
        name = "MixerUpdateCheckerThread"
        isDaemon = true
    }

    private class MixerList(list: List<String>) : ObservableListBase<String>() {
        private val list = ArrayList(list)

        override val size get() = list.size
        override fun get(index: Int): String = list[index]

        override fun setAll(c: Collection<String>): Boolean {
            beginChange()
            return try {
                nextAdd(0, c.size)
                list.clear()
                list.addAll(c)
            } finally {
                endChange()
            }
        }
    }
}