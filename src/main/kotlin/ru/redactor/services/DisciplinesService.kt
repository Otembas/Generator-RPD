package ru.redactor.services

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import ru.redactor.enums.Mandatory
import ru.redactor.enums.TypeWorks
import ru.redactor.models.Department
import ru.redactor.models.Discipline
import ru.redactor.models.DisciplineHours
import ru.redactor.models.Semester
import ru.redactor.models.SemestersHours
import ru.redactor.models.TypeWork
import ru.redactor.properties.AppProperties
import ru.redactor.repositories.DepartmentsRepository
import ru.redactor.repositories.DisciplinesRepository
import java.util.UUID

/**
 * Сервис для работы с дисциплинами.
 *
 * @property appProperties Настройки приложения
 * @property excelService Сервис для работы с excel файлами
 * @property departmentsRepository Репозиторий для работы с кафедрами
 * @property disciplinesRepository Репозиторий для работы с дисциплинами
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@Service
class DisciplinesService(
    private val appProperties: AppProperties,
    private val excelService: ExcelService,
    private val departmentsRepository: DepartmentsRepository,
    private val disciplinesRepository: DisciplinesRepository
) {
    companion object {
        /**
         * Значение по умолчанию для количества часов в зачетной единице
         */
        private const val DEFAULT_UNIT_IN_HOURS = 36
    }

    /**
     * Возвращает список дисциплин из excel файла.
     *
     * @param workbook Представление excel файла
     * @param department Имя кафедры
     *
     * @return Список дисциплин
     */
    @Suppress("MagicNumber", "NestedBlockDepth")
    fun getDisciplines(workbook: XSSFWorkbook, department: String? = null): List<Discipline> {
        var (sheet, row, cell) = excelService.getPlanData(workbook)
        cell ?: return listOf()
        val columnIndex = cell.columnIndex
        var rowIndex = cell.rowIndex + 3
        val disciplines = mutableListOf<Discipline>()
        var currentMandatory = Mandatory.REQUIRED_PART
        val competenciesCell = excelService.findCellByValue(sheet, "Компетенции")!!
        var condition = !row.getCell(columnIndex).stringCellValue.contains("Блок 2") &&
            !row.getCell((columnIndex - 1).takeIf { it >= 0 } ?: 0).stringCellValue.contains("Блок 2")
        while (condition) {
            when (row.getCell(0).stringCellValue) {
                Mandatory.REQUIRED_PART.category -> {
                    currentMandatory = Mandatory.REQUIRED_PART
                }

                Mandatory.FORMED_PARTICIPANTS.category -> {
                    currentMandatory = Mandatory.FORMED_PARTICIPANTS
                }
            }
            when (row.getCell(columnIndex).stringCellValue) {
                "+", "-" -> {
                    fillDiscipline(
                        workbook,
                        sheet,
                        row,
                        competenciesCell,
                        columnIndex,
                        currentMandatory,
                        department
                    )?.let { discipline ->
                        disciplines.add(discipline)
                    }
                }
            }
            rowIndex++
            condition = !row.getCell(columnIndex).stringCellValue.contains("Блок 2") &&
                !row.getCell((columnIndex - 1).takeIf { it >= 0 } ?: 0).stringCellValue.contains("Блок 2")
            row = sheet.getRow(rowIndex)
        }
        return disciplines
    }

    /**
     * Возвращает список дисциплин.
     *
     * @param workbook Представление excel файла
     * @param name Имя дисциплины
     * @param defaultList Список дисциплин по умолчанию
     *
     * @return Список дисциплин
     */
    @Suppress("ReturnCount")
    fun getDisciplines(
        workbook: XSSFWorkbook,
        name: String,
        defaultList: List<Discipline>
    ): List<Discipline> {
        val (sheet, _, cell) = excelService.getPlanData(workbook)
        cell ?: return defaultList
        val columnIndex = cell.columnIndex
        val competenciesCell = excelService.findCellByValue(sheet, "Компетенции")!!
        val disciplineCell = excelService.findCellByValue(sheet, name) ?: return defaultList
        val mandatory = getMandatory(disciplineCell)
        return fillDiscipline(
            workbook,
            sheet,
            disciplineCell.row as XSSFRow,
            competenciesCell,
            columnIndex,
            mandatory
        )
            ?.let { listOf(it) }
            ?: defaultList
    }

    /**
     * Возвращает список имен всех дисциплин.
     *
     * @param id Идентификатор сессии
     *
     * @return Список имен дисциплин
     */
    fun getAllDisciplineNames(id: UUID): List<String> = disciplinesRepository.getAllNames(id)?.toList() ?: listOf()

    /**
     * Возвращает список дисциплин с часами по семестрам.
     *
     * @param workbook Представление excel файла
     *
     * @return Список дисциплин с часами по семестрам
     */
    @Suppress("MagicNumber")
    fun getDisciplinesHours(workbook: XSSFWorkbook): List<DisciplineHours> {
        var (sheet, row, cell) = excelService.getPlanData(workbook)
        cell ?: return listOf()
        val columnIndex = cell.columnIndex
        var rowIndex = cell.rowIndex + 3
        val disciplineHours = mutableListOf<DisciplineHours>()
        var condition = !row.getCell(columnIndex).stringCellValue.contains("Блок 2") &&
            !row.getCell((columnIndex - 1).takeIf { it >= 0 } ?: 0).stringCellValue.contains("Блок 2")
        while (condition) {
            when (row.getCell(columnIndex).stringCellValue) {
                "+", "-" -> {
                    val disciplineNameCell = row.getCell(columnIndex + 2)
                    if (!disciplineNameCell.cellStyle.font.bold) {
                        disciplineHours.add(
                            DisciplineHours(disciplineNameCell.stringCellValue, findSemestersHours(sheet, row))
                        )
                    }
                }
            }
            condition = !row.getCell(columnIndex).stringCellValue.contains("Блок 2") &&
                !row.getCell((columnIndex - 1).takeIf { it >= 0 } ?: 0).stringCellValue.contains("Блок 2")
            rowIndex++
            row = sheet.getRow(rowIndex)
        }
        return disciplineHours
    }

    /**
     * Возвращает обязательности дисциплины.
     *
     * @param disciplineCell Ячейка дисциплины
     *
     * @return Обязательность дисциплины
     */
    private fun getMandatory(disciplineCell: Cell): Mandatory {
        val sheet = disciplineCell.sheet
        var row = disciplineCell.row
        var currentMandatory: Mandatory? = null
        while (currentMandatory == null && row.rowNum > sheet.firstRowNum) {
            row = sheet.getRow(row.rowNum - 1)
            currentMandatory = Mandatory.getMandatoryByCategory(row.getCell(0).stringCellValue)
        }
        return currentMandatory ?: Mandatory.REQUIRED_PART
    }

    /**
     * Заполнение объекта дисциплины.
     *
     * @param workbook Представление excel файла
     * @param sheet Лист из представления excel файла
     * @param row Строка из представления excel файла с данными для заполнения
     * @param competenciesCell Ячейка с компетенциями дисциплины
     * @param columnIndex Индекс столбца строки с данными
     * @param currentMandatory Текущее значение обязательности дисциплины
     * @param departmentFilterValue Значение кафедры из фильтра
     *
     * @return Объект дисциплины или null, если нет необходимых данных в строке
     */
    private fun fillDiscipline(
        workbook: XSSFWorkbook,
        sheet: Sheet,
        row: XSSFRow,
        competenciesCell: Cell,
        columnIndex: Int,
        currentMandatory: Mandatory,
        departmentFilterValue: String? = null
    ): Discipline? =
        row.getCell(columnIndex + 2).let {
            if (!it.cellStyle.font.bold) {
                val department = getDepartment(row)
                if (departmentFilterValue != null && department.name != departmentFilterValue) return@let null
                val competences = getCompetences(workbook, row, competenciesCell)
                val unitsCell = excelService.findCellByValue(sheet, "з.е.")!!
                val hoursCell = excelService.findCellByValue(sheet, "Итого акад.часов")!!
                val semestersHours = findSemestersHours(sheet, row)
                Discipline(
                    row.getCell(columnIndex + 1).stringCellValue,
                    it.stringCellValue,
                    currentMandatory.value,
                    competences,
                    row.getCell(unitsCell.columnIndex).stringCellValue.toIntOrNull() ?: 0,
                    row.getCell(hoursCell.columnIndex).stringCellValue.toIntOrNull() ?: 0,
                    semestersHours,
                    department
                )
            } else {
                null
            }
        }

    /**
     * Возвращает список компетенций дисциплины.
     *
     * @param workbook Представление excel файла
     * @param row Строка из представления excel файла с данными
     * @param competenciesCell Ячейка с компетенциями дисциплины
     *
     * @return Список компетенций дисциплины
     */
    private fun getCompetences(workbook: XSSFWorkbook, row: XSSFRow, competenciesCell: Cell): List<String> {
        val competences = row.getCell(competenciesCell.columnIndex).stringCellValue.split(";")
        val competenceSheet = workbook.getSheet("Компетенции")
        val contentCell = excelService.findCellByValue(competenceSheet, "Содержание")!!
        return competences.mapNotNull { competence ->
            val competenceName = competence.trim().let { c ->
                if (c.first() == 'И') c.drop(1) else c
            }
            excelService.findCellByValue(competenceSheet, competenceName, true)?.let { cell ->
                "${cell.stringCellValue} " +
                    cell.row.getCell(contentCell.columnIndex).stringCellValue
            }
        }
    }

    /**
     * Возвращает часы дисциплины по семестрам.
     *
     * @param sheet Лист из представления excel файла
     * @param disciplineRow Строка с данными дисциплины
     *
     * @return Часы дисциплины по семестрам
     */
    @Suppress("MagicNumber")
    private fun findSemestersHours(sheet: Sheet, disciplineRow: Row): SemestersHours {
        val firstCourseCell = excelService.findCellByValue(sheet, "Курс 1")!!
        var currentCell = sheet.getRow(firstCourseCell.rowIndex + 1).getCell(firstCourseCell.columnIndex)
        val typeWorks = mutableListOf<TypeWork>()
        var i = 1
        while (currentCell.stringCellValue.contains("Семестр")) {
            val typeWorkNameRow = sheet.getRow(currentCell.rowIndex + 1)
            val hoursBySemester = fillHoursBySemester(currentCell.columnIndex, disciplineRow, typeWorkNameRow)
            hoursBySemester.entries.forEach { entry ->
                val typeWork: TypeWorks? = TypeWorks.getTypeWorkByWorkName(entry.key.replace(" ", ""))
                typeWorks
                    .find { it.name == (typeWork?.value ?: entry.key) }
                    ?.hoursBySemester
                    ?.add(Semester(i, entry.value))
                    ?: typeWorks.add(
                        TypeWork(typeWork?.value ?: entry.key, mutableListOf(Semester(i, entry.value)), typeWork)
                    )
            }
            i++
            currentCell = currentCell.row.getCell(currentCell.columnIndex + appProperties.semesterOffset)
        }
        val unitInHours = excelService.findCellByValue(
            firstCourseCell.sheet.workbook.getSheet("Нормы"),
            "Академических часов в одной зачетной единице трудоемкости (з.е.)"
        )
        return SemestersHours(
            typeWorks.filter { it.type?.category == ru.redactor.enums.TypeWorkCategory.CLASSROOM },
            typeWorks.filter { it.type?.category == ru.redactor.enums.TypeWorkCategory.OTHER_CONTACT },
            typeWorks.filter { it.type?.category == ru.redactor.enums.TypeWorkCategory.INDIVIDUAL },
            unitInHours?.let { it.row.getCell(it.columnIndex + 5).stringCellValue.toIntOrNull() }
                ?: DEFAULT_UNIT_IN_HOURS
        )
    }

    /**
     * Заполнение количества часов по семестрам.
     *
     * @param semesterColumn Номер столбца семестра
     * @param disciplineRow Строка с данными дисциплины
     * @param typeWorkNamesRow Строка с именем вида работы
     */
    private fun fillHoursBySemester(
        semesterColumn: Int,
        disciplineRow: Row,
        typeWorkNamesRow: Row
    ): Map<String, String> {
        val typeWorksWithHours = mutableMapOf<String, String>()
        for (i in 0..<appProperties.semesterOffset) {
            val name = typeWorkNamesRow.getCell(semesterColumn + i).stringCellValue
            val value = disciplineRow.getCell(semesterColumn + i).stringCellValue
            typeWorksWithHours[name] = value
        }
        return typeWorksWithHours
    }

    /**
     * Возвращает кафедру дисциплины.
     *
     * @param disciplineRow Строка с данными о дисциплине
     *
     * @return Кафедра
     */
    private fun getDepartment(disciplineRow: Row): Department {
        val disciplineDepartmentNumberCell =
            excelService.findCellByValue(disciplineRow.sheet, "Закрепленная кафедра")!!
        val sheet = disciplineRow.sheet.workbook.getSheet("Кафедры")
        val fixedDepartmentCell = excelService.findCellByValue(
            sheet,
            disciplineRow.getCell(disciplineDepartmentNumberCell.columnIndex).stringCellValue
        )!!
        val fixedDepartmentNameCell = excelService.findCellByValue(sheet, "Название кафедры")!!
        val department = departmentsRepository.get(
            fixedDepartmentCell.row.getCell(fixedDepartmentNameCell.columnIndex).stringCellValue
        ) ?: Department("", "", listOf())
        return department
    }
}
