package com.anever.school.data.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

data class Student(
    val id: String,
    val name: String,
    val avatar: String?,
    val rollNo: String,
    val classId: String,
    val section: String,
    val guardians: List<String>,
    val phone: String,
    val email: String,
    val healthNotes: String?,
    val busStopId: String?
)

data class Subject(
    val id: String,
    val name: String,
    val code: String,
    val teacher: Teacher,
    val room: String
)

data class Teacher(val id: String, val name: String)

data class TimetableEntry(
    val id: String,
    val dayOfWeek: Int, // 1..7
    val start: LocalTime,
    val end: LocalTime,
    val subjectId: String,
    val room: String
)

enum class AttendanceStatus { Present, Absent, Late }

data class Attendance(
    val id: String,
    val date: LocalDate,
    val subjectId: String?,
    val status: AttendanceStatus
)

enum class AssignmentStatus { todo, submitted, graded }

data class Attachment(val name: String, val url: String)

data class Assignment(
    val id: String,
    val title: String,
    val subjectId: String,
    val dueAt: LocalDateTime,
    val description: String,
    val attachments: List<Attachment>,
    val status: AssignmentStatus,
    val grade: String? = null,
    val feedback: String? = null
)

data class Exam(
    val id: String,
    val term: String,
    val name: String,
    val schedule: List<ExamSlot>
)

data class ExamSlot(
    val subjectId: String,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val room: String,
    val seatNo: String
)

data class Notice(
    val id: String,
    val category: String,
    val title: String,
    val body: String,
    val attachments: List<Attachment>,
    val postedAt: LocalDateTime,
    val from: String
)

data class ExamResult(
    val id: String,
    val examId: String,
    val subjectId: String,
    val marks: Int,
    val grade: String
)

