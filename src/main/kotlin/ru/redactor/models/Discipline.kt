package ru.redactor.models

/**
 * Модель дисциплины.
 *
 * @property code Код
 * @property name Имя
 * @property mandatory Обязательность
 * @property competencies Список компетенций
 * @property totalUnits Общее количество зачетных единиц
 * @property totalHours Общее количество часов
 * @property semestersHours Часы по семестрам
 * @property department Кафедра
 * @property previousDisciplines Список предшествующих дисциплин
 * @property reportInfo Информация генерируемого файла
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
data class Discipline(
    val code: String,
    val name: String,
    val mandatory: String,
    val competencies: List<String>,
    val totalUnits: Int,
    val totalHours: Int,
    val semestersHours: SemestersHours,
    val department: Department
) {
    lateinit var previousDisciplines: String

    lateinit var reportInfo: ReportInfo

    /**
     * Возвращает количество часов вида работы из списка видов работ по его имени.
     *
     * @param typeWorks Список видов работы
     * @param name Имя вида работы
     *
     * @return Количество часов вида работы
     */
    fun getTypeWork(typeWorks: List<TypeWork>, name: String): String =
        typeWorks.find { it.name == name }?.all ?: ""
}
