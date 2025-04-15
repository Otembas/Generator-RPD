package ru.redactor.services

import fr.opensagres.xdocreport.document.registry.XDocReportRegistry
import fr.opensagres.xdocreport.template.TemplateEngineKind
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import ru.redactor.enums.SimpleField
import ru.redactor.exceptions.InvalidFilterValuesException
import ru.redactor.models.Discipline
import ru.redactor.models.FileInfo
import ru.redactor.models.Filter
import ru.redactor.models.ReportInfo
import ru.redactor.properties.AppProperties
import ru.redactor.toClassname
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Сервис для работы генерируемыми документами.
 *
 * @property appProperties Настройки приложения
 * @property disciplinesService Сервис для работы с дисциплинами
 * @property excelService Сервис для работы с excel файлами
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@Service
class ReportsService(
    private val appProperties: AppProperties,
    private val disciplinesService: DisciplinesService,
    private val excelService: ExcelService
) {
    companion object {
        /**
         * Имя файла шаблона
         */
        private const val TEMPLATE_FILENAME = "template.docx"
    }

    init {
        this::class.java.classLoader.getResource("template.docx")!!.path.substringBeforeLast("/").let {
            val generatedDir = File("$it/generated/")
            if (!generatedDir.exists()) generatedDir.mkdirs()
        }
    }

    /**
     * Дополнительная информация об генерируемом файле
     */
    private val info = ConcurrentHashMap<UUID, ReportInfo>()

    /**
     * Установка дополнительной информации.
     *
     * @param id Идентификатор сессии
     * @param reportInfo Дополнительная информация
     */
    fun setReportInfo(id: UUID, reportInfo: ReportInfo) {
        this.info[id] = reportInfo
    }

    /**
     * Генерация файлов.
     *
     * @param filter Объект фильтра
     *
     * @return Список с информацией о сгенерированных файлов
     */
    @Suppress("LongMethod")
    fun generateDocxFile(filter: Filter): List<FileInfo> {
        val report = XDocReportRegistry.getRegistry().loadReport(
            this.javaClass.classLoader.getResourceAsStream(TEMPLATE_FILENAME),
            TemplateEngineKind.Velocity
        )
        val context = report.createContext()
        val planFile =
            this::class.java.classLoader.getResourceAsStream("${filter.id} - ${appProperties.files.planFilename}")
        val workbook = XSSFWorkbook(planFile)
        val disciplines = getDisciplines(filter, workbook)
        SimpleField.entries.forEach {
            val sheet = workbook.getSheet(it.sheet)
            val cell = excelService.findCellByValue(sheet, it.xlsValue)
            if (cell != null) {
                context.put(
                    it.toClassname(),
                    cell.row
                        .getCell(cell.columnIndex + it.offset)
                        .stringCellValue
                        .substringAfter(it.xlsValue)
                        .replace(":", "")
                        .replace("/", "")
                        .trim()
                )
            } else {
                context.put(
                    it.toClassname(),
                    "Необходимо заполнить вручную"
                )
            }
        }
        val fileInfo = mutableListOf<FileInfo>()
        disciplines.forEach { discipline ->
            val currentDiscipline = discipline.apply {
                previousDisciplines = disciplinesService.getDisciplinesHours(workbook).mapNotNull {
                    if (semestersHours.firstSemester > it.semestersHours.firstSemester && name != it.name) {
                        it.name
                    } else {
                        null
                    }
                }
                reportInfo = info[filter.id]!!
            }
            val metadata = FieldsMetadata()
            context.put("Discipline", currentDiscipline)
            metadata.addFieldAsList("competences.Name")
            report.fieldsMetadata = metadata
            context.put("competences", currentDiscipline.competencies)
            context.put(
                "Semesters",
                currentDiscipline.semestersHours.contact.hours.map {
                    if (it.hours.isBlank()) "" else it.number.toString()
                }
            )
            val file = saveFile("${filter.id} - ${currentDiscipline.code} ${currentDiscipline.name}.docx")
            report.process(context, FileOutputStream(file))
            fileInfo.add(FileInfo(file.name, file.name.substringAfter("${filter.id} - ").replace(".docx", "")))
        }
        planFile?.close()
        return fileInfo
    }

    /**
     * Удаление сгенерированных файлов.
     *
     * @param id Идентификатор сессии
     */
    fun removeGeneratedFiles(id: UUID) {
        val generatedDir = ClassPathResource("generated").file
        if (generatedDir.exists() && generatedDir.isDirectory) {
            generatedDir.listFiles()?.forEach { file ->
                if (file.name.contains(id.toString())) file.delete()
            }
        }
        File(generatedDir.path.substringBeforeLast("\\")).listFiles()?.forEach { file ->
            if (file.name.contains(id.toString())) file.delete()
        }
    }

    /**
     * Возвращает список дисциплин на основе данных фильтра.
     *
     * @param filter Объект фильтра
     * @param workbook Представление excel файла
     *
     * @return Список дисциплин
     */
    private fun getDisciplines(filter: Filter, workbook: XSSFWorkbook): List<Discipline> =
        when {
            filter.getAll -> {
                disciplinesService.getDisciplines(workbook, filter.department)
            }

            !filter.getAll && filter.department != null -> {
                disciplinesService.getDisciplines(workbook, filter.department)
            }

            filter.discipline != null -> {
                disciplinesService.getDisciplines(workbook, filter.discipline, listOf())
            }

            else -> throw InvalidFilterValuesException("Filter has invalid values $filter")
        }

    /**
     * Сохранение сгенерированного файла.
     *
     * @param filename Имя файла
     *
     * @return Файл
     */
    private fun saveFile(filename: String): File =
        this::class.java.classLoader.getResource("template.docx")!!.path.substringBeforeLast("/").let {
            File("$it/generated/$filename")
        }
}
