package com.pattexpattex.kvintakord.app.views.trackcontrol

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.pattexpattex.kvintakord.music.player.QueueManager
import com.pattexpattex.kvintakord.music.player.toReadableTime
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import tornadofx.*

class CurrentTrackControlView : View() {
    private val playerManager: PlayerManager by inject()
    private val queueManager: QueueManager by inject()
    private val playingTrackProperty = playerManager.audioPlayer.playingTrackProperty

    override val root = vbox {
        hgrow = Priority.ALWAYS
        minWidth = 300.0
        prefWidth = 400.0
        paddingAll = 10
        paddingBottom = 0
        alignment = Pos.CENTER

        tilepane {
            alignment = Pos.CENTER
            hgap = 5.0
            prefRows = 1

            button(queueManager.shuffleProperty.map { it.emoji }) { //Shuffle
                addClass(Style.TrackControlButton)
                action { queueManager.shuffle++ }
            }

            button("⏹") { //Stop
                addClass(Style.TrackControlButton)
                action { playerManager.stop() }
            }

            button(playerManager.audioPlayer.isPausedProperty.map { if (it) "▶" else "⏸" }) { //Play/Pause
                addClass(Style.TrackControlButton)
                action { playerManager.togglePaused() }
            }

            button("⏭") { //Skip
                addClass(Style.TrackControlButton)
                action { queueManager.skipTrack() }
            }

            button(queueManager.loopProperty.map { it.emoji }) { //Loop
                addClass(Style.TrackControlButton)
                action { queueManager.loop++ }
            }
        }
        hbox {
            alignment = Pos.CENTER
            paddingAll = 10
            prefWidth = 600.0
            maxWidth = Region.USE_PREF_SIZE

            val positionProperty = playingTrackProperty.select { it.positionProperty }

            label(positionProperty.map { it.toReadableTime() }).addClass(Style.TrackControlTimeLabel)
            slider {
                hgrow = Priority.ALWAYS
                min = .0

                maxProperty().bind(playingTrackProperty.map {
                    when (it.duration) {
                        Long.MAX_VALUE -> -1.0
                        else -> it.duration.toDouble()
                    }
                })

                disableWhen {
                    playingTrackProperty.booleanBinding { it == null || it.duration == Long.MAX_VALUE }
                }

                valueProperty().bindBidirectional(positionProperty)
            }

            label(playingTrackProperty.map { it.duration.toReadableTime() }).addClass(Style.TrackControlTimeLabel)
        }
    }
}
