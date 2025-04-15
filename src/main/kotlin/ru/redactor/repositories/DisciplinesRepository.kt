package ru.redactor.repositories

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.redactor.properties.AppProperties
import ru.redactor.services.ExcelService
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class DisciplinesRepository(
    private val appProperties: AppProperties,
    private val excelService: ExcelService
) : BaseRepository() {
    private val disciplines = ConcurrentHashMap<UUID, DisciplineNames>()

    @Suppress("TooGenericExceptionCaught")
    fun loadDisciplines(id: UUID, file: MultipartFile) {
        try {
            load(file, appProperties.files.planFilename, id, false)
        } catch (e: Throwable) {
            println(e.message)
        }
    }

    @Suppress("MagicNumber", "NestedBlockDepth")
    override fun loadEntities(id: UUID, file: File) {
        disciplines[id] = DisciplineNames(file.name)
        val workbook = XSSFWorkbook(file)
        var (sheet, row, cell) = excelService.getPlanData(workbook)
        cell ?: return
        val columnIndex = cell.columnIndex
        var rowIndex = cell.rowIndex + 3
        while (!row.getCell(columnIndex).stringCellValue.contains("Блок 2")) {
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
        }
    }

    override fun loadEntities(file: File) = Unit

    fun getAllNames(id: UUID) = disciplines[id]?.names

    fun remove(id: UUID) {
        disciplines.remove(id)
    }

    private class DisciplineNames(val filename: String, val names: MutableList<String> = mutableListOf())
}
