package com.anever.school.data.local.dao


import com.anever.school.data.model.*
import kotlinx.datetime.*

object DummyDb {
    private val teacherSmith = Teacher("t1", "Mr. Smith")
    private val teacherLee = Teacher("t2", "Ms. Lee")
    private val teacherKhan = Teacher("t3", "Dr. Khan")

    val subjects = listOf(
        Subject("s1", "Mathematics", "MATH101", teacherSmith, "A-201"),
        Subject("s2", "Physics", "PHY102", teacherLee, "B-105"),
        Subject("s3", "Computer Science", "CS103", teacherKhan, "Lab-2")
    )

    val timetable = listOf(
        TimetableEntry("tt1", 1, LocalTime(9, 0),  LocalTime(9, 50),  "s1", "A-201"),
        TimetableEntry("tt2", 1, LocalTime(10, 0), LocalTime(10, 50), "s2", "B-105"),
        TimetableEntry("tt3", 1, LocalTime(11, 10), LocalTime(12, 0),  "s3", "Lab-2"),
        TimetableEntry("tt4", 2, LocalTime(9, 0),  LocalTime(9, 50),  "s2", "B-105"),
        TimetableEntry("tt5", 2, LocalTime(10, 0), LocalTime(10, 50), "s1", "A-201"),
        TimetableEntry("tt6", 3, LocalTime(9, 0),  LocalTime(9, 50),  "s3", "Lab-2"),
        TimetableEntry("tt7", 4, LocalTime(9, 0),  LocalTime(9, 50),  "s1", "A-201"),
        TimetableEntry("tt8", 5, LocalTime(10, 0), LocalTime(10, 50), "s3", "Lab-2"),
    )

    val assignments = listOf(
        Assignment(
            id = "a1",
            title = "Algebra Worksheet",
            subjectId = "s1",
            dueAt = LocalDateTime(LocalDate(2025, 8, 15), LocalTime(23, 59)),
            description = "Solve 10 problems from chapter 3.",
            attachments = listOf(Attachment("worksheet.pdf", "https://example.com/worksheet.pdf")),
            status = AssignmentStatus.todo
        ),
        Assignment(
            id = "a2",
            title = "Physics Lab Report",
            subjectId = "s2",
            dueAt = LocalDateTime(LocalDate(2025, 8, 18), LocalTime(17, 0)),
            description = "Submit lab report on kinematics.",
            attachments = emptyList(),
            status = AssignmentStatus.submitted
        ),
        Assignment(
            id = "a3",
            title = "Programming Exercise 1",
            subjectId = "s3",
            dueAt = LocalDateTime(LocalDate(2025, 8, 20), LocalTime(23, 59)),
            description = "Arrays & loops task.",
            attachments = emptyList(),
            status = AssignmentStatus.todo
        ),
    )

    val exam = Exam(
        id = "e1",
        term = "Term 1",
        name = "Midterm Exams",
        schedule = listOf(
            ExamSlot("s1", LocalDate(2025, 9, 3), LocalTime(9, 0), LocalTime(10, 30), "Hall-1", "A23"),
            ExamSlot("s2", LocalDate(2025, 9, 5), LocalTime(9, 0), LocalTime(10, 30), "Hall-2", "B11"),
            ExamSlot("s3", LocalDate(2025, 9, 8), LocalTime(9, 0), LocalTime(10, 30), "Hall-1", "C07")
        )
    )

    val notices = listOf(
        Notice("n1", "Class", "CS Lab moved to Lab-3",
            "Tomorrow's CS lab will be in Lab-3.", emptyList(),
            LocalDateTime(2025, 8, 12, 18, 45), "Admin"),
        Notice("n2", "Exams", "Midterm Timetable",
            "Midterm exam schedule has been posted.", emptyList(),
            LocalDateTime(2025, 8, 10, 9, 0), "Examination Cell"),
        Notice("n3", "Events", "Tech Fest Registration",
            "Register by Aug 25.", emptyList(),
            LocalDateTime(2025, 8, 9, 12, 10), "Cultural Committee")
    )
}

class InMemorySubjectDao : SubjectDao {
    override fun getAllSubjects() = DummyDb.subjects
    override fun getSubjectById(id: String) = DummyDb.subjects.find { it.id == id }
}

class InMemoryTimetableDao : TimetableDao {
    override fun getTodaySchedule(dayOfWeek: Int) =
        DummyDb.timetable.filter { it.dayOfWeek == dayOfWeek }.sortedBy { it.start }

    override fun getWeekSchedule(): Map<Int, List<TimetableEntry>> =
        DummyDb.timetable.groupBy { it.dayOfWeek }
            .mapValues { (_, v) -> v.sortedBy { it.start } }
}

class InMemoryAssignmentDao : AssignmentDao {
    override fun getAssignmentsToDo() =
        DummyDb.assignments.filter { it.status == AssignmentStatus.todo }.sortedBy { it.dueAt }

    override fun getAssignmentById(id: String) =
        DummyDb.assignments.find { it.id == id }
}

class InMemoryExamDao(
    private val subjectDao: SubjectDao = InMemorySubjectDao()
) : ExamDao {
    override fun getUpcomingExamSlots(limit: Int): List<ExamSlotExt> {
        val exam = DummyDb.exam
        return exam.schedule
            .sortedWith(compareBy({ it.date }, { it.start }))
            .take(limit)
            .mapNotNull { slot ->
                val subj = subjectDao.getSubjectById(slot.subjectId) ?: return@mapNotNull null
                ExamSlotExt(
                    examName = exam.name,
                    subject = subj,
                    date = slot.date, start = slot.start, end = slot.end,
                    room = slot.room, seatNo = slot.seatNo
                )
            }
    }
}

class InMemoryNoticeDao : NoticeDao {
    override fun getLatestNotices(limit: Int) =
        DummyDb.notices.sortedByDescending { it.postedAt }.take(limit)
}
