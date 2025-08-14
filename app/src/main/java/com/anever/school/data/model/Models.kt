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

data class Request(
    val id: String,
    val type: String, // "Leave"
    val reason: String,
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val status: String, // "Pending" | "Approved" | "Rejected"
    val createdAt: LocalDateTime
)

data class BusStop(
    val id: String,
    val name: String,
    val pickup: LocalTime,
    val drop: LocalTime
)

data class Route(
    val id: String,
    val name: String,
    val busNo: String,
    val driverName: String,
    val driverPhone: String,
    val stops: List<BusStop>,
    val studentStopId: String
)

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val copies: Int,
    val available: Int
)

enum class LoanStatus { Reserved, Current, Returned }


data class Loan(
    val id: String,
    val bookId: String,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val status: LoanStatus,
    val renewals: Int = 0 // number of renews used
)

data class Event(
    val id: String,
    val title: String,
    val category: String, // "Fest" | "Seminar" | "Workshop"
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val venue: String,
    val description: String,
    val capacity: Int
)

data class EventPass(
    val id: String,
    val eventId: String,
    val code: String,
    val status: String, // "Active" | "Cancelled"
    val issuedAt: LocalDateTime
)
