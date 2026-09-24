package dev.mahin.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MahinApplication

fun main(args: Array<String>) {
    runApplication<MahinApplication>(*args)
}
