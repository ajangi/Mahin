package dev.mahin.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class MahinApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<MahinApplication>(*args)
}
