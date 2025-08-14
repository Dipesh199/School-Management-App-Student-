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

    private val routeStops = listOf(
        BusStop("st1", "Rathausplatz",      LocalTime(7, 30), LocalTime(16, 50)),
        BusStop("st2", "Bahnhof Ost",       LocalTime(7, 40), LocalTime(16, 40)),
        BusStop("st3", "Lindenstraße",      LocalTime(7, 50), LocalTime(16, 30)),
        BusStop("st4", "Campus Süd Tor",    LocalTime(8,  0), LocalTime(16, 20)),
        BusStop("st5", "Technopark",        LocalTime(8, 10), LocalTime(16, 10)),
        BusStop("st6", "School Main Gate",  LocalTime(8, 20), LocalTime(16,  0)),
    )

    var route: Route = Route(
        id = "r1",
        name = "East Line",
        busNo = "B12",
        driverName = "R. Meier",
        driverPhone = "+49 171 2345678",
        stops = routeStops,
        studentStopId = "st3" // default for current student
    )

    var books: MutableList<Book> = mutableListOf(
        Book("b1", "Clean Code", "Robert C. Martin", "9780132350884", copies = 5, available = 2),
        Book("b2", "Kotlin in Action", "Dmitry Jemerov", "9781617293290", copies = 4, available = 1),
        Book("b3", "Android Programming", "Bill Phillips", "9780135245125", copies = 6, available = 4),
        Book("b4", "Design Patterns", "Erich Gamma", "9780201633610", copies = 3, available = 0),
        Book("b5", "The Pragmatic Programmer", "Andrew Hunt", "9780201616224", copies = 5, available = 5),
        Book("b6", "Introduction to Algorithms", "Cormen", "9780262033848", copies = 2, available = 1),
    )

    // a couple of loans (one overdue)
    var loans: MutableList<Loan> = mutableListOf(
        Loan(
            id = "l1", bookId = "b1",
            issueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(DatePeriod(days = 10)),
            dueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(DatePeriod(days = 4)),
            status = LoanStatus.Current, renewals = 0
        ),
        Loan(
            id = "l2", bookId = "b2",
            issueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(DatePeriod(days = 25)),
            dueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(DatePeriod(days = 11)),
            status = LoanStatus.Current, renewals = 1
        )
    )

    // ⬇️ NEW: Events dataset
    val events: MutableList<Event> = mutableListOf(
        Event(
            id = "ev1",
            title = "Tech Fest 2025",
            category = "Fest",
            date = LocalDate(2025, 9, 14),
            start = LocalTime(10, 0),
            end = LocalTime(18, 0),
            venue = "Main Ground",
            description = "Project expo, hack mini, and robotics demo.",
            capacity = 200
        ),
        Event(
            id = "ev2",
            title = "AI for Everyone",
            category = "Seminar",
            date = LocalDate(2025, 9, 22),
            start = LocalTime(14, 0),
            end = LocalTime(16, 0),
            venue = "Auditorium A",
            description = "Intro to practical AI applications.",
            capacity = 120
        ),
        Event(
            id = "ev3",
            title = "Design Thinking Workshop",
            category = "Workshop",
            date = LocalDate(2025, 10, 3),
            start = LocalTime(9, 30),
            end = LocalTime(12, 30),
            venue = "Innovation Lab",
            description = "Hands-on session with templates and teams.",
            capacity = 60
        )
    )

    // ⬇️ NEW: issued passes
    val passes: MutableList<EventPass> = mutableListOf()
}

object NoticeBookmarks {
    val ids: MutableSet<String> = mutableSetOf()
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

class InMemoryResultDao : ResultDao {
    override fun getResultsForExam(examId: String): List<ExamResult> =
        DummyDb.results.filter { it.examId == examId }
    override fun getAllResults(): List<ExamResult> = DummyDb.results
}

class InMemoryNoticeDao : NoticeDao {
    override fun getLatestNotices(limit: Int) =
        DummyDb.notices.sortedByDescending { it.postedAt }.take(limit)

    override fun getAllNotices(): List<Notice> =
        DummyDb.notices.sortedByDescending { it.postedAt }
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

class InMemoryTransportDao : TransportDao {
    override fun getRoute(): Route = DummyDb.route
    override fun updateStudentStop(stopId: String): Route {
        DummyDb.route = DummyDb.route.copy(studentStopId = stopId)
        return DummyDb.route
    }
}

class InMemoryLibraryDao : LibraryDao {
    override fun getAllBooks(): List<Book> = DummyDb.books.toList()
    override fun searchBooks(query: String): List<Book> {
        if (query.isBlank()) return getAllBooks()
        val q = query.lowercase()
        return DummyDb.books.filter { it.title.lowercase().contains(q) || it.author.lowercase().contains(q) }
    }
    override fun getLoans(): List<Loan> = DummyDb.loans.toList()
    override fun putLoan(loan: Loan) {
        val idx = DummyDb.loans.indexOfFirst { it.id == loan.id }
        if (idx >= 0) DummyDb.loans[idx] = loan else DummyDb.loans.add(loan)
    }
    override fun updateBook(updated: Book) {
        val idx = DummyDb.books.indexOfFirst { it.id == updated.id }
        if (idx >= 0) DummyDb.books[idx] = updated
    }
    override fun deleteLoan(loanId: String) {      // ⬅️ NEW
        DummyDb.loans.removeAll { it.id == loanId }
    }
}

// ⬇️ NEW: Event DAO
class InMemoryEventDao : EventDao {
    override fun getAllEvents(): List<Event> = DummyDb.events.sortedWith(
        compareBy<Event> { it.date }.thenBy { it.start }
    )

    override fun getEventById(id: String): Event? = DummyDb.events.firstOrNull { it.id == id }
}

// ⬇️ NEW: Pass DAO
class InMemoryPassDao : PassDao {
    override fun getPasses(): List<EventPass> = DummyDb.passes.toList()

    override fun issuePass(eventId: String): EventPass {
        val code = "PASS-" + eventId.uppercase() + "-" + System.currentTimeMillis().toString().takeLast(4)
        val pass = EventPass(
            id = "p${System.currentTimeMillis()}",
            eventId = eventId,
            code = code,
            status = "Active",
            issuedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
        DummyDb.passes.add(pass)
        return pass
    }

    override fun cancelPass(passId: String) {
        DummyDb.passes.removeAll { it.id == passId }
    }
}