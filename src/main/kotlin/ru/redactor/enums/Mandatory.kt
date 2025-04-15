package ru.redactor.enums

/**
 * Обязательность дисциплины.
 *
 * @property category Категория обязательности
 * @property value Значение обязательности, подставляемое в файл
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
enum class Mandatory(val category: String, val value: String) {
    /**
     * Обязательная часть
     */
    REQUIRED_PART("Обязательная часть", "обязательной части"),

    /**
     * Часть, формируемая участниками образовательных отношений
     */
    FORMED_PARTICIPANTS(
        "Часть, формируемая участниками образовательных отношений",
        "части, формируемой участниками образовательных отношений"
    );

    companion object {
        /**
         * Возвращает обязательность по категории.
         *
         * @param category Категория
         *
         * @return Обязательность или null
         */
        fun getMandatoryByCategory(category: String): Mandatory? = Mandatory.entries.find { it.category == category }
    }
}
