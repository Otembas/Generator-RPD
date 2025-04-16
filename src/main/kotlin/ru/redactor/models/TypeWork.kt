package ru.redactor.models

import ru.redactor.enums.TypeWorks
import ru.redactor.utils.HoursUtils.prepareHours
import ru.redactor.utils.HoursUtils.toStringHours

/**
 * Модель вида работы.
 *
 * @property name Имя
 * @property hoursBySemester Количество часов по семестрам
 * @property type Тип вида работы
 * @property all Общее количество часов вида работы
 * @property hours Количество часов по семестрам для документа
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
class TypeWork(val name: String, val hoursBySemester: MutableList<Semester>, val type: TypeWorks? = null) {
    val all by lazy {
        prepareHours(toStringHours(hoursBySemester.sumOf { if (it.hours.isBlank()) 0.0 else it.hours.toDouble() }))
    }

    val sortingHours by lazy {
        hoursBySemester.sortedWith { s1, s2 ->
            when {
                s1.hours.isBlank() && s2.hours.isBlank() -> 0
                s1.hours.isBlank() -> 1
                s2.hours.isBlank() -> -1
                else -> s1.number - s2.number
            }
        }
    }

    lateinit var hours: List<Semester>
}
