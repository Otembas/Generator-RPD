package ru.redactor.models

import ru.redactor.utils.HoursUtils.getHoursValue
import ru.redactor.utils.HoursUtils.prepareHours
import ru.redactor.utils.HoursUtils.toStringHours

/**
 * Модель количества часов видов работ по семестрам.
 *
 * @property classroomWorks Аудиторные виды работ
 * @property otherContactWorks Иные контактные виды работ
 * @property individualWorks Самостоятельные виды работ
 * @property unitInHours Количество часов в зачетной единице
 * @property classroomWorksAll Общее количество часов аудиторных видов работ
 * @property otherContactWorksAll Общее количество часов иных контактных видов работ
 * @property individualWorksAll Общее количество часов самостоятельных видов работ
 * @property contact Общая информация о контактных видах работ
 * @property control Общая информация о видах работ для контроля
 * @property individual Общая информация о самостоятельных видах работ
 * @property total Общая трудоемкость видов работ
 * @property totalUnits Общее количество зачетных единиц
 * @property firstSemesterNumber Номер первого семестра
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@Suppress("MagicNumber")
class SemestersHours(
    val classroomWorks: List<TypeWork>,
    val otherContactWorks: List<TypeWork>,
    val individualWorks: List<TypeWork>,
    val unitInHours: Int
) {
    companion object {
        /**
         * Максимальное количество семестров для таблицы для документа
         */
        private const val MAX_SEMESTERS_COUNT = 4
    }

    val classroomWorksAll: String =
        prepareHours(toStringHours(classroomWorks.sumOf { work -> getHoursValue(work.all) }))

    val otherContactWorksAll: String =
        prepareHours(toStringHours(otherContactWorks.sumOf { work -> getHoursValue(work.all) }))

    val individualWorksAll: String =
        prepareHours(toStringHours(individualWorks.sumOf { work -> getHoursValue(work.all) }))

    val contact: TypeWork
        get() = getAllWorks("Контактная работа", classroom, otherContact)

    val control: TypeWork = TypeWork(
        "Подготовка к экзамену",
        individualWorks.find { it.name == "Подготовка к текущему контролю" }!!.hoursBySemester
    )

    val classroom: TypeWork = getAllWorks("Аудиторные занятия", classroomWorks)

    val otherContact: TypeWork = getAllWorks("Иная контактная работа", otherContactWorks)

    val individual: TypeWork = getAllWorks("Самостоятельная работа", individualWorks)

    val total: TypeWork
        get() = getAllWorks("Общая трудоемкость", contact, individual)

    val totalUnits: TypeWork
        get() = TypeWork(
            "Общее количество зачетных единиц",
            total
                .hoursBySemester
                .map { Semester(it.number, prepareHours(toStringHours(getHoursValue(it.hours) / unitInHours))) }
                .toMutableList()
        ).apply {
            hours = sortingHours.take(MAX_SEMESTERS_COUNT)
        }

    val firstSemesterNumber = classroom.sortingHours.firstOrNull()?.number ?: -1

    init {
        prepareAllWorks()
    }

    /**
     * Подготовка всех видов работ для вывода в документе.
     */
    private fun prepareAllWorks() {
        prepareWorks(classroomWorks)
        prepareWorks(otherContactWorks)
        prepareWorks(individualWorks)
        prepareWorks(listOf(control))
    }

    /**
     * Подготовка видов работ определенной категории для вывода в документе.
     *
     * @param typeWorks Список видов работ
     */
    private fun prepareWorks(typeWorks: List<TypeWork>) {
        typeWorks.forEach {
            val firstSemester = it.sortingHours.firstOrNull() ?: return@forEach
            val list = it.sortingHours.toMutableList()
            if (firstSemester.number != firstSemesterNumber) {
                val offset = firstSemester.number - firstSemesterNumber
                for (i in 0..<offset) {
                    list.add(i, Semester(i, ""))
                }
            }
            it.hours = list.take(MAX_SEMESTERS_COUNT)
        }
    }

    /**
     * Возвращает общие данные видов работ.
     *
     * @param name Имя общего вида работы
     * @param works Данные видов работ
     *
     * @return Общее данные видов работ
     */
    private fun getAllWorks(name: String, works: List<TypeWork>): TypeWork {
        val typeWorksHours = mutableListOf<Semester>()
        works.forEach { work ->
            work.hoursBySemester.forEachIndexed { index, semester ->
                val semesterHours = typeWorksHours.getOrNull(index)
                if (semesterHours == null) {
                    typeWorksHours.add(
                        index,
                        Semester(
                            semester.number,
                            prepareHours(toStringHours(getHoursValue(semester.hours)))
                        )
                    )
                } else {
                    typeWorksHours[index] = Semester(
                        semester.number,
                        prepareHours(
                            toStringHours(getHoursValue(semesterHours.hours) + getHoursValue(semester.hours))
                        )
                    )
                }
            }
        }
        return TypeWork(name, typeWorksHours).apply { hours = sortingHours.take(MAX_SEMESTERS_COUNT) }
    }

    /**
     * Возвращает общие данные двух видов работ.
     *
     * @param name Имя общего вида работы
     * @param firstWork Данные вида работ
     * @param secondWork Данные вида работ
     *
     * @return Общее данные видов работ
     */
    private fun getAllWorks(name: String, firstWork: TypeWork, secondWork: TypeWork): TypeWork {
        val typeWorks = mutableListOf<Semester>()
        firstWork.hoursBySemester.forEachIndexed { index, classroomWork ->
            typeWorks.add(
                index,
                Semester(
                    classroomWork.number,
                    (
                        prepareHours(
                            toStringHours(
                                getHoursValue(classroomWork.hours) +
                                    getHoursValue(secondWork.hoursBySemester.getOrNull(index)?.hours ?: "")
                            )
                        )
                        )
                )
            )
        }
        return TypeWork(name, typeWorks).apply { hours = sortingHours.take(MAX_SEMESTERS_COUNT) }
    }
}
