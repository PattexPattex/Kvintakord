package com.pattexpattex.kvintakord.app

import com.pattexpattex.kvintakord.app.tray.TrayIconManager
import com.pattexpattex.kvintakord.app.views.DefaultView
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.find
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

class App : tornadofx.App(DefaultView::class, Style::class) {
    override val configBasePath: Path = Paths.get("./conf")

    override fun start(stage: Stage) {
        stage.apply {
            minWidth = 820.0
            minHeight = 600.0

            getImage()?.let {
                icons.add(Image(it))
            }

            super.start(this)
            find<TrayIconManager>().create()
        }
    }

    private fun getImage(): InputStream? = object {}.javaClass.getResourceAsStream("/icon.png")
}