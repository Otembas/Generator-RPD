package ru.redactor.utils

/**
 * Объект с методами для работы с количеством часов вида работы.
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
object HoursUtils {
    /**
     * Возвращает числовое значение часов вида работы.
     *
     * @param hours Строковое представление часов вида работы
     *
     * @return Числовое значение часов вида работы
     */
    fun getHoursValue(hours: String): Double = if (hours.isBlank()) 0.0 else hours.toDouble()

    /**
     * Подготовка количества часов для клиента.
     *
     * @param hours Исходное значение количества часов
     *
     * @return Преобразованное количество часов
     */
    fun prepareHours(hours: String): String {
        var str = ""
        str += if (hours.takeLast(2) == ".0") {
            hours.replace(".0", "")
        } else {
            hours
        }
        return if (str == "0") "" else str
    }

    /**
     * Возвращает строковое представление количества часов.
     *
     * @param hours Числовое значение количества часов
     *
     * @return Строковое представление количества часов
     */
    @Suppress("MagicNumber")
    fun toStringHours(hours: Double) = ((hours * 10).toInt() / 10.0).toString()
}
