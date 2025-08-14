package com.anever.school.data.local.dao

import com.anever.school.data.model.Assignment
import com.anever.school.data.model.Attendance
import com.anever.school.data.model.Exam
import com.anever.school.data.model.ExamResult
import com.anever.school.data.model.Notice
import com.anever.school.data.model.Request
import com.anever.school.data.model.Route
import com.anever.school.data.model.Subject
import com.anever.school.data.model.TimetableEntry
import kotlinx.datetime.LocalDate

interface SubjectDao {
    fun getAllSubjects(): List<Subject>
    fun getSubjectById(id: String): Subject?
}

interface TimetableDao {
    fun getTodaySchedule(dayOfWeek: Int): List<TimetableEntry>
    fun getWeekSchedule(): Map<Int, List<TimetableEntry>>
}

interface AssignmentDao {
    fun getAllAssignments(): List<Assignment>
    fun getAssignmentsToDo(): List<Assignment>
    fun getAssignmentById(id: String): Assignment?
}

interface ExamDao {
    fun getUpcomingExamSlots(limit: Int = 3): List<ExamSlotExt>
    fun getAllExams(): List<Exam>
}

data class ExamSlotExt(
    val examName: String,
    val subject: Subject,
    val date: kotlinx.datetime.LocalDate,
    val start: kotlinx.datetime.LocalTime,
    val end: kotlinx.datetime.LocalTime,
    val room: String,
    val seatNo: String
)

interface NoticeDao {
    fun getLatestNotices(limit: Int = 3): List<Notice>
    fun getAllNotices(): List<Notice>
}

interface ResultDao {
    fun getResultsForExam(examId: String): List<ExamResult>
    fun getAllResults(): List<ExamResult>
}

interface AttendanceDao {
    fun getAllAttendance(): List<Attendance>
    fun getAttendanceBetween(start: LocalDate, end: LocalDate): List<Attendance>
}

interface RequestDao {
    fun addRequest(request: Request)
    fun getAllRequests(): List<Request>
}

interface TransportDao {
    fun getRoute(): Route
    fun updateStudentStop(stopId: String): Route
}
