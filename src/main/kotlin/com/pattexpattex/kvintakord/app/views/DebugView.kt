package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.Style
import javafx.scene.paint.Color
import tornadofx.*

class DebugView : View() {
    override val root = borderpane {
        center = vbox {
            button().action {
                openInternalWindow(escapeClosesWindow = true, view = object : Fragment() {
                    override val root = vbox {
                        Style.Colors.all().forEach {
                            label {
                                text = it.toString()

                                prefWidth = 100.0
                                style {
                                    backgroundColor = multi(it)
                                    textFill = Color.WHITE
                                }
                            }
                        }
                    }
                })
            }
        }
    }
}