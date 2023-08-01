package com.pattexpattex.kvintakord.app.cell.track

import com.pattexpattex.kvintakord.app.ContextMenuBuilder
import com.pattexpattex.kvintakord.app.ImageCache
import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.openUrl
import com.pattexpattex.kvintakord.music.adapter.AudioTrackAdapter
import com.pattexpattex.kvintakord.music.player.QueueManager
import com.pattexpattex.kvintakord.music.player.toReadableTime
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ListCell
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import tornadofx.*

class AudioTrackCell : ListCell<AudioTrackAdapter>() {
    private val queueManager = find<QueueManager>()

    init {
        onDoubleClick {
            if (isQueued()) {
                item?.let(queueManager::skipToTrack)
            } else if (isSearch()) {
                item?.let(queueManager::addToQueue)
            }
        }

        addClass(Style.TrackCell)
        contentDisplay = ContentDisplay.CENTER
        alignment = Pos.CENTER

        setOnDragDetected { event ->
            if (item == null || !isQueued()) {
                return@setOnDragDetected
            }

            val dragboard = startDragAndDrop(TransferMode.MOVE)
            val clipboard = ClipboardContent()
            clipboard.putString(item.identifier)

            dragboard.dragView = ImageCache.getImage(item.clientInfo.imageUrl)
            dragboard.setContent(clipboard)
            event.consume()
        }

        setOnDragOver {
            if (it.gestureSource != this && it.dragboard.hasString()) {
                it.acceptTransferModes(TransferMode.MOVE)
            }

            it.consume()
        }

        setOnDragEntered {
            if (it.gestureSource != this && it.dragboard.hasString()) {
                opacity = .3
            }
        }

        setOnDragExited {
            if (it.gestureSource != this && it.dragboard.hasString()) {
                opacity = 1.0
            }
        }

        setOnDragDropped { event ->
            if (item == null) {
                return@setOnDragDropped
            }

            val dragboard = event.dragboard
            var success = false

            if (dragboard.hasString()) {
                val items = listView.items
                val draggedIndex = items.indexOfFirst { it.identifier == dragboard.string }
                val thisIndex = items.indexOf(item)

                queueManager.moveTrack(draggedIndex, thisIndex)
                success = true
            }

            event.isDropCompleted = success
            event.consume()
        }

        setOnDragDone { it.consume() }
    }

    override fun updateItem(item: AudioTrackAdapter?, empty: Boolean) {
        super.updateItem(item, empty)

        graphic = if (empty || item == null) {
            null
        } else {
            borderpane {
                prefWidth = 300.0

                when {
                    isCurrent() -> ContextMenuBuilder.currentTrack(this, item)
                    isQueued() -> ContextMenuBuilder.queuedTrack(this, item)
                    isSearch() -> ContextMenuBuilder.searchTrack(this, item)
                }

                left = hbox {
                    spacing = 5.0
                    alignment = Pos.CENTER_LEFT

                    label((listView.items.indexOf(item) + 1).toString()) {
                        alignment = Pos.CENTER
                        paddingLeft = 5

                        if (!isQueued()) {
                            hide()
                        }
                    }

                    imageview {
                        image = ImageCache.getImage(item.clientInfo.imageUrl)
                    }
                    paddingRight = 5
                }

                center = vbox {
                    alignment = Pos.CENTER_LEFT
                    prefWidth = 100.0
                    hyperlink(item.clientInfo.title) {
                        paddingAll = 0
                        action {
                            openUrl(item.clientInfo.uri)
                        }

                        ContextMenuBuilder.hyperlink(this, item.clientInfo.uri)
                    }.addClass(Style.GenericTrackNameLabel)
                    hyperlink(item.clientInfo.author) {
                        paddingAll = 0
                        action {
                            openUrl(item.clientInfo.authorUrl)
                        }

                        ContextMenuBuilder.hyperlink(this, item.clientInfo.authorUrl)
                    }.addClass(Style.GenericTrackAuthorLabel)
                }

                right = hbox {
                    spacing = 5.0
                    alignment = Pos.CENTER_RIGHT

                    button("▶") {
                        action {
                            if (isSearch()) {
                                queueManager.playNow(item)
                            } else {
                                queueManager.skipToTrack(listView.items.indexOf(item))
                            }
                        }

                        if (isCurrent()) {
                            hide()
                        }
                    }.addClass(Style.TrackCellButton)

                    button("❌") {
                        action {
                            queueManager.removeFromQueue(item)
                        }

                        if (!isQueued()) {
                            hide()
                        }

                    }.addClass(Style.TrackCellButton)

                    button("➕") {
                        action {
                            queueManager.addToQueue(item)
                        }

                        if (!isSearch()) {
                            hide()
                        }
                    }.addClass(Style.TrackCellButton)

                    label(item.duration.toReadableTime()) {
                        alignment = Pos.CENTER_RIGHT
                    }
                }
            }
        }
    }

    private fun isCurrent() = hasClass(Style.CurrentTrackCell)
    private fun isQueued() = hasClass(Style.QueuedTrackCell)
    private fun isSearch() = hasClass(Style.SearchTrackCell)
}