package org.holo

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.application.*
import io.ktor.server.netty.*
import org.holo.plugins.configureHTTP
import org.holo.plugins.configureMonitoring
import org.holo.plugins.configureRouting
import org.holo.plugins.configureSerialization

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val config = ConfigFactory.load()

    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}