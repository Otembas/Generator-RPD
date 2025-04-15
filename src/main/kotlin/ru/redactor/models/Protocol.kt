package ru.redactor.models

import java.time.OffsetDateTime
import java.util.TimeZone

/**
 * Модель протокола утверждения дисциплины.
 *
 * @property number Номер
 * @property date Дата
 * @property timeZone Часовая зона
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
class Protocol(val number: Int, val date: OffsetDateTime, val timeZone: TimeZone)
