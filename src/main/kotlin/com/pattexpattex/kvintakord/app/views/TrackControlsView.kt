package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.ObjectPropertyWrapper
import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.fragments.ContextMenuBuilder
import com.pattexpattex.kvintakord.app.openUrl
import com.pattexpattex.kvintakord.music.audio.AudioDispatcher
import com.pattexpattex.kvintakord.music.player.Executors
import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.pattexpattex.kvintakord.music.player.metadata
import com.pattexpattex.kvintakord.music.player.toReadableTime
import javafx.beans.property.Property
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import tornadofx.*
import java.util.concurrent.TimeUnit

class TrackControlsView : View("TrackControls") {
    private val player = find<PlayerManager>()
    private var prevVol: Int = 100
    private val mixer = stringProperty()
    private val mixers = listProperty(arrayListOf<String>().asObservable())
    private val position = ObjectPropertyWrapper({ player.musicManager.currentTrack.value?.position ?: 0 }, { player.musicManager.currentTrack.value?.position = it })

    init {
        player.audioDispatcher.addListener(::updateMixers)
        updateMixers(player.audioDispatcher)

        Executors.scheduledExecutor.scheduleAtFixedRate({
            runLater { position.setLazy(player.musicManager.currentTrack.value?.position ?: 0) }
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun updateMixers(dispatcher: AudioDispatcher) {
        runLater {
            val newMixer = dispatcher.getMixer()?.mixerInfo?.name
            mixer.set(newMixer)
            mixers.setAll(dispatcher.getMixers().map { it.mixerInfo.name })
            mixer.set(newMixer)
        }
    }

    override val root = hbox {
        alignment = Pos.CENTER

        addClass(Style.TrackControl)

        prefHeight = 100.0

        hbox {
            alignment = Pos.CENTER_LEFT
            hgrow = Priority.ALWAYS
            prefWidth = 380.0
            minWidth = 180.0

            vbox {
                alignment = Pos.CENTER

                imageview {
                    fitWidth = 100.0
                    isSmooth = true
                    isPreserveRatio = true

                    imageProperty().bind(player.musicManager.currentTrack.objectBinding {
                        runCatching { Image(it?.metadata?.image) }.getOrNull()
                    })

                    fitWidthProperty().bind(imageProperty().doubleBinding { if (it == null) .0 else 100.0 })
                }
            }

            vbox {
                //hgrow = Priority.ALWAYS
                alignment = Pos.CENTER_LEFT
                paddingLeft = 10
                //minWidth = 150.0
                //maxWidth = 200.0

                hyperlink {
                    //hgrow = Priority.ALWAYS
                    maxWidth = Region.USE_PREF_SIZE

                    hiddenWhen { textProperty().isNull }
                    textProperty().bind(player.musicManager.currentTrack.stringBinding {
                        when (it) {
                            null -> null
                            else -> it.metadata?.name ?: "Untitled"
                        }
                    })

                    action {
                        player.musicManager.currentTrack.value?.info?.uri?.let { openUrl(it) }
                    }

                    ContextMenuBuilder.hyperlink(this, player.musicManager.currentTrack.stringBinding { it?.metadata?.uri })
                }.addClass(Style.GenericTrackNameLabel)

                hyperlink {
                    maxWidth = Region.USE_PREF_SIZE
                    //hgrow = Priority.ALWAYS

                    hiddenWhen { textProperty().isNull }
                    textProperty().bind(player.musicManager.currentTrack.stringBinding { it?.metadata?.author })

                    action {
                        player.musicManager.currentTrack.value?.metadata?.authorUrl?.let { openUrl(it) }
                    }

                    ContextMenuBuilder.hyperlink(this, player.musicManager.currentTrack.stringBinding { it?.metadata?.authorUrl })
                }.addClass(Style.GenericTrackAuthorLabel)

                // TODO
                /*scrollpane {
                    //maxWidth = 200.0
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    hyperlink {
                        minWidth = Region.USE_PREF_SIZE

                        textProperty().bind(player.musicManager.currentTrack.stringBinding { when (it) {
                            null -> "Nothing is playing"
                            else -> it.metadata?.name ?: "Untitled"
                        } })

                        action {
                            player.musicManager.currentTrack.value?.info?.uri?.let { hostServices.showDocument(it) }
                        }

                        ContextMenuBuilder.hyperlink(this, player.musicManager.currentTrack.value?.metadata?.uri)
                    }.addClass(Style.GenericTrackNameLabel)

                    sequentialTransition(true) {
                        timeline {
                            keyframe(10.seconds) {
                                keyvalue(this@scrollpane.hvalueProperty(), this@scrollpane.hmax)
                            }
                        }

                        timeline {
                            keyframe(2.seconds) {}
                        }

                        timeline {
                            keyframe(10.seconds) {
                                keyvalue(this@scrollpane.hvalueProperty(), 0)
                            }
                        }

                        timeline {
                            keyframe(2.seconds) {}
                        }

                        cycleCount = Timeline.INDEFINITE
                    }
                }*/
            }
        }

        vbox {
            hgrow = Priority.ALWAYS
            minWidth = 300.0
            //maxWidth = Region.USE_PREF_SIZE
            prefWidth = 400.0
            paddingAll = 10
            paddingBottom = 0
            alignment = Pos.CENTER

            tilepane {
                alignment = Pos.CENTER
                hgap = 5.0
                prefRows = 1

                button { //Shuffle
                    textProperty().bind(player.musicManager.shuffleMode.stringBinding { it!!.emoji })

                    action {
                        player.musicManager.incShuffle()
                    }
                }.addClass(Style.TrackControlButton)

                button("⏹") { //Stop
                    action {
                        player.stop()
                    }
                }.addClass(Style.TrackControlButton)

                button { //Play/Pause
                    textProperty().bind(player.paused.stringBinding { if (it == true) "▶" else "⏸" })

                    action {
                        player.togglePaused()
                    }
                }.addClass(Style.TrackControlButton)

                button("⏭") { //Skip
                    action {
                        player.musicManager.skipTrack()
                    }
                }.addClass(Style.TrackControlButton)

                button { //Loop
                    textProperty().bind(player.musicManager.loopMode.stringBinding { it!!.emoji })

                    action {
                        player.musicManager.incLoop()
                    }
                }.addClass(Style.TrackControlButton)
            }
            hbox {
                //hgrow = Priority.ALWAYS
                alignment = Pos.CENTER
                paddingAll = 10
                //minWidth = 240.0
                prefWidth = 600.0
                maxWidth = Region.USE_PREF_SIZE

                label {
                    textProperty().bind(position.stringBinding { it.toReadableTime() })
                }.addClass(Style.TrackControlTimeLabel)
                slider {
                    hgrow = Priority.ALWAYS
                    min = .0

                    maxProperty().bind(player.musicManager.currentTrack.doubleBinding {
                        when (it?.duration) {
                            Long.MAX_VALUE -> -1.0
                            else -> it?.duration?.toDouble() ?: .0
                        }
                    })

                    disableWhen {
                        player.musicManager.currentTrack.booleanBinding { it == null || it.duration == Long.MAX_VALUE }
                    }

                    valueProperty().bindBidirectional(position as Property<Number>)
                }
                label {
                    textProperty().bind(player.musicManager.currentTrack.stringBinding {
                        toReadableTime(it?.duration ?: 0)
                    })
                }.addClass(Style.TrackControlTimeLabel)
            }
        }

        vbox {
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER_RIGHT
            minWidth = 180.0
            prefWidth = 380.0
            paddingRight = 10

            hbox {
                paddingBottom = 10

                button {
                    textProperty().bind(player.volume.stringBinding { if (it != 0) "🔊" else "🔈" })

                    action {
                        if (player.volume.value != 0) {
                            prevVol = player.volume.value
                            player.volume.set(0)
                        } else {
                            player.volume.set(prevVol)
                        }
                    }
                }.addClass(Style.TrackControlButton) //mute
                slider { //volume
                    alignment = Pos.CENTER_RIGHT
                    paddingLeft = 10
                    prefWidth = 100.0

                    valueProperty().bindBidirectional(player.volume as Property<Number>)
                }
            }

            combobox(mixer, mixers) {
                mixer.onChange {
                    selectedItem?.let { player.audioDispatcher.setMixer(it) }
                }
            }.addClass(Style.TrackControlMixerCombobox)
        }
    }
}
