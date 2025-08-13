package com.anever.school.data.local.dao


import com.anever.school.data.model.*
import kotlinx.datetime.*
import kotlin.random.Random

object DummyDb {
    private val teacherSmith = Teacher("t1", "Mr. Smith")
    private val teacherLee   = Teacher("t2", "Ms. Lee")
    private val teacherKhan  = Teacher("t3", "Dr. Khan")

    val subjects = listOf(
        Subject("s1", "Mathematics", "MATH101", teacherSmith, "A-201"),
        Subject("s2", "Physics",     "PHY102",  teacherLee,   "B-105"),
        Subject("s3", "Computer Science", "CS103", teacherKhan, "Lab-2")
    )

    // dayOfWeek: 1=Mon ... 7=Sun
    val timetable = listOf(
        TimetableEntry("tt1", 1, LocalTime(9, 0),  LocalTime(9, 50),  "s1", "A-201"),
        TimetableEntry("tt2", 1, LocalTime(10, 0), LocalTime(10, 50), "s2", "B-105"),
        TimetableEntry("tt3", 1, LocalTime(11, 10),LocalTime(12, 0),  "s3", "Lab-2"),
        TimetableEntry("tt4", 2, LocalTime(9, 0),  LocalTime(9, 50),  "s2", "B-105"),
        TimetableEntry("tt5", 2, LocalTime(10, 0), LocalTime(10, 50), "s1", "A-201"),
        TimetableEntry("tt6", 3, LocalTime(9, 0),  LocalTime(9, 50),  "s3", "Lab-2"),
        TimetableEntry("tt7", 4, LocalTime(9, 0),  LocalTime(9, 50),  "s1", "A-201"),
        TimetableEntry("tt8", 5, LocalTime(10, 0), LocalTime(10, 50), "s3", "Lab-2"),
    )

    val assignments = listOf(
        Assignment("a1", "Algebra Worksheet", "s1",
            LocalDateTime(LocalDate(2025, 8, 15), LocalTime(23, 59)),
            "Solve 10 problems from chapter 3.",
            listOf(Attachment("worksheet.pdf","https://example.com/worksheet.pdf")),
            AssignmentStatus.todo
        ),
        Assignment("a2", "Physics Lab Report", "s2",
            LocalDateTime(LocalDate(2025, 8, 18), LocalTime(17, 0)),
            "Submit lab report on kinematics.",
            emptyList(),
            AssignmentStatus.submitted
        ),
        Assignment("a3", "Programming Exercise 1", "s3",
            LocalDateTime(LocalDate(2025, 8, 20), LocalTime(23, 59)),
            "Arrays & loops task.", emptyList(), AssignmentStatus.todo
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

    // ⬅️ NEW: sample results for e1
    val results = listOf(
        ExamResult("r1","e1","s1", 88, "A"),
        ExamResult("r2","e1","s2", 76, "B+"),
        ExamResult("r3","e1","s3", 92, "A+")
    )

    val notices = listOf(
        Notice("n1","Class","CS Lab moved to Lab-3","Tomorrow's CS lab will be in Lab-3.", emptyList(),
            LocalDateTime(2025, 8, 12, 18, 45),"Admin"),
        Notice("n2","Exams","Midterm Timetable","Midterm exam schedule has been posted.", emptyList(),
            LocalDateTime(2025, 8, 10, 9, 0),"Examination Cell"),
        Notice("n3","Events","Tech Fest Registration","Register by Aug 25.", emptyList(),
            LocalDateTime(2025, 8, 9, 12, 10),"Cultural Committee")
    )

    val attendance: List<Attendance> by lazy {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val start = today.minus(DatePeriod(days = 44))
        val days = generateSequence(start) { it.plus(DatePeriod(days = 1)) }
            .takeWhile { it <= today }
            .toList()

        val subs = subjects.map { it.id }
        val rnd = Random(42)
        val list = mutableListOf<Attendance>()
        var idCounter = 1
        for (d in days) {
            // Skip Sundays (no classes)
            if (d.dayOfWeek == DayOfWeek.SUNDAY) continue
            subs.forEach { sid ->
                val r = rnd.nextInt(100)
                val status = when {
                    r < 80 -> AttendanceStatus.Present   // 80%
                    r < 90 -> AttendanceStatus.Late      // 10%
                    else   -> AttendanceStatus.Absent    // 10%
                }
                list += Attendance(
                    id = "att${idCounter++}",
                    date = d,
                    subjectId = sid,
                    status = status
                )
            }
        }
        list
    }

    // ⬇️ NEW: requests (mutable in-memory)
    val requests: MutableList<Request> = mutableListOf()
}

class InMemorySubjectDao : SubjectDao {
    override fun getAllSubjects() = DummyDb.subjects
    override fun getSubjectById(id: String) = DummyDb.subjects.find { it.id == id }
}

class InMemoryTimetableDao : TimetableDao {
    override fun getTodaySchedule(dayOfWeek: Int) =
        DummyDb.timetable.filter { it.dayOfWeek == dayOfWeek }.sortedBy { it.start }
    override fun getWeekSchedule(): Map<Int, List<TimetableEntry>> =
        DummyDb.timetable.groupBy { it.dayOfWeek }.mapValues { (_, v) -> v.sortedBy { it.start } }
}

class InMemoryAssignmentDao : AssignmentDao {
    override fun getAllAssignments() = DummyDb.assignments.sortedBy { it.dueAt }
    override fun getAssignmentsToDo() = DummyDb.assignments.filter { it.status == AssignmentStatus.todo }.sortedBy { it.dueAt }
    override fun getAssignmentById(id: String) = DummyDb.assignments.find { it.id == id }
}

class InMemoryExamDao(
    private val subjectDao: SubjectDao = InMemorySubjectDao()
) : ExamDao {
    override fun getUpcomingExamSlots(limit: Int): List<ExamSlotExt> {
        val exam = DummyDb.exam
        return exam.schedule.sortedWith(compareBy({ it.date }, { it.start })).take(limit).mapNotNull { slot ->
            val subj = subjectDao.getSubjectById(slot.subjectId) ?: return@mapNotNull null
            ExamSlotExt(examName = exam.name, subject = subj,
                date = slot.date, start = slot.start, end = slot.end,
                room = slot.room, seatNo = slot.seatNo)
        }
    }
    override fun getAllExams(): List<Exam> = listOf(DummyDb.exam)
}

// ⬅️ NEW
class InMemoryResultDao : ResultDao {
    override fun getResultsForExam(examId: String): List<ExamResult> =
        DummyDb.results.filter { it.examId == examId }
    override fun getAllResults(): List<ExamResult> = DummyDb.results
}

class InMemoryNoticeDao : NoticeDao {
    override fun getLatestNotices(limit: Int) =
        DummyDb.notices.sortedByDescending { it.postedAt }.take(limit)
}

class InMemoryAttendanceDao : AttendanceDao {
    override fun getAllAttendance(): List<Attendance> = DummyDb.attendance

    override fun getAttendanceBetween(start: LocalDate, end: LocalDate): List<Attendance> =
        DummyDb.attendance.filter { it.date >= start && it.date <= end }
}

class InMemoryRequestDao : RequestDao {
    override fun addRequest(request: Request) {
        DummyDb.requests.add(request)
    }
    override fun getAllRequests(): List<Request> = DummyDb.requests
}