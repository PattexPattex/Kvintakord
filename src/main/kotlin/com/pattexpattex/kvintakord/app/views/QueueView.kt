package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.cell.track.AudioTrackCell
import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.pattexpattex.kvintakord.music.player.QueueManager
import javafx.geometry.Insets
import tornadofx.*

class QueueView : View() {
    private val playerManager by inject<PlayerManager>()
    private val queueManager by inject<QueueManager>()

    override val root = borderpane {
        addClass(Style.QueueView)

        top = vbox {
            label("Current track") {
                addClass(Style.QueueCurrentTrackLabel)
                padding = Insets(6.0, .0, 2.0, 10.0)
            }

            listview(playerManager.audioPlayer.playingTrackProperty.map { observableListOf(it) }) {
                addClass(Style.QueueCurrentTrackListView)
                prefHeight = 48.0
                setCellFactory { AudioTrackCell().addClass(Style.CurrentTrackCell) }
            }
        }

        center = vbox {
            label("Next up") {
                padding = Insets(6.0, .0, 2.0, 10.0)
            }.addClass(Style.QueueNextTrackLabel)

            listview(queueManager.queue) {
                addClass(Style.QueueNextTrackListView)
                prefHeightProperty().bind(items.sizeProperty.times(46.0).plus(2))
                placeholder = label("Queue is empty").addClass(Style.QueueListViewPlaceholder)

                setCellFactory { AudioTrackCell().addClass(Style.QueuedTrackCell) }
            }
        }
    }
}