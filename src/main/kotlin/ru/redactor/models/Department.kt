package ru.redactor.models

/**
 * Модель кафедры.
 *
 * @property name Имя
 * @property director Заведующий
 * @property teachers Список преподавателей
 * @property nameForReport Имя кафедры для файла
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
class Department(val name: String, val director: String, val teachers: List<String>) {
    val nameForReport = name.replaceFirstChar { it.lowercase() }
}
