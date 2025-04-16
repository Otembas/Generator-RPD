package ru.redactor.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.redactor.properties.AppProperties
import ru.redactor.repositories.DepartmentsRepository

/**
 * Контроллер запросов, связанных с кафедрами.
 *
 * @property appProperties Настройки приложения
 * @property departmentsRepository Репозиторий для работы с кафедрами
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@RestController
@RequestMapping("/api/departments")
class DepartmentsController(
    private val appProperties: AppProperties,
    private val departmentsRepository: DepartmentsRepository
) {
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
    fun getAllDepartmentsTeachers(): Set<String> {
        var teachers = departmentsRepository.getAllTeachers()
        if (teachers.isEmpty()) {
            Thread.sleep(appProperties.waitingTeachersDelay)
            teachers = departmentsRepository.getAllTeachers()
        }
        return teachers
    }
}
