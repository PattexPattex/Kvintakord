package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.fragments.ContextMenuBuilder
import com.pattexpattex.kvintakord.app.openUrl
import com.pattexpattex.kvintakord.music.audio.MixerManager
import com.pattexpattex.kvintakord.music.player.*
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import tornadofx.*
import java.util.concurrent.TimeUnit

class TrackControlsView : View("TrackControls") {
    private val playerManager by inject<PlayerManager>()
    private val queueManager by inject<QueueManager>()
    private val mixerManager by inject<MixerManager>()

    init {

        Executors.scheduledExecutor.scheduleAtFixedRate({
            runLater { playerManager.audioPlayer.playingTrackProperty.flatMap { it.positionProperty }.value }
        }, 0, 1, TimeUnit.SECONDS)
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

                    imageProperty().bind(playerManager.audioPlayer.playingTrackProperty.map {
                        runCatching { Image(it.metadata?.image) }.getOrNull()
                    })

                    fitWidthProperty().bind(imageProperty().map { if (it == null) .0 else 100.0 })
                }
            }

            vbox {
                //hgrow = Priority.ALWAYS
                alignment = Pos.CENTER_LEFT
                paddingLeft = 10
                //minWidth = 150.0
                //maxWidth = 200.0

                hyperlink(playerManager.audioPlayer.playingTrackProperty.map {
                    when (it) {
                        null -> null
                        else -> it.metadata?.name ?: "Untitled"
                    }
                }) {
                    //hgrow = Priority.ALWAYS
                    maxWidth = Region.USE_PREF_SIZE

                    hiddenWhen { textProperty().isNull }

                    action {
                        playerManager.audioPlayer.playingTrack?.metadata?.uri?.let { openUrl(it) }
                    }

                    ContextMenuBuilder.hyperlink(this, playerManager.audioPlayer.playingTrackProperty.map { it.metadata?.uri })
                }.addClass(Style.GenericTrackNameLabel)

                hyperlink(playerManager.audioPlayer.playingTrackProperty.map { it.metadata?.author }) {
                    maxWidth = Region.USE_PREF_SIZE
                    //hgrow = Priority.ALWAYS

                    hiddenWhen { textProperty().isNull }

                    action {
                        playerManager.audioPlayer.playingTrack?.metadata?.authorUrl?.let { openUrl(it) }
                    }

                    ContextMenuBuilder.hyperlink(this, playerManager.audioPlayer.playingTrackProperty.map { it.metadata?.authorUrl })
                }.addClass(Style.GenericTrackAuthorLabel)

                // TODO
                /*scrollpane {
                    //maxWidth = 200.0
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    hyperlink {
                        minWidth = Region.USE_PREF_SIZE

                        textProperty().bind(player.musicManager.currentTrack.map { when (it) {
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

                    textProperty().bind(queueManager.shuffleProperty.map { it.emoji })
                    action {
                        queueManager.shuffle++
                    }
                }.addClass(Style.TrackControlButton)

                button("⏹") { //Stop
                    action {
                        playerManager.stop()
                    }
                }.addClass(Style.TrackControlButton)

                button { //Play/Pause
                    textProperty().bind(playerManager.audioPlayer.isPausedProperty.map { if (it) "▶" else "⏸" })

                    action {
                        playerManager.togglePaused()
                    }
                }.addClass(Style.TrackControlButton)

                button("⏭") { //Skip
                    action {
                        queueManager.skipTrack()
                    }
                }.addClass(Style.TrackControlButton)

                button { //Loop
                    textProperty().bind(queueManager.loopProperty.map { it.emoji })

                    action {
                        queueManager.loop++
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

                val positionProperty = playerManager.audioPlayer.playingTrackProperty.select { it.positionProperty }

                label(positionProperty.map { it.toReadableTime() }).addClass(Style.TrackControlTimeLabel)
                slider {
                    hgrow = Priority.ALWAYS
                    min = .0

                    maxProperty().bind(playerManager.audioPlayer.playingTrackProperty.map {
                        when (it.duration) {
                            Long.MAX_VALUE -> -1.0
                            else -> it?.duration?.toDouble() ?: .0
                        }
                    })

                    disableWhen {
                        playerManager.audioPlayer.playingTrackProperty.map { it == null || it.duration == Long.MAX_VALUE }
                    }

                    valueProperty().bindBidirectional(positionProperty)
                }
                label {
                    textProperty().bind(playerManager.audioPlayer.playingTrackProperty.map {
                        toReadableTime(it.duration)
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
                    textProperty().bind(playerManager.audioPlayer.volumeProperty.map { if (it != 0) "🔊" else "🔈" })
                    var prevVol = 100

                    action {
                        if (playerManager.audioPlayer.volume != 0) {
                            prevVol = playerManager.audioPlayer.volume
                            playerManager.audioPlayer.volume = 0
                        } else {
                            playerManager.audioPlayer.volume = prevVol
                        }
                    }
                }.addClass(Style.TrackControlButton) //mute

                slider { //volume
                    alignment = Pos.CENTER_RIGHT
                    paddingLeft = 10
                    prefWidth = 100.0

                    valueProperty().bindBidirectional(playerManager.audioPlayer.volumeProperty)
                }
            }

            combobox(mixerManager.mixerProperty, mixerManager.availableMixers).addClass(Style.TrackControlMixerCombobox)
        }
    }
}
