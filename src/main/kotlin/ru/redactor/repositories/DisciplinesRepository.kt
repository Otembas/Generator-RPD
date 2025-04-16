package ru.redactor.repositories

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.redactor.properties.AppProperties
import ru.redactor.services.ExcelService
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Репозиторий для работы с дисциплинами.
 *
 * @property appProperties Настройки приложения
 * @property excelService Сервис для работы с excel файлов
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@Component
class DisciplinesRepository(
    private val appProperties: AppProperties,
    private val excelService: ExcelService
) : BaseRepository() {
    /**
     * Логгер приложения
     */
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Хранилище дисциплин
     */
    private val disciplines = ConcurrentHashMap<UUID, DisciplineNames>()

    /**
     * Загрузка дисциплин из файла.
     *
     * @param id Идентификатор сессии
     * @param file Файл с данными
     */
    @Suppress("TooGenericExceptionCaught")
    fun loadDisciplines(id: UUID, file: MultipartFile) {
        try {
            load(file, appProperties.files.planFilename, id, false)
        } catch (e: Throwable) {
            logger.error(e.message)
        }
    }

    /**
     * Загрузка сущностей дисциплин.
     *
     * @param id Идентификатор сессии
     * @param file Файл с данными
     */
    @Suppress("MagicNumber", "NestedBlockDepth")
    override fun loadEntities(id: UUID, file: File) {
        disciplines[id] = DisciplineNames(file.name)
        val workbook = XSSFWorkbook(file)
        var (sheet, row, cell) = excelService.getPlanData(workbook)
        cell ?: return
        val columnIndex = cell.columnIndex
        var rowIndex = cell.rowIndex + 3
        var condition = !row.getCell(columnIndex).stringCellValue.contains("Блок 2") &&
            !row.getCell((columnIndex - 1).takeIf { it >= 0 } ?: 0).stringCellValue.contains("Блок 2")
        while (condition) {
            when (row.getCell(columnIndex).stringCellValue) {
                "+", "-" -> {
                    row.getCell(columnIndex + 2).let {
                        if (!it.cellStyle.font.bold) {
                            disciplines[id]?.names?.add(it.stringCellValue)
                        }
                    }
                }
            }
            rowIndex++
            row = sheet.getRow(rowIndex)
            condition = !row.getCell(columnIndex).stringCellValue.contains("Блок 2") &&
                !row.getCell((columnIndex - 1).takeIf { it >= 0 } ?: 0).stringCellValue.contains("Блок 2")
        }
    }

    /**
     * Загрузка дисциплин из файла.
     *
     * @param file Файл с данными
     */
    override fun loadEntities(file: File) = Unit

    /**
     * Возвращает список имен дисциплин.
     *
     * @param id Идентификатор сессии
     *
     * @return Список имен дисциплин
     */
    fun getAllNames(id: UUID) = disciplines[id]?.names

    /**
     * Удаляет дисциплины по идентификатору сессии.
     *
     * @param id Идентификатор сессии
     */
    fun remove(id: UUID) {
        logger.debug("Removing disciplines by {}", id)
        disciplines.remove(id)
    }

    /**
     * Класс для хранения имен дисциплин из файла.
     *
     * @property filename Имя файла
     * @property names Имена дисциплин
     */
    private class DisciplineNames(val filename: String, val names: MutableList<String> = mutableListOf())
}
