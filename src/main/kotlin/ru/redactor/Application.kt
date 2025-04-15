package ru.redactor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.redactor.properties.AppProperties

/**
 * Главный класс приложения.
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class Application

fun main() {
    runApplication<Application>()
}
