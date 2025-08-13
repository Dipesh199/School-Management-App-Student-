package com.anever.school.ui.util

import com.anever.school.data.TodayClass
import kotlinx.datetime.*

fun dayName(dayOfWeek: Int): String =
    DayOfWeek(dayOfWeek).name.lowercase().replaceFirstChar { it.titlecase() } // e.g., Monday

enum class ClassStatus { ONGOING, NEXT, PAST, UPCOMING }

data class ClassWithStatus(val item: TodayClass, val status: ClassStatus)

fun markStatusesForToday(items: List<TodayClass>, now: LocalTime): List<ClassWithStatus> {
    if (items.isEmpty()) return emptyList()
    val sorted = items.sortedBy { it.start }
    var nextMarked = false
    return sorted.map { c ->
        val status = when {
            now >= c.start && now < c.end -> ClassStatus.ONGOING
            now < c.start && !nextMarked -> {
                nextMarked = true
                ClassStatus.NEXT
            }
            now < c.start -> ClassStatus.UPCOMING
            else -> ClassStatus.PAST
        }
        ClassWithStatus(c, status)
    }
}
