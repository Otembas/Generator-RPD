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
    val xlsValue: String,
    val sheet: String,
    val offset: Int = 0
) {
    /**
     * Квалификация
     */
    QUALIFICATION("Квалификация", "Титул"),

    /**
     * Форма обучения
     */
    FORM_EDUCATION("Форма обучения", "Титул"),

    /**
     * Факультет
     */
    FACULTY("Факультет", "Титул", 1),

    /**
     * Специализация
     */
    SPEC("Направление подготовки", "Титул"),

    /**
     * Профиль
     */
    PROFILE("Программа магистратуры", "Титул", 1),

    /**
     * Председатель УМК
     */
    CHAIRMAN_UMK("Председатель УМК", "Титул", 10)
}
