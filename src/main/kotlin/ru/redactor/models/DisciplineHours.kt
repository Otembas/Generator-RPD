package ru.redactor.models

/**
 * Модель дисциплины с часами по семестрам.
 *
 * @property name Имя
 * @property semestersHours Часы по семестрам
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
class DisciplineHours(val name: String, val semestersHours: SemestersHours)
