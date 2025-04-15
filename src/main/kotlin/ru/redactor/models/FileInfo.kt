package ru.redactor.models

/**
 * Модель информации о файле.
 *
 * @property originalFilename Исходное имя файла
 * @property filename Имя файла для клиента
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
class FileInfo(val originalFilename: String, val filename: String)
