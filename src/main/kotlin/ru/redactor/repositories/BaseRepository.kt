package ru.redactor.repositories

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.UUID

/**
 * Базовый класс репозитория.
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
abstract class BaseRepository {
    /**
     * Загрузка данных из файла.
     *
     * @param file Файл из запроса
     * @param filename Имя файла
     * @param id Идентификатор сессии
     * @param delete Удаление файла
     */
    protected fun load(file: MultipartFile, filename: String, id: UUID? = null, delete: Boolean = true) {
        this::class.java.classLoader.getResource("template.docx")?.path?.substringBeforeLast("/")?.let {
            val newFile = File("$it/${id?.let { id -> "$id - $filename"}}")
            file.transferTo(newFile)
            id?.let { id -> loadEntities(id, newFile) } ?: loadEntities(newFile)
            if (delete) newFile.delete()
        }
    }

    /**
     * Загрузка сущностей репозитория из файла.
     *
     * @param id Идентификатор файла
     * @param file Файл с данными
     */
    protected abstract fun loadEntities(id: UUID, file: File)

    /**
     * Загрузка сущностей репозитория из файла.
     *
     * @param file Файл с данными
     */
    protected abstract fun loadEntities(file: File)
}
