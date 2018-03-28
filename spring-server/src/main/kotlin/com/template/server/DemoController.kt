package com.template.server

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDateTime

class DemoController(){

    @GetMapping("/date", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun getCurrentDate(): Any{
        return mapOf("date" to LocalDateTime.now().toLocalDate())
    }
}