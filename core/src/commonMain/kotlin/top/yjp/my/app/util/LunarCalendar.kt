package top.yjp.my.app.util

/**
 * 农历计算工具：公历转农历、天干地支、生肖、节假日判断
 * 纯 Kotlin 实现，不依赖平台 API，适用于 KMP commonMain
 *
 * 数据范围：1900-2100
 * 数据编码：每个农历年用一个 Int 表示
 *   - 低 4 位 (0-3)：闰月月份 (0=无闰月)
 *   - 位 4-15：正月到十二月的大小月 (1=大月30天, 0=小月29天)
 *   - 位 16-27：如有闰月，表示闰月之后各月的大小月
 */
object LunarCalendar {

    /** 农历日期 */
    data class LunarDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean
    )

    /** 农历完整信息（用于 UI 展示） */
    data class LunarInfo(
        val lunarDate: LunarDate,
        val yearName: String,       // 天干地支年，如"甲辰"
        val zodiac: String,          // 生肖，如"龙"
        val monthName: String,       // 农历月名，如"正月"
        val dayName: String,         // 农历日名，如"初一"
        val holiday: String?,        // 节假日名称，非节假日为 null
        val isHoliday: Boolean,      // 是否为节假日
        val isLunarHoliday: Boolean  // 节假日是否为农历日期
    )

    // ──────────────────────────────────────────────
    // 农历数据表 1900-2100
    // 每个 Int 编码一个农历年的月大小信息
    // ──────────────────────────────────────────────
    private val lunarYearData = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, // 1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, // 1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, // 1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, // 1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, // 1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, // 1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, // 1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, // 1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, // 1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0, // 1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, // 2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, // 2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, // 2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, // 2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, // 2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06aa0, 0x1a6c4, 0x0aae0, // 2050-2059
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, // 2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, // 2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, // 2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a4d0, 0x0d150, 0x0f252, // 2090-2099
        0x0d520  // 2100
    )

    // 天干
    private val heavenlyStems = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    // 地支
    private val earthlyBranches = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    // 生肖
    private val zodiacs = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    // 农历月名
    private val monthNames = arrayOf(
        "", "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )

    // 农历日名（前20天为前缀+数字）
    private val dayPrefix = arrayOf(
        "", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    // 基准日期：1900年1月31日 = 农历1900年正月初一
    private const val BASE_YEAR = 1900
    private const val BASE_MONTH = 1
    private const val BASE_DAY = 31

    /** 将公历日期转换为农历信息 */
    fun solarToLunar(solarYear: Int, solarMonth: Int, solarDay: Int): LunarInfo {
        val offset = daysBetween(BASE_YEAR, BASE_MONTH, BASE_DAY, solarYear, solarMonth, solarDay)

        // 查找农历年份
        var lunarYear = BASE_YEAR
        var daysLeft = offset
        var yearDays: Int
        while (true) {
            yearDays = lunarYearDays(lunarYear)
            if (daysLeft < yearDays) break
            daysLeft -= yearDays
            lunarYear++
        }

        // 查找农历月份和日期
        val leapMonth = getLeapMonth(lunarYear)
        var lunarMonth = 1
        var isLeap = false

        for (m in 1..12) {
            val monthDays = lunarMonthDays(lunarYear, m, false)
            if (daysLeft < monthDays) break
            daysLeft -= monthDays
            lunarMonth++

            // 检查闰月
            if (leapMonth > 0 && m == leapMonth) {
                val leapDays = lunarMonthDays(lunarYear, m, true)
                if (daysLeft < leapDays) {
                    lunarMonth = m  // 修正：闰月时 lunarMonth 应为 m 而非 m+1
                    isLeap = true
                    break
                }
                daysLeft -= leapDays
            }
        }

        val lunarDay = daysLeft + 1
        val lunarDate = LunarDate(lunarYear, lunarMonth, lunarDay, isLeap)

        // 天干地支年
        val stemIndex = (lunarYear - 4) % 10
        val branchIndex = (lunarYear - 4) % 12
        val yearName = heavenlyStems[if (stemIndex >= 0) stemIndex else stemIndex + 10] +
                earthlyBranches[if (branchIndex >= 0) branchIndex else branchIndex + 12]
        val zodiac = zodiacs[if (branchIndex >= 0) branchIndex else branchIndex + 12]

        // 月名
        val monthName = if (isLeap) "闰${monthNames[lunarMonth]}" else monthNames[lunarMonth]

        // 日名
        val dayName = if (lunarDay in 1..30) dayPrefix[lunarDay] else "$lunarDay"

        // 节假日
        val holidayInfo = getHoliday(solarYear, solarMonth, solarDay, lunarMonth, lunarDay, isLeap, lunarYear)

        return LunarInfo(
            lunarDate = lunarDate,
            yearName = yearName,
            zodiac = zodiac,
            monthName = monthName,
            dayName = dayName,
            holiday = holidayInfo?.first,
            isHoliday = holidayInfo != null,
            isLunarHoliday = holidayInfo?.second ?: false
        )
    }

    /** 为月视图返回简洁的农历显示文本 */
    fun lunarDisplayText(solarYear: Int, solarMonth: Int, solarDay: Int): String {
        val info = solarToLunar(solarYear, solarMonth, solarDay)

        // 节假日优先显示节假日名称
        if (info.isHoliday) {
            return info.holiday ?: info.dayName
        }

        // 初一显示月名
        return if (info.lunarDate.day == 1) {
            info.monthName
        } else {
            info.dayName
        }
    }

    // ══════════════════════════════════════════════
    // 内部计算方法
    // ══════════════════════════════════════════════

    /** 计算两个公历日期之间的天数差 */
    private fun daysBetween(
        y1: Int, m1: Int, d1: Int,
        y2: Int, m2: Int, d2: Int
    ): Int {
        return dateToDays(y2, m2, d2) - dateToDays(y1, m1, d1)
    }

    /** 公历日期转累计天数（基于 0001-01-01） */
    private fun dateToDays(year: Int, month: Int, day: Int): Int {
        var y = year - 1
        var days = y * 365 + y / 4 - y / 100 + y / 400
        for (m in 1 until month) {
            days += solarMonthDays(year, m)
        }
        return days + day - 1
    }

    /** 公历月份天数 */
    private fun solarMonthDays(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isSolarLeapYear(year)) 29 else 28
            else -> 0
        }
    }

    private fun isSolarLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /** 获取农历年的总天数 */
    private fun lunarYearDays(lunarYear: Int): Int {
        var days = 0
        val data = getLunarYearData(lunarYear)
        for (m in 1..12) {
            days += lunarMonthDays(lunarYear, m, false)
        }
        val leap = getLeapMonth(lunarYear)
        if (leap > 0) {
            days += lunarMonthDays(lunarYear, leap, true)
        }
        return days
    }

    /** 获取农历月天数（29 或 30） */
    private fun lunarMonthDays(lunarYear: Int, month: Int, isLeap: Boolean): Int {
        val data = getLunarYearData(lunarYear)
        return if (isLeap) {
            if ((data shr 16) and (1 shl (month - 1)) != 0) 30 else 29
        } else {
            if ((data shr 4) and (1 shl (month - 1)) != 0) 30 else 29
        }
    }

    /** 获取闰月月份（0 表示无闰月） */
    private fun getLeapMonth(lunarYear: Int): Int {
        return getLunarYearData(lunarYear) and 0x0f
    }

    /** 获取农历年数据 */
    private fun getLunarYearData(lunarYear: Int): Int {
        return if (lunarYear in BASE_YEAR..BASE_YEAR + lunarYearData.lastIndex) {
            lunarYearData[lunarYear - BASE_YEAR]
        } else {
            0
        }
    }

    // ══════════════════════════════════════════════
    // 节假日判断
    // ══════════════════════════════════════════════

    /**
     * 判断节假日
     * @return Pair(节日名称, 是否农历节日) 或 null
     */
    private fun getHoliday(
        solarYear: Int, solarMonth: Int, solarDay: Int,
        lunarMonth: Int, lunarDay: Int, isLeapMonth: Boolean,
        lunarYear: Int
    ): Pair<String, Boolean>? {

        // ── 公历节日 ──
        when {
            solarMonth == 1 && solarDay == 1 -> return "元旦" to false
            solarMonth == 2 && solarDay == 14 -> return "情人节" to false
            solarMonth == 3 && solarDay == 8 -> return "妇女节" to false
            solarMonth == 3 && solarDay == 12 -> return "植树节" to false
            solarMonth == 4 && solarDay == 1 -> return "愚人节" to false
            solarMonth == 5 && solarDay == 1 -> return "劳动节" to false
            solarMonth == 5 && solarDay == 4 -> return "青年节" to false
            solarMonth == 6 && solarDay == 1 -> return "儿童节" to false
            solarMonth == 7 && solarDay == 1 -> return "建党节" to false
            solarMonth == 8 && solarDay == 1 -> return "建军节" to false
            solarMonth == 9 && solarDay == 10 -> return "教师节" to false
            solarMonth == 10 && solarDay == 1 -> return "国庆节" to false
            solarMonth == 12 && solarDay == 25 -> return "圣诞节" to false
        }

        // ── 清明节（公历 4月4日 或 4月5日）──
        if (solarMonth == 4 && (solarDay == 4 || solarDay == 5)) {
            val qingming = getQingmingDay(solarYear)
            if (solarDay == qingming) return "清明节" to false
        }

        // ── 农历节日 ──
        if (!isLeapMonth) {
            when {
                lunarMonth == 1 && lunarDay == 1 -> return "春节" to true
                lunarMonth == 1 && lunarDay == 15 -> return "元宵节" to true
                lunarMonth == 5 && lunarDay == 5 -> return "端午节" to true
                lunarMonth == 7 && lunarDay == 7 -> return "七夕" to true
                lunarMonth == 7 && lunarDay == 15 -> return "中元节" to true
                lunarMonth == 8 && lunarDay == 15 -> return "中秋节" to true
                lunarMonth == 9 && lunarDay == 9 -> return "重阳节" to true
                lunarMonth == 12 && lunarDay == 30 -> return "除夕" to true
            }
            // 腊月二十九也可能是除夕（小月）
            if (lunarMonth == 12 && lunarDay == 29) {
                val decDays = lunarMonthDays(lunarYear, 12, false)
                if (decDays == 29) return "除夕" to true
            }
        }

        // ── 母亲节（5月第二个星期日）──
        if (solarMonth == 5) {
            val motherDay = getNthWeekdayOfMonth(solarYear, 5, 0, 2) // Sunday=0, 2nd
            if (solarDay == motherDay) return "母亲节" to false
        }

        // ── 父亲节（6月第三个星期日）──
        if (solarMonth == 6) {
            val fatherDay = getNthWeekdayOfMonth(solarYear, 6, 0, 3)
            if (solarDay == fatherDay) return "父亲节" to false
        }

        return null
    }

    /** 计算清明节日期（4月4日或5日） */
    private fun getQingmingDay(year: Int): Int {
        // 清明 = 4月4日或5日，根据公式：(Y*D + C) - L
        // 简化：取 4月5日，世纪交替时可能为 4日
        return if (year % 4 == 0 && year % 100 != 0 || year == 2000) 4 else 5
    }

    /** 获取某月第 n 个星期几的日期 */
    private fun getNthWeekdayOfMonth(year: Int, month: Int, weekday: Int, n: Int): Int {
        // weekday: 0=Sunday, 1=Monday, ..., 6=Saturday
        val firstDayOfWeek = dayOfWeek(year, month, 1) // 0=Sunday
        val firstTargetDay = 1 + ((weekday - firstDayOfWeek + 7) % 7)
        return firstTargetDay + (n - 1) * 7
    }

    /** 计算某天是星期几 (0=Sunday, 1=Monday, ..., 6=Saturday) */
    private fun dayOfWeek(year: Int, month: Int, day: Int): Int {
        val totalDays = dateToDays(year, month, day)
        // 0001-01-01 是星期一
        return ((totalDays + 1) % 7)
    }
}
