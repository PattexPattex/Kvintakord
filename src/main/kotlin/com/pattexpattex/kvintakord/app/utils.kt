package com.pattexpattex.kvintakord.app

import com.pattexpattex.kvintakord.app.views.DefaultView
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.Util
import tornadofx.find
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun openUrl(url: String) {
    find<DefaultView>().hostServices.showDocument(url)
}

@OptIn(ExperimentalTime::class)
fun <T> logTime(description: String = "Action", log: Logger = SLF4J(Util.getCallingClass().kotlin), block: () -> T): T =
    measureTimedValue(block).also { log.info("$description required ${it.duration}") }.value

object SLF4J {
    operator fun getValue(thisValue: Any?, kProperty: KProperty<*>): Logger = LoggerFactory.getLogger(thisValue!!::class.java)!!
    operator fun get(klass: KClass<*>) = lazy { LoggerFactory.getLogger(klass.java) }
    operator fun get(name: String) = lazy { LoggerFactory.getLogger(name) }
    operator fun invoke(klass: KClass<*>): Logger = LoggerFactory.getLogger(klass.java)
    operator fun invoke(name: String): Logger = LoggerFactory.getLogger(name)
}

class LimitedHashSet<T>(private val limit: Int = 10) : AbstractMutableSet<T>() {
    private val set = HashSet<T>()

    private fun checkSize() {
        while (set.size > limit) {
            set.remove(set.last())
        }
    }

    override fun add(element: T): Boolean = set.add(element).also { checkSize() }

    override val size get() = set.size

    override fun iterator(): MutableIterator<T> = set.iterator()
}

class ObjectPropertyWrapper<T>(private val get: () -> T, private val set: (T) -> Unit) : SimpleObjectProperty<T>() {
    override fun get(): T {
        return get.invoke().also { super.get() }
    }

    override fun set(newValue: T) {
        set.invoke(newValue)
        super.set(newValue)
    }

    fun setLazy(newValue: T) {
        super.set(newValue)
    }
}
