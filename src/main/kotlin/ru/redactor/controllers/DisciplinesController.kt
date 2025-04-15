package ru.redactor.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.redactor.services.DisciplinesService
import java.util.UUID

/**
 * Контроллер запросов, связанных с дисциплинами.
 *
 * @property disciplinesService Сервис для работы с дисциплинами
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@RestController
@RequestMapping("/api/disciplines")
class DisciplinesController(val disciplinesService: DisciplinesService) {
    /**
     * Возвращает список имен дисциплин.
     *
     * @param id Идентификатор сессии
     *
     * @return Список имен дисциплин
     */
    @PostMapping("/names")
    fun getDisciplineNames(@RequestParam("id") id: UUID): ResponseEntity<List<String>> =
        ResponseEntity.ok(disciplinesService.getAllDisciplineNames(id))
}
