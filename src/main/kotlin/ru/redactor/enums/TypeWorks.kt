package ru.redactor.enums

/**
 * Виды работ дисциплины.
 *
 * @property workName Имя работы
 * @property value Значение имени вида работы, подставляемое в файл
 * @property category Категория вида работы
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
enum class TypeWorks(val workName: String, val value: String, val category: TypeWorkCategory) {
    /**
     * Лекционные занятия
     */
    LECTURES("Лек", "Занятия лекционного типа", TypeWorkCategory.CLASSROOM),

    /**
     * Лабораторные занятия
     */
    LABORATORIES("Лаб", "Лабораторные занятия", TypeWorkCategory.CLASSROOM),

    /**
     * Практические занятия
     */
    PRACTICES("Пр", "Практические занятия", TypeWorkCategory.CLASSROOM),

    /**
     * Групповые и индивидуальные консультации по курсовым работам (курсовым проектам)
     */
    CONSULTATIONS(
        "КРП",
        "Групповые и индивидуальные консультации по курсовым работам (курсовым проектам)",
        TypeWorkCategory.CLASSROOM
    ),

    /**
     * Контроль самостоятельной работы (КСР)
     */
    CONTROL_INDIVIDUAL("КСР", "Контроль самостоятельной работы (КСР)", TypeWorkCategory.OTHER_CONTACT),

    /**
     * Промежуточная аттестация (ИКР)
     */
    CERTIFICATIONS("ИКР", "Промежуточная аттестация (ИКР)", TypeWorkCategory.OTHER_CONTACT),

    /**
     * Самостоятельная работа
     */
    INDIVIDUALS("СР", "Самостоятельная работа", TypeWorkCategory.INDIVIDUAL),

    /**
     * Подготовка к текущему контролю
     */
    CONTROL("Контроль", "Подготовка к текущему контролю", TypeWorkCategory.INDIVIDUAL);

    companion object {
        /**
         * Возвращает вид работы по имени.
         *
         * @param workName Имя вида работы
         *
         * @return Вид работы
         */
        @Suppress("TooGenericExceptionCaught")
        fun getTypeWorkByWorkName(workName: String) = TypeWorks.entries.find { it.workName == workName }
    }
}
