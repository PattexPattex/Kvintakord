package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.fragments.AudioTrackCell
import com.pattexpattex.kvintakord.music.player.PlayerManager
import javafx.geometry.Insets
import tornadofx.*

class QueueView : View() {
    private val player = find<PlayerManager>()

    override val root = borderpane {
        top = vbox {
            label("Current track") {
                padding = Insets(6.0, .0, 2.0, 10.0)
            }.addClass(Style.QueueCurrentTrackLabel)

            listview(player.audioPlayer.playingTrackProperty.map { observableListOf(it) }) {
                prefHeight = 48.0
                setCellFactory { AudioTrackCell().addClass(Style.CurrentTrackCell) }
            }.addClass(Style.QueueCurrentTrackListView)
        }

        center = vbox {
            label("Next up") {
                padding = Insets(6.0, .0, 2.0, 10.0)
            }.addClass(Style.QueueNextTrackLabel)

            listview(player.queueManager.queue) {
                setCellFactory { AudioTrackCell().addClass(Style.QueuedTrackCell) }

                prefHeightProperty().bind(items.sizeProperty.times(46.0).plus(2))

                placeholder = label("Queue is empty").addClass(Style.QueueListViewPlaceholder)
            }.addClass(Style.QueueNextTrackListView)
        }
    }.addClass(Style.QueueView)
}