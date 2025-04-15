package ru.redactor.exceptions

/**
 * Исключение, выбрасываемое при некорректном значении фильтра.
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
class InvalidFilterValuesException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
