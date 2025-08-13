package com.anever.school.data.local.dao

import com.anever.school.data.model.Assignment
import com.anever.school.data.model.Notice
import com.anever.school.data.model.Subject
import com.anever.school.data.model.TimetableEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime


interface SubjectDao {
    fun getAllSubjects(): List<Subject>
    fun getSubjectById(id: String): Subject?
}

interface TimetableDao {
    fun getTodaySchedule(dayOfWeek: Int): List<TimetableEntry>
    fun getWeekSchedule(): Map<Int, List<TimetableEntry>>
}

interface AssignmentDao {
    fun getAssignmentsToDo(): List<Assignment>
    fun getAssignmentById(id: String): Assignment?
}

interface ExamDao {
    fun getUpcomingExamSlots(limit: Int = 3): List<ExamSlotExt>
}

data class ExamSlotExt(
    val examName: String,
    val subject: Subject,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val room: String,
    val seatNo: String
)

interface NoticeDao {
    fun getLatestNotices(limit: Int = 3): List<Notice>
}
