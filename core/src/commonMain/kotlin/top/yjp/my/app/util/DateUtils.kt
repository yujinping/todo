package top.yjp.my.app.util

import kotlinx.datetime.*

object DateUtils {
    fun today(): String {
        val now = kotlinx.datetime.Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        return now.toLocalDateTime(tz).date.toString() // YYYY-MM-DD
    }

    fun startOfWeek(date: String): String {
        val ld = LocalDate.parse(date)
        val dayOfWeek = ld.dayOfWeek.ordinal // Monday=0, Sunday=6
        val start = ld.minus(dayOfWeek, DateTimeUnit.DAY)
        return start.toString()
    }

    fun endOfWeek(date: String): String {
        val ld = LocalDate.parse(date)
        val dayOfWeek = ld.dayOfWeek.ordinal
        val end = ld.plus(6 - dayOfWeek, DateTimeUnit.DAY)
        return end.toString()
    }

    fun startOfMonth(date: String): String {
        val ld = LocalDate.parse(date)
        return LocalDate(ld.year, ld.monthNumber, 1).toString()
    }

    fun endOfMonth(date: String): String {
        val ld = LocalDate.parse(date)
        val lastDay = when (ld.monthNumber) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (ld.year % 4 == 0 && (ld.year % 100 != 0 || ld.year % 400 == 0)) 29 else 28
            else -> 30
        }
        return LocalDate(ld.year, ld.monthNumber, lastDay).toString()
    }

    fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> 30
        }
    }

    fun dayOfWeekLabel(ordinal: Int): String {
        return when (ordinal) {
            0 -> "一"
            1 -> "二"
            2 -> "三"
            3 -> "四"
            4 -> "五"
            5 -> "六"
            6 -> "日"
            else -> ""
        }
    }

    fun monthLabel(month: Int): String {
        return "${month}月"
    }

    fun formatDisplayDate(date: String): String {
        val ld = LocalDate.parse(date)
        return "${ld.year}年${ld.monthNumber}月${ld.dayOfMonth}日"
    }

    fun formatMonthYear(year: Int, month: Int): String {
        return "${year}年${month}月"
    }

    fun isDateToday(date: String): Boolean {
        return date == today()
    }

    fun getWeekDates(date: String): List<String> {
        val start = startOfWeek(date)
        val ld = LocalDate.parse(start)
        return (0..6).map { ld.plus(it, DateTimeUnit.DAY).toString() }
    }

    fun getMonthDates(year: Int, month: Int): List<String> {
        val days = daysInMonth(year, month)
        return (1..days).map { LocalDate(year, month, it).toString() }
    }
}
