package ru.redactor.models

import java.time.format.TextStyle
import java.util.Locale

/**
 * Модель дополнительной информации о файле.
 *
 * @property departmentProtocolString Строковое представление протокола утверждения на заседании кафедры
 * @property commissionProtocolString Строковое представление протокола утверждения на учебно-методической комиссии
 *                                    факультета
 * @property year Год формирования РПД
 * @property creators Список составителей программы
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
class ReportInfo(
    departmentProtocol: Protocol? = null,
    commissionProtocol: Protocol? = null,
    val year: Int? = null,
    val creators: List<String>? = null
) {
    companion object {
        /**
         * Строковое представление протокола по умолчанию
         */
        private const val DEFAULT_PROTOCOL_STRING = "Необходимо заполнить вручную"
    }

    val departmentProtocolString: String = getProtocolString(departmentProtocol)

    val commissionProtocolString: String = getProtocolString(commissionProtocol)

    /**
     * Возвращает строковое представление протокола утверждения дисциплины.
     *
     * @param protocol Протокол утверждения
     *
     * @return Строковое представление протокола
     */
    private fun getProtocolString(protocol: Protocol?): String {
        if (protocol == null) return DEFAULT_PROTOCOL_STRING
        val preparedDate = protocol.date.atZoneSameInstant(protocol.timeZone.toZoneId())
        val dateString = "«${preparedDate.dayOfMonth}» " +
            "${preparedDate.month.getDisplayName(TextStyle.FULL, Locale.of("ru"))} " +
            "${preparedDate.year}"
        return "№ ${protocol.number} от $dateString"
    }
}
