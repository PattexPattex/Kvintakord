package com.pattexpattex.kvintakord.app.views.trackcontrol

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.music.audio.MixerManager
import com.pattexpattex.kvintakord.music.player.PlayerManager
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class SoundControlView : View() {
    private val mixerManager: MixerManager by inject()
    private val playerManager: PlayerManager by inject()

    override val root = vbox {
        hgrow = Priority.ALWAYS
        alignment = Pos.CENTER_RIGHT
        minWidth = 180.0
        prefWidth = 380.0
        paddingRight = 10

        hbox {
            paddingBottom = 10

            button(playerManager.audioPlayer.volumeProperty.map { if (it != 0) "🔊" else "🔈" }) {
                addClass(Style.TrackControlButton)
                var prevVol = 100

                action {
                    if (playerManager.audioPlayer.volume != 0) {
                        prevVol = playerManager.audioPlayer.volume
                        playerManager.audioPlayer.volume = 0
                    } else {
                        playerManager.audioPlayer.volume = prevVol
                    }
                }
            } //mute

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
