package ru.redactor.repositories

import jakarta.annotation.PreDestroy
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.redactor.models.Department
import ru.redactor.properties.AppProperties
import java.io.File
import java.io.OutputStreamWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Репозиторий кафедр.
 *
 * @property appProperties Настройки приложения
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@Component
class DepartmentsRepository(private val appProperties: AppProperties) : BaseRepository() {
    /**
     * Логгер приложения
     */
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Хранилище кафедр
     */
    private val departments: ConcurrentHashMap<String, Department> = ConcurrentHashMap()

    init {
        val resource = ClassPathResource("departments.txt")
        if (resource.exists()) {
            loadEntities(resource.file)
        }
    }

    /**
     * Возвращает кафедру по имени.
     *
     * @param name Имя
     *
     * @return Объект кафедры
     */
    fun get(name: String) = departments[name.trim().replaceFirstChar { it.lowercase() }]

    /**
     * Возвращает имена всех кафедр.
     *
     * @return Список имен кафедр
     */
    fun getAllNames(): List<String> =
        departments.values.map { it.name.replaceFirstChar { firstChar -> firstChar.uppercase() } }

    /**
     * Возвращает список преподавателей кафедр.
     *
     * @return Список преподавателей
     */
    fun getAllTeachers(): Set<String> = departments.values.map { it.teachers }.flatten().toSet()

    /**
     * Загрузка кафедр.
     *
     * @param file Файл с данными
     */
    @Suppress("TooGenericExceptionCaught")
    fun loadDepartments(file: MultipartFile) {
        try {
            load(
                file,
                if (file.originalFilename?.contains("txt") == true) {
                    appProperties.files.departmentTxtFilename
                } else {
                    appProperties.files.departmentExcelFilename
                }
            )
        } catch (e: Throwable) {
            logger.error(e.message, e)
        }
    }

    /**
     * Загрузка сущностей.
     *
     * @param id Идентификатор сессии
     * @param file Файл с данными
     */
    final override fun loadEntities(id: UUID, file: File) = Unit

    /**
     * Загрузка сущностей.
     *
     * @param file Файл с данными
     */
    final override fun loadEntities(file: File) {
        when (file.extension) {
            "txt" -> loadDepartmentsFromTxt(file)
            "xls", "xlsx" -> loadDepartmentsFromExcel(file)
        }
        logger.info("Departments loaded")
    }

    /**
     * Загрузка кафедр из текстового файла.
     *
     * @param file Файл с данными
     */
    private fun loadDepartmentsFromTxt(file: File) {
        if (file.exists() && file.canRead()) {
            file.bufferedReader().readText().split("\r\n\r\n").forEach { line ->
                if (line.isNotBlank()) {
                    val lineValue = line.trim().split("\r\n")
                    saveDepartment(lineValue[0], lineValue[1], lineValue.subList(1, lineValue.size))
                }
            }
        } else {
            logger.warn("Failed to load departments file: File don't exists or file can't be read")
        }
    }

    /**
     * Загрузка файла из excel файла.
     *
     * @param file Файл с данными
     */
    private fun loadDepartmentsFromExcel(file: File) {
        val workbook = XSSFWorkbook(file)
        val sheet = workbook.getSheetAt(0)
        var rowIndex = 0
        var departmentName = ""
        val teachers = mutableListOf<String>()
        sheet.rowIterator().forEach { row ->
            if (row.getCell(0).stringCellValue.isNotBlank()) {
                if (rowIndex == 0) {
                    departmentName = row.getCell(0).stringCellValue.trim()
                } else {
                    teachers.add(row.getCell(0).stringCellValue.trim())
                }
                rowIndex++
            } else {
                saveDepartment(departmentName, teachers.first(), teachers)
                teachers.clear()
                rowIndex = 0
            }
        }
        saveDepartment(departmentName, teachers.first(), teachers)
    }

    /**
     * Сохранение кафедр в файл перед остановкой приложения.
     */
    @PreDestroy
    fun destroy() {
        this::class.java.classLoader.getResource("template.docx")?.path?.substringBeforeLast("/")?.let {
            logger.info("Saving departments")
            val file = File("$it/departments.txt")
            file.writer().use { writer ->
                departments.entries.forEach { entry -> writeDepartment(writer, entry) }
            }
        }
    }

    /**
     * Запись данных о кафедре в файл.
     *
     * @param writer Объект для записи данных в файл
     * @param entry Запись хранилища с кафедрами
     */
    private fun writeDepartment(writer: OutputStreamWriter, entry: Map.Entry<String, Department>) {
        writer.write("${entry.key}\r\n")
        writer.write("${entry.value.director}\r\n")
        entry.value.teachers.drop(1).forEach { teacher -> writer.write("${teacher}\r\n") }
        writer.write("\r\n")
    }

    /**
     * Сохранение кафедры в хранилище.
     *
     * @param key Ключ
     * @param director Заведующий кафедры
     * @param teachers Список преподавателей кафедры
     */
    private fun saveDepartment(key: String, director: String, teachers: List<String>) {
        logger.debug("Saving department {}", key)
        val departmentName = key.trim().replaceFirstChar { it.lowercase() }
        departments[departmentName] = Department(key.trim(), director.trim(), teachers.toList())
    }
}
