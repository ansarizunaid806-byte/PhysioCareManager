package com.physiocare.manager.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val shortDateFormat = SimpleDateFormat("dd MMM", Locale.ENGLISH)
    private val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    fun formatEpoch(epoch: Long): String = dateFormat.format(Date(epoch))
    fun formatEpochShort(epoch: Long): String = shortDateFormat.format(Date(epoch))
    fun formatDay(epoch: Long): String = dayFormat.format(Date(epoch))
    fun formatMonthYear(epoch: Long): String = monthYearFormat.format(Date(epoch))

    fun getStartOfDay(calendar: Calendar = Calendar.getInstance()): Long {
        return calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getEndOfDay(calendar: Calendar = Calendar.getInstance()): Long {
        return calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun getStartOfMonth(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getEndOfMonth(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun getCurrentMonthStart(): Long {
        val cal = Calendar.getInstance()
        return getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun getCurrentMonthEnd(): Long {
        val cal = Calendar.getInstance()
        return getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun getEpochForDate(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getYear(epoch: Long): Int = Calendar.getInstance().apply { timeInMillis = epoch }.get(Calendar.YEAR)
    fun getMonth(epoch: Long): Int = Calendar.getInstance().apply { timeInMillis = epoch }.get(Calendar.MONTH)
    fun getDayOfMonth(epoch: Long): Int = Calendar.getInstance().apply { timeInMillis = epoch }.get(Calendar.DAY_OF_MONTH)
    fun getDayOfWeek(epoch: Long): Int = Calendar.getInstance().apply { timeInMillis = epoch }.get(Calendar.DAY_OF_WEEK)

    fun isToday(epoch: Long): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = epoch }
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }
}

object CurrencyUtils {
    private val formatter = NumberFormat.getInstance(Locale("en", "IN"))

    fun format(amount: Int): String = "₹${formatter.format(amount)}"
    fun format(amount: Long): String = "₹${formatter.format(amount)}"
}

object StringUtils {
    fun formatIndianNumber(number: Int): String {
        val formatter = NumberFormat.getInstance(Locale("en", "IN"))
        return formatter.format(number)
    }
}
