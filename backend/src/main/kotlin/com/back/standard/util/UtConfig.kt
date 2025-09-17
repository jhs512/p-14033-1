package com.back.standard.util

import org.apache.tika.Tika
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UtConfig {
    @Bean
    fun tika(): Tika {
        return Tika()
    }
}