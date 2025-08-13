package com.anever.school.data


import com.anever.school.data.local.dao.*
import com.anever.school.data.model.*
import kotlinx.datetime.*

class Repository(
    private val subjectDao: SubjectDao = InMemorySubjectDao(),
    private val timetableDao: TimetableDao = InMemoryTimetableDao(),
    private val assignmentDao: AssignmentDao = InMemoryAssignmentDao(),
    private val examDao: ExamDao = InMemoryExamDao(),
    private val noticeDao: NoticeDao = InMemoryNoticeDao()
) {

    fun getTodayClasses(today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date): List<TodayClass> {
        val dayOfWeek = today.dayOfWeek.isoDayNumber // 1..7
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
    fun getUpcomingExams(limit: Int = 3): List<ExamSlotExt> = examDao.getUpcomingExamSlots(limit)
    fun getLatestNotices(limit: Int = 3): List<Notice> = noticeDao.getLatestNotices(limit)
    fun getSubjectById(id: String) = subjectDao.getSubjectById(id)
    fun getAssignmentById(id: String) = assignmentDao.getAssignmentById(id)
    fun getAllAssignments(): List<Assignment> = assignmentDao.getAllAssignments()
    fun getAllSubjects(): List<Subject> = subjectDao.getAllSubjects()

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
    val dayOfWeek: Int,               // 1..7
    val classes: List<TodayClass>
)
