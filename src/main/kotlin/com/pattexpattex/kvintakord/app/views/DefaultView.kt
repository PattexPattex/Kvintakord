package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.music.player.PlayerManager
import tornadofx.*

class DefaultView : View() {
    val player by inject<PlayerManager>()

    init {
        titleProperty.bind(player.audioPlayer.playingTrackProperty.stringBinding { it?.clientInfo?.title ?: "Nothing is playing" })
    }

    override val root = borderpane {
        setPrefSize(1080.0, 720.0)

        center = tabpane {
            style {
                backgroundColor += Style.Colors.SyntaxBG
            }

            tab("Queue") {
                isClosable = false
                add<QueueView>()
            }

            tab("Search") {
                isClosable = false
                add<SearchView>()
            }
        }.addClass(Style.ViewSelectionTabPane)

        bottom = find(TRACK_CONTROLS_CLASS).root
    }

    companion object {
        val TRACK_CONTROLS_CLASS = TrackControlsView::class
    }
}