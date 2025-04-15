package ru.redactor.services

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service

/**
 * Сервис для работы с excel файлами.
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@Service
class ExcelService {
    /**
     * Возвращает ячейку по ее значению.
     *
     * @param sheet Лист из представления excel файла
     * @param value Значение ячейки
     * @param strict Строгое совпадение значения ячейки
     *
     * @return Ячейка или null, если не найдена
     */
    fun findCellByValue(sheet: Sheet, value: String, strict: Boolean = false): Cell? {
        sheet.rowIterator().forEach { row ->
            row.cellIterator().forEach { cell ->
                if (checkCellValue(cell, value, strict)) return cell
            }
        }
        return null
    }

    /**
     * Возвращает данные из файла учебного плана.
     *
     * @param workbook Представление excel файла
     *
     * @return Лист, строку и ячейку из представления
     */
    @Suppress("MagicNumber")
    fun getPlanData(workbook: XSSFWorkbook): Triple<XSSFSheet, XSSFRow, Cell?> {
        val sheet = workbook.getSheet("План")
        val cell = findCellByValue(sheet, "Считать в плане")
        return Triple(
            sheet,
            sheet.getRow((cell?.rowIndex ?: 0) + 3),
            cell
        )
    }

    /**
     * Проверка значения ячейки на совпадение с искомым значением.
     *
     * @param cell Ячейка
     * @param value Искомое значение
     * @param strict Строгое совпадение значений
     *
     * @return Результат проверки
     */
    private fun checkCellValue(cell: Cell, value: String, strict: Boolean) =
        if (cell.cellType == CellType.STRING) {
            when (strict) {
                true -> cell.stringCellValue == value
                false -> cell.stringCellValue.contains(value)
            }
        } else {
            false
        }
}
