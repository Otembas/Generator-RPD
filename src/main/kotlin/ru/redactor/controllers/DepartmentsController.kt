package ru.redactor.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.redactor.repositories.DepartmentsRepository

/**
 * Контроллер запросов, связанных с кафедрами.
 *
 * @property departmentsRepository Репозиторий для работы с кафедрами
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@RestController
@RequestMapping("/api/departments")
class DepartmentsController(private val departmentsRepository: DepartmentsRepository) {
    /**
     * Возвращает список имен кафедр.
     *
     * @return Список имен кафедр
     */
    @GetMapping("/all")
    fun getDepartments(): List<String> = departmentsRepository.getAllNames()

    /**
     * Возвращает список преподавателей со всех кафедр.
     *
     * @return Список преподавателей
     */
    @GetMapping("/teachers")
    fun getAllDepartmentsTeachers(): Set<String> = departmentsRepository.getAllTeachers()
}
