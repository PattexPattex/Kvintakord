package com.pattexpattex.kvintakord.app.views.trackcontrol

import com.pattexpattex.kvintakord.app.ContextMenuBuilder
import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.openUrl
import com.pattexpattex.kvintakord.music.player.PlayerManager
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import tornadofx.*

class CurrentTrackTitleView : View() {
    private val playerManager: PlayerManager by inject()
    private val playingTrackProperty = playerManager.audioPlayer.playingTrackProperty

    override val root = hbox {
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

                imageProperty().bind(playingTrackProperty.map { Image(it.clientInfo.imageUrl) })
                fitWidthProperty().bind(imageProperty().map { if (it == null) .0 else 100.0 })
            }
        }

        vbox {
            alignment = Pos.CENTER_LEFT
            paddingLeft = 10

            hyperlink(playingTrackProperty.map { it.clientInfo.title }) {
                addClass(Style.GenericTrackNameLabel)
                maxWidth = Region.USE_PREF_SIZE

                hiddenWhen { textProperty().isNull }
                action { openUrl(playerManager.audioPlayer.playingTrack?.clientInfo?.uri) }
                ContextMenuBuilder.hyperlink(this, playingTrackProperty.map { it.clientInfo.uri })
            }

            hyperlink(playingTrackProperty.map { it.clientInfo.author }) {
                addClass(Style.GenericTrackAuthorLabel)
                maxWidth = Region.USE_PREF_SIZE

                hiddenWhen { textProperty().isNull }
                action { openUrl(playerManager.audioPlayer.playingTrack?.clientInfo?.authorUrl) }
                ContextMenuBuilder.hyperlink(this, playingTrackProperty.map { it.clientInfo.authorUrl })
            }
        }
    }
}
