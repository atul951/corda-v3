package com.template.server

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
@EnableSwagger2
private class ServerStarter{}

fun main(args: Array<String>){
    val app = SpringApplication(ServerStarter::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.isWebEnvironment = true
    app.run(*args)
}