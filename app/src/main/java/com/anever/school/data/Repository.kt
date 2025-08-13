package com.anever.school.data


import com.anever.school.data.local.dao.*
import com.anever.school.data.model.*
import kotlinx.datetime.*

class Repository(
    private val subjectDao: SubjectDao = InMemorySubjectDao(),
    private val timetableDao: TimetableDao = InMemoryTimetableDao(),
    private val assignmentDao: AssignmentDao = InMemoryAssignmentDao(),
    private val examDao: ExamDao = InMemoryExamDao(),
    private val noticeDao: NoticeDao = InMemoryNoticeDao(),
    private val resultDao: ResultDao = InMemoryResultDao()          // ⬅️ NEW
) {

    fun getTodayClasses(today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date): List<TodayClass> {
        val dayOfWeek = today.dayOfWeek.isoDayNumber
        val schedule = timetableDao.getTodaySchedule(dayOfWeek)
        return schedule.mapNotNull { tt ->
            val subj = subjectDao.getSubjectById(tt.subjectId) ?: return@mapNotNull null
            TodayClass(
                id = subj.id,
                subject = subj.name,
                time = "${tt.start} - ${tt.end}",
                room = tt.room,
                teacher = subj.teacher.name,
                start = tt.start,
                end = tt.end,
                dayOfWeek = tt.dayOfWeek
            )
        }
    }

    fun getWeekClasses(): List<DaySchedule> {
        val byDay = timetableDao.getWeekSchedule()
        return (1..7).map { d ->
            val items = byDay[d].orEmpty().mapNotNull { tt ->
                val subj = subjectDao.getSubjectById(tt.subjectId) ?: return@mapNotNull null
                TodayClass(
                    id = subj.id,
                    subject = subj.name,
                    time = "${tt.start} - ${tt.end}",
                    room = tt.room,
                    teacher = subj.teacher.name,
                    start = tt.start,
                    end = tt.end,
                    dayOfWeek = tt.dayOfWeek
                )
            }
            DaySchedule(dayOfWeek = d, classes = items)
        }
    }

    fun getToDoAssignments(): List<Assignment> = assignmentDao.getAssignmentsToDo()
    fun getAllAssignments(): List<Assignment> = assignmentDao.getAllAssignments()
    fun getAllSubjects(): List<Subject> = subjectDao.getAllSubjects()
    fun getUpcomingExams(limit: Int = 3): List<ExamSlotExt> = examDao.getUpcomingExamSlots(limit)

    // ⬅️ NEW
    fun getExams(): List<Exam> = examDao.getAllExams()

    // ⬅️ NEW
    data class ResultRow(val subject: Subject, val marks: Int, val grade: String)

    fun getResultsForExam(examId: String): List<ResultRow> =
        resultDao.getResultsForExam(examId).mapNotNull { r ->
            val subj = subjectDao.getSubjectById(r.subjectId) ?: return@mapNotNull null
            ResultRow(subject = subj, marks = r.marks, grade = r.grade)
        }.sortedBy { it.subject.name }

    // ⬅️ NEW
    fun computeGpa(results: List<ResultRow>): Double {
        if (results.isEmpty()) return 0.0
        val avg = results.map { gradeToPoints(it.grade) }.average()
        return ((avg * 100.0).toInt() / 100.0)  // round to 2 decimals
    }

    private fun gradeToPoints(grade: String): Double = when (grade.uppercase()) {
        "A+" -> 10.0
        "A"  -> 9.0
        "B+" -> 8.0
        "B"  -> 7.0
        "C"  -> 6.0
        "D"  -> 5.0
        "E"  -> 4.0
        else -> 0.0
    }

    fun getSubjectById(id: String) = subjectDao.getSubjectById(id)
    fun getAssignmentById(id: String) = assignmentDao.getAssignmentById(id)
    fun getLatestNotices(limit: Int = 3): List<Notice> = noticeDao.getLatestNotices(limit)

}

data class TodayClass(
    val id: String,
    val subject: String,
    val time: String,
    val room: String,
    val teacher: String,
    val start: LocalTime,
    val end: LocalTime,
    val dayOfWeek: Int
)

data class DaySchedule(
    val dayOfWeek: Int,
    val classes: List<TodayClass>
)

