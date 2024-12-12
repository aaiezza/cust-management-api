package io.github.aaiezza.custman.helloworld

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {

    private val logger: Logger = LoggerFactory.getLogger(HelloWorldController::class.java)

    @GetMapping("/")
    fun sayHello(): String {
        logger.info("Hello endpoint was called")
        return "Hello, World!"
    }
}
