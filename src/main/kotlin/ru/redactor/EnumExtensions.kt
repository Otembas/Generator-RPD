package ru.redactor

/**
 * Возвращает имя перечисления в виде имени класса.
 *
 * @return Имя перечисления в виде имени класса
 */
fun <T : Enum<T>> Enum<T>.toClassname(): String {
    var prevChar = '$'
    val sb = StringBuilder()
    for (c in name.lowercase().replaceFirstChar { it.uppercase() }) {
        if (prevChar == '_') {
            sb.append(c.uppercase())
        } else if (c != '_') {
            sb.append(c)
        }
        prevChar = c
    }
    return sb.toString()
}
