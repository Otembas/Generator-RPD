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
 * @property classroomWorksAll Общее количество часов аудиторных видов работ
 * @property otherContactWorksAll Общее количество часов иных контактных видов работ
 * @property individualWorksAll Общее количество часов самостоятельных видов работ
 * @property contact Общая информация о контактных видах работ
 * @property contact Общая информация о видах работ для контроля
 * @property individual Общая информация о самостоятельных видах работ
 * @property firstSemester Номер первого семестра
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@Suppress("MagicNumber")
class SemestersHours(
    val classroomWorks: List<TypeWork>,
    val otherContactWorks: List<TypeWork>,
    val individualWorks: List<TypeWork>,
) {
    val classroomWorksAll: String =
        prepareHours(toStringHours(classroomWorks.sumOf { work -> getHoursValue(work.all) }))

    val otherContactWorksAll: String =
        prepareHours(toStringHours(otherContactWorks.sumOf { work -> getHoursValue(work.all) }))

    val individualWorksAll: String =
        prepareHours(toStringHours(individualWorks.sumOf { work -> getHoursValue(work.all) }))

    val contact: TypeWork
        get() {
            val typeWorksHours = mutableListOf<Semester>()
            classroom.hoursBySemester.forEachIndexed { index, classroomWork ->
                typeWorksHours.add(
                    index,
                    Semester(
                        classroomWork.number,
                        (
                            getHoursValue(classroomWork.hours) +
                                getHoursValue(otherContact.hoursBySemester.getOrNull(index)?.hours ?: "")
                            ).toString().takeIf { it != "0.0" } ?: ""
                    )
                )
            }
            return TypeWork("Контактная работа", typeWorksHours)
        }

    val control: TypeWork
        get() = TypeWork(
            "Подготовка к экзамену",
            individualWorks.find { it.name == "Подготовка к текущему контролю" }!!.hoursBySemester
        )

    val classroom: TypeWork = getAllWorks("Аудиторные занятия", classroomWorks)

    val otherContact: TypeWork = getAllWorks("Иная контактная работа", otherContactWorks)

    val individual: TypeWork = getAllWorks("Самостоятельная работа", individualWorks)

    val firstSemester = classroom.hours.takeIf { it.isNotEmpty() }?.first()?.number ?: -1

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
        return TypeWork(name, typeWorksHours)
    }
}
