package ru.redactor.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Настройки приложения.
 *
 * @property semesterOffset Смещение семестра в файле источника
 * @property waitingTeachersDelay Задержка ожидания загрузки преподавателей
 * @property defaultReportValue Значение по умолчанию для полей отчета
 * @property files Настройки файлов
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@ConfigurationProperties(prefix = "application")
class AppProperties(
    val semesterOffset: Int,
    val waitingTeachersDelay: Duration,
    val defaultReportValue: String,
    val files: Files
) {
    /**
     * Настройки файлов.
     *
     * @property departmentTxtFilename Имя текстового файла с данными о кафедрах
     * @property departmentExcelFilename Имя excel файла с данными о кафедрах
     * @property planFilename Имя файла с учебным планом
     */
    class Files(
        val departmentTxtFilename: String,
        val departmentExcelFilename: String,
        val planFilename: String
    )
}
