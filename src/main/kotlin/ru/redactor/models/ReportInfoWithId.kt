package ru.redactor.models

import java.util.UUID

/**
 * Модель дополнительной информации о файле с идентификатором сессии.
 *
 * @property id Идентификатор сессии
 * @property reportInfo Дополнительная информация о файле
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
class ReportInfoWithId(val id: UUID, val reportInfo: ReportInfo)
