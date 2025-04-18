package ru.redactor.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.redactor.models.FileInfo
import ru.redactor.models.Filter
import ru.redactor.models.ReportInfoWithId
import ru.redactor.repositories.DisciplinesRepository
import ru.redactor.services.ReportsService
import java.util.UUID

/**
 * Контроллер запросов, связанных с генерацией файлов из шаблона.
 *
 * @property reportsService Сервис для генерации файлов
 * @property disciplinesRepository Репозиторий для работы с дисциплинами
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@RestController
@RequestMapping("/api/report")
class ReportsController(
    private val reportsService: ReportsService,
    private val disciplinesRepository: DisciplinesRepository
) {
    /**
     * Логгер приложения
     */
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Возвращает список данных о сгенерированных файлах.
     *
     * @param filter Данные фильтра
     *
     * @return Ответ, содержащий список данных о сгенерированных файлах
     */
    @Suppress("TooGenericExceptionCaught")
    @PostMapping("/docx")
    fun generateDocx(@RequestBody filter: Filter): ResponseEntity<List<FileInfo>> {
        val result = try {
            reportsService.generateDocxFile(filter)
        } catch (e: Throwable) {
            logger.error(e.message, e)
            return ResponseEntity.badRequest().body(listOf())
        }
        return ResponseEntity.ok().body(result)
    }

    /**
     * Устанавливает информацию о генерируемых файлах.
     *
     * @param body Информация о генерируемых файлах с идентификатором сессии
     */
    @PostMapping("/info")
    fun loadReportInfo(@RequestBody body: ReportInfoWithId) {
        logger.debug("Loading report info for {}", body.id)
        reportsService.setReportInfo(body.id, body.reportInfo)
    }

    /**
     * Выполняет очистку текущей сессии.
     *
     * @param body Карта с идентификатором сессии
     */
    @Suppress("TooGenericExceptionCaught")
    @PostMapping("/clean")
    fun clean(@RequestBody body: Map<String, UUID>) {
        try {
            logger.info("Cleaning for {}", body["id"])
            reportsService.removeGeneratedFiles(body["id"]!!)
            disciplinesRepository.remove(body["id"]!!)
        } catch (e: Throwable) {
            logger.error(e.message, e)
        }
    }
}
