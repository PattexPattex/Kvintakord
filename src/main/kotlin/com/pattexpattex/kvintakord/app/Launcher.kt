package com.pattexpattex.kvintakord.app

import org.slf4j.bridge.SLF4JBridgeHandler
import tornadofx.launch

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        launch<App>(args)
    }
}