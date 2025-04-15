package ru.redactor.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Настройки приложения.
 *
 * @property semesterOffset Смещение семестра в файле источника
 * @property files Настройки файлов
 *
 * @author Konstantin Rogachev <ghosix7@gmail.com>
 */
@ConfigurationProperties(prefix = "application")
class AppProperties(val semesterOffset: Int, val files: Files) {
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
