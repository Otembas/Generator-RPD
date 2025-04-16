package ru.redactor.enums

/**
 * Простые поля, подставляемые в файл.
 *
 * @property xlsValue Значение в файле источнике
 * @property sheet Лист в файле источнике
 * @property offset Смещение значения поля в файле источнике
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@Suppress("MagicNumber")
enum class SimpleField(
    val xlsValue: List<String>,
    val sheet: String,
    val offset: Int = 0
) {
    /**
     * Квалификация
     */
    QUALIFICATION(listOf("Квалификация"), "Титул"),

    /**
     * Форма обучения
     */
    FORM_EDUCATION(listOf("Форма обучения"), "Титул"),

    /**
     * Факультет
     */
    FACULTY(listOf("Факультет"), "Титул", 1),

    /**
     * Специализация
     */
    SPEC(listOf("Направление подготовки"), "Титул"),

    /**
     * Профиль
     */
    PROFILE(listOf("Программа магистратуры", "Профиль"), "Титул", 1),

    /**
     * Председатель УМК
     */
    CHAIRMAN_UMK(listOf("Председатель УМК"), "Титул", 10)
}
