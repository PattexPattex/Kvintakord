package com.pattexpattex.kvintakord.app.tray

import com.dustinredmond.fxtrayicon.FXTrayIcon
import com.pattexpattex.kvintakord.music.player.*
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.MenuItem
import tornadofx.Controller
import tornadofx.onChange
import tornadofx.runLater
import java.awt.CheckboxMenuItem
import java.awt.Menu
import java.awt.SystemTray

class TrayIconManager : Controller() {
    private val playerManager by inject<PlayerManager>()
    private val queueManager by inject<QueueManager>()

    private val trayIcon by lazy {
        FXTrayIcon.Builder(primaryStage, TrayIconManager::class.java.getResource("/icon.png")).apply {
            toolTip("Kvintakord")
            menuItem("Pause track") { updatePauseMenuItem(playerManager.togglePaused()) }
            menuItem("Skip track") { queueManager.skipTrack() }
            menuItem("Restart track") { playerManager.audioPlayer.playingTrack?.position = 0 }
            separator()
            menu("Loop",
                innerCheckMenuItem("Off") { queueManager.loop = LoopMode.OFF; updateLoopMenuItems(LoopMode.OFF) },
                innerCheckMenuItem("All") { queueManager.loop = LoopMode.ALL; updateLoopMenuItems(LoopMode.ALL) },
                innerCheckMenuItem("Single") { queueManager.loop = LoopMode.SINGLE; updateLoopMenuItems(LoopMode.SINGLE) }
            )
            checkMenuItem("Shuffle") { queueManager.shuffle++; updateShuffleMenuItem(queueManager.shuffle) }
            separator()
            menuItem("Stop") { playerManager.stop() }
            separator()
            addExitMenuItem("Exit")
        }.build()
    }

    fun create() {
        if (!SystemTray.isSupported()) {
            return log.warning("SystemTray is not supported")
        }

        Platform.setImplicitExit(false)
        primaryStage.setOnCloseRequest {
            primaryStage.hide()
        }

        playerManager.audioPlayer.isPausedProperty.onChange {
            updatePauseMenuItem(it)
        }

        queueManager.loopProperty.onChange {
            updateLoopMenuItems(it)
        }

        queueManager.shuffleProperty.onChange {
            updateShuffleMenuItem(it)
        }

        trayIcon.show()

        Executors.scheduledExecutor.execute {
            while (!trayIcon.isShowing) {
                Thread.onSpinWait()
            }

            runLater {
                updatePauseMenuItem(playerManager.audioPlayer.isPaused)
                updateLoopMenuItems(queueManager.loop)
                updateShuffleMenuItem(queueManager.shuffle)
            }
        }
    }

    private fun innerMenuItem(label: String, eventHandler: EventHandler<ActionEvent>): MenuItem {
        return MenuItem(label).apply { onAction = eventHandler }
    }

    private fun innerCheckMenuItem(label: String, eventHandler: EventHandler<ActionEvent>): MenuItem {
        return CheckMenuItem(label).apply { onAction = eventHandler }
    }

    private fun updateLoopMenuItems(value: LoopMode?) {
        for (i in 0..2) {
            ((trayIcon.getMenuItem(4) as? Menu)?.getItem(i) as? CheckboxMenuItem)?.apply { state = label.equals(value?.formatted, true) }
        }
    }

    private fun updateShuffleMenuItem(value: ShuffleMode?) {
        (trayIcon.getMenuItem(5) as? CheckboxMenuItem)?.apply { state = value == ShuffleMode.ON }
    }

    private fun updatePauseMenuItem(value: Boolean?) {
        trayIcon.getMenuItem(0).label = if (value == true) "Play track" else "Pause track"
    }
}