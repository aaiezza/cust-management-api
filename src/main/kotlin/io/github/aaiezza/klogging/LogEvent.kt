package io.github.aaiezza.klogging

import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import java.util.*

data class EventId(val uuid: UUID)

sealed class LogEvent {
    abstract val eventId: EventId
    abstract val action: String
    abstract val data: Map<String, Any>

    companion object
}

abstract class BaseLogEvent private constructor(
    override val eventId: EventId = EventId(UUID.randomUUID()),
    override val data: Map<String, Any>,
) : LogEvent() {
    constructor(builderAction: (MutableMap<String, Any>) -> Unit) : this(data = {
        val dataMap = mutableMapOf<String, Any>()
        builderAction(dataMap)
        dataMap
    }())

    override val action: String
        get() = this::class.simpleName ?: "UnknownAction"
}

private val logger = KotlinLogging.logger {}

private fun LogEvent.log(logger: (message: () -> Any?) -> Unit) {
    MDC.put("event_id", eventId.toString())
    MDC.put("action", action)
    val stack = Thread.currentThread().stackTrace
    MDC.put("logger_name", "${stack[2].className}.${stack[2].methodName}")
    data.entries.forEach { MDC.put(it.key, it.value.toString()) }
    try {
        logger { "" }
    } finally {
        MDC.remove("event_id")
        MDC.remove("action")
        MDC.remove("logger_name")
        data.forEach { MDC.remove(it.key) }
    }
}

fun LogEvent.info() = log(logger::info)
fun LogEvent.error() = log(logger::error)
fun LogEvent.warn() = log(logger::warn)
fun LogEvent.debug() = log(logger::debug)
fun LogEvent.trace() = log(logger::trace)
