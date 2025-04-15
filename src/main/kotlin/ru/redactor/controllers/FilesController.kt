package ru.redactor.controllers

import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.redactor.models.FileInfo
import ru.redactor.repositories.DepartmentsRepository
import ru.redactor.repositories.DisciplinesRepository
import java.util.UUID

/**
 * Контроллер запросов, связанных с файлами.
 *
 * @property departmentsRepository Репозиторий для работы с кафедрами
 * @property disciplinesRepository Репозиторий для работы с дисциплинами
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@RestController
@RequestMapping("/api/files")
class FilesController(
    private val departmentsRepository: DepartmentsRepository,
    private val disciplinesRepository: DisciplinesRepository
) {
    /**
     * Загружает список кафедр из файла.
     *
     * @param file Файл со списком кафедр
     */
    @PostMapping("/departments")
    fun loadDepartments(@RequestParam("file") file: MultipartFile) {
        departmentsRepository.loadDepartments(file)
    }

    /**
     * Загружает список дисциплин из файла.
     *
     * @param file Файл с дисциплинами
     * @param id Идентификатор сессии
     */
    @PostMapping("/disciplines")
    fun loadDisciplines(@RequestParam("file") file: MultipartFile, @RequestParam("id") id: UUID) {
        disciplinesRepository.loadDisciplines(id, file)
    }

    /**
     * Возвращает массив байтов файла.
     *
     * @param body Информация о файле
     *
     * @return Ответ, содержащий массив байтов файла
     */
    @Suppress("TooGenericExceptionCaught")
    @PostMapping("/download")
    fun downloadFile(@RequestBody body: FileInfo): ResponseEntity<ByteArray> =
        try {
            val file = ClassPathResource("generated/${body.originalFilename}")
            ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated.docx")
                .body(file.contentAsByteArray)
        } catch (e: Throwable) {
            println(e.message)
            ResponseEntity.badRequest().body(e.message?.toByteArray() ?: ByteArray(0))
        }
}
