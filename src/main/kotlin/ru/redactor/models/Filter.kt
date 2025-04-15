package ru.redactor.models

import java.util.UUID

/**
 * Модель фильтра.
 *
 * @property id Идентификатор сессии
 * @property department Кафедра
 * @property discipline Дисциплина
 * @property getAll Генерация всех дисциплин
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
data class Filter(
    val id: UUID,
    val department: String? = null,
    val discipline: String? = null,
    val getAll: Boolean = false
)
