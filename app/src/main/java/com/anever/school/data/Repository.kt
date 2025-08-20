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
    private val resultDao: ResultDao = InMemoryResultDao(),
    private val attendanceDao: AttendanceDao = InMemoryAttendanceDao(),
    private val requestDao: RequestDao = InMemoryRequestDao(),
    private val transportDao: TransportDao = InMemoryTransportDao(),
    private val libraryDao: LibraryDao = InMemoryLibraryDao(),
    private val eventDao: EventDao = InMemoryEventDao(),
    private val passDao: PassDao = InMemoryPassDao(),
    private val lostFoundDao: LostFoundDao = InMemoryLostFoundDao(),          // ⬅️ NEW
    private val lostFoundAlertsDao: LostFoundAlertsDao = InMemoryLostFoundAlertsDao()
) {

    // ---------- Lost & Found ----------
    private val lfCategories = listOf("Electronics", "Books", "Clothing", "ID/Docs", "Accessories", "Others")

    data class LFRow(val item: LostFoundItem, val isMine: Boolean)

    fun listLostFound(
        type: String? = null, category: String? = null, query: String? = null
    ): List<LFRow> {
        val q = query?.trim().orEmpty()
        return lostFoundDao.getAll()
            .asSequence()
            .filter { type == null || it.type.equals(type, true) }
            .filter { category == null || it.category.equals(category, true) }
            .filter {
                q.isBlank() || it.title.contains(q, true) ||
                        it.description.contains(q, true) || it.location.contains(q, true)
            }
            .map { LFRow(it, it.createdBy == "Me") }
            .toList()
    }

    fun addLostOrFound(
        type: String,
        title: String,
        category: String,
        description: String,
        location: String,
        reward: Int?,
        contactName: String = "Me",
        contactPhone: String = "+91 9151 0000"
    ): LostFoundItem {
        val item = LostFoundItem(
            id = "lf" + System.currentTimeMillis(),
            type = type,
            title = title,
            category = category,
            description = description,
            dateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            location = location,
            contactName = contactName,
            contactPhone = contactPhone,
            reward = reward,
            status = "Open",
            createdBy = "Me"
        )
        lostFoundDao.add(item)
        return item
    }

    fun markResolved(id: String): Boolean {
        val found = lostFoundDao.getAll().firstOrNull { it.id == id } ?: return false
        if (found.createdBy != "Me") return false
        lostFoundDao.update(found.copy(status = "Resolved"))
        return true
    }

    fun getAlertCategories(): List<Pair<String, Boolean>> {
        val subs = lostFoundAlertsDao.getSubscribedCategories()
        return lfCategories.map { it to subs.contains(it) }
    }

    fun toggleAlertCategory(cat: String): Boolean = lostFoundAlertsDao.toggleCategory(cat)

    fun latestLostFound(limit: Int = 3): List<LostFoundItem> =
        lostFoundDao.getAll().take(limit)

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


    data class DayMark(val date: LocalDate, val status: AttendanceStatus?) // null = no data

    fun getMonthMarks(year: Int, month: Int): List<DayMark> {
        val first = LocalDate(year, month, 1)
        val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
        val last = nextMonth.minus(DatePeriod(days = 1))

        val records = attendanceDao.getAttendanceBetween(first, last).groupBy { it.date }
        val days = generateSequence(first) { it.plus(DatePeriod(days = 1)) }
            .takeWhile { it <= last }
            .toList()

        return days.map { d ->
            val dayRecs = records[d].orEmpty()
            val status = if (dayRecs.isEmpty()) null else {
                when {
                    dayRecs.any { it.status == AttendanceStatus.Absent } -> AttendanceStatus.Absent
                    dayRecs.any { it.status == AttendanceStatus.Late } -> AttendanceStatus.Late
                    else -> AttendanceStatus.Present
                }
            }
            DayMark(d, status)
        }
    }

    data class SubjectAttendanceRow(val subject: Subject, val presentPct: Int, val present: Int, val total: Int)

    fun getSubjectAttendanceSummary(): List<SubjectAttendanceRow> {
        val all = attendanceDao.getAllAttendance()
        val bySubject = all.groupBy { it.subjectId }
        return subjectDao.getAllSubjects().mapNotNull { s ->
            val recs = bySubject[s.id].orEmpty()
            if (recs.isEmpty()) return@mapNotNull null
            val present = recs.count { it.status == AttendanceStatus.Present }
            val total = recs.size
            val pct = if (total == 0) 0 else (present * 100 / total)
            SubjectAttendanceRow(s, pct, present, total)
        }.sortedBy { it.subject.name }
    }

    // ----- Attendance (per-day detail) -----
    data class SubjectDayStatus(val subject: Subject, val status: AttendanceStatus?)

    fun getDaySubjectMarks(date: LocalDate): List<SubjectDayStatus> {
        val recs = attendanceDao.getAttendanceBetween(date, date).groupBy { it.subjectId }
        return subjectDao.getAllSubjects().mapNotNull { s ->
            val day = recs[s.id]?.firstOrNull() ?: return@mapNotNull null
            SubjectDayStatus(subject = s, status = day.status)
        }.sortedBy { it.subject.name }
    }

    fun get30DayTrend(): List<Int> {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val start = today.minus(DatePeriod(days = 29))
        val window = attendanceDao.getAttendanceBetween(start, today)
        val byDate = window.groupBy { it.date }
        val dates = generateSequence(start) { it.plus(DatePeriod(days = 1)) }.take(30).toList()
        return dates.map { d ->
            val recs = byDate[d].orEmpty()
            if (recs.isEmpty()) 0
            else (recs.count { it.status == AttendanceStatus.Present } * 100 / recs.size)
        }
    }

    // ---------- Requests ----------
    fun submitLeaveRequest(reason: String, from: LocalDate, to: LocalDate): Request {
        val req = Request(
            id = "req${System.currentTimeMillis()}",
            type = "Leave",
            reason = reason,
            fromDate = from,
            toDate = to,
            status = "Pending",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
        requestDao.addRequest(req)
        return req
    }

    // ----- Notices -----
    data class NoticeItem(val notice: Notice, val isBookmarked: Boolean)

    fun getNotices(category: String? = null, query: String? = null): List<NoticeItem> {
        val all = noticeDao.getAllNotices()
            .filter { category == null || it.category.equals(category, ignoreCase = true) }
            .filter { query.isNullOrBlank() || it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true) }
        return all.map { NoticeItem(it, NoticeBookmarks.ids.contains(it.id)) }
    }

    fun getNoticeById(id: String): Notice? =
        noticeDao.getAllNotices().find { it.id == id }

    fun toggleNoticeBookmark(id: String): Boolean {
        val set = NoticeBookmarks.ids
        return if (set.remove(id)) false else { set.add(id); true }
    }

    // ---------- Transport ----------
    fun getTransportRoute(): Route = transportDao.getRoute()

    data class TransportEta(val status: String, val info: String)

    fun calcEtaFor(stop: BusStop, now: LocalDateTime): TransportEta {
        val tz = TimeZone.currentSystemDefault()
        val today = now.date
        val pickupInstant = LocalDateTime(today, stop.pickup).toInstant(tz)
        val dropInstant = LocalDateTime(today, stop.drop).toInstant(tz)
        val nowInstant = now.toInstant(tz)

        return when {
            nowInstant < pickupInstant -> {
                TransportEta("Pickup", "in " + humanize(pickupInstant - nowInstant) + " at ${stop.pickup}")
            }
            nowInstant in pickupInstant..dropInstant -> {
                TransportEta("En route", "drop at ${stop.drop} (in " + humanize(dropInstant - nowInstant) + ")")
            }
            else -> {
                // Next day pickup
                TransportEta("Next Pickup", "tomorrow at ${stop.pickup}")
            }
        }
    }

    private fun humanize(d: kotlin.time.Duration): String {
        val s = d.inWholeSeconds
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return buildString {
            if (h > 0) append("${h}h ")
            if (m > 0) append("${m}m ")
            append("${sec}s")
        }
    }

    fun requestChangeStop(toStopId: String): Request {
        val route = transportDao.updateStudentStop(toStopId) // reflect immediately in UI
        val selected = route.stops.firstOrNull { it.id == toStopId }
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val req = Request(
            id = "req${System.currentTimeMillis()}",
            type = "ChangeStop",
            reason = "Change stop to ${selected?.name ?: toStopId}",
            fromDate = today,                 // reuse fields for audit
            toDate = today,
            status = "Pending",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
        requestDao.addRequest(req)
        return req
    }

    // ---------- Library ----------
    data class BookRow(val book: Book, val isBorrowed: Boolean, val reservedLoanId: String?)


    fun browseBooks(query: String): List<BookRow> {
        val books = if (query.isBlank()) libraryDao.getAllBooks() else libraryDao.searchBooks(query)
        val loans = libraryDao.getLoans()
        val borrowedIds = loans.filter { it.status == LoanStatus.Current }.map { it.bookId }.toSet()
        val reservedByMe: Map<String, String> = loans
            .filter { it.status == LoanStatus.Reserved }
            .associate { it.bookId to it.id } // bookId -> loanId (reservation)
        return books.map { b ->
            BookRow(
                book = b,
                isBorrowed = b.id in borrowedIds,
                reservedLoanId = reservedByMe[b.id]
            )
        }.sortedBy { it.book.title }
    }

    fun getLoansWithMeta(): List<LoanMeta> {
        val loans = libraryDao.getLoans().filter { it.status == LoanStatus.Current || it.status == LoanStatus.Reserved }
        val books = libraryDao.getAllBooks().associateBy { it.id }
        return loans.mapNotNull { loan ->
            val book = books[loan.bookId] ?: return@mapNotNull null
            LoanMeta(loan, book, computeDaysLeft(loan), computeFine(loan))
        }.sortedBy { it.loan.dueDate }
    }

    data class LoanMeta(val loan: Loan, val book: Book, val daysLeft: Int, val fine: Int)

    private fun computeDaysLeft(loan: Loan): Int {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return (loan.dueDate.toEpochDays() - today.toEpochDays())
    }

    private fun computeFine(loan: Loan, ratePerDay: Int = 1): Int {
        val overdue = -computeDaysLeft(loan)
        return if (overdue > 0) overdue * ratePerDay else 0
    }

    fun reserveBook(bookId: String): Result<String> {
        val books = libraryDao.getAllBooks()
        val book = books.firstOrNull { it.id == bookId }
            ?: return Result.failure(IllegalArgumentException("Book not found"))

        if (book.available <= 0) return Result.failure(IllegalStateException("No copies available"))

        val loans = libraryDao.getLoans()
        val alreadyBorrowed = loans.any { it.bookId == bookId && it.status == LoanStatus.Current }
        if (alreadyBorrowed) return Result.failure(IllegalStateException("You already borrowed this book"))

        val alreadyReserved = loans.any { it.bookId == bookId && it.status == LoanStatus.Reserved }
        if (alreadyReserved) return Result.failure(IllegalStateException("You already reserved this book"))

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val holdUntil = today.plus(DatePeriod(days = 3)) // reservation hold
        val loan = Loan(
            id = "l${System.currentTimeMillis()}",
            bookId = bookId,
            issueDate = today,
            dueDate = holdUntil,
            status = LoanStatus.Reserved,
            renewals = 0
        )
        libraryDao.putLoan(loan)
        libraryDao.updateBook(book.copy(available = book.available - 1))
        return Result.success("Reserved: ${book.title} (hold until $holdUntil)")
    }

    fun cancelReservation(loanId: String): Result<String> {
        val loans = libraryDao.getLoans()
        val loan = loans.firstOrNull { it.id == loanId }
            ?: return Result.failure(IllegalArgumentException("Reservation not found"))

        if (loan.status != LoanStatus.Reserved) {
            return Result.failure(IllegalStateException("Only reservations can be canceled"))
        }

        val book = libraryDao.getAllBooks().firstOrNull { it.id == loan.bookId }
            ?: return Result.failure(IllegalStateException("Book not found"))

        libraryDao.deleteLoan(loanId)
        libraryDao.updateBook(book.copy(available = book.available + 1))
        return Result.success("Canceled reservation for ${book.title}")
    }


    fun renewLoan(loanId: String): Result<String> {
        val loans = libraryDao.getLoans()
        val idx = loans.indexOfFirst { it.id == loanId }
        if (idx < 0) return Result.failure(IllegalArgumentException("Loan not found"))
        val loan = loans[idx]

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        if (today > loan.dueDate) return Result.failure(IllegalStateException("Cannot renew overdue loan"))
        if (loan.renewals >= 1) return Result.failure(IllegalStateException("Renewal limit reached"))

        val updated = loan.copy(dueDate = loan.dueDate.plus(DatePeriod(days = 7)), renewals = loan.renewals + 1)
        libraryDao.putLoan(updated)
        return Result.success("Renewed until ${updated.dueDate}")
    }

    data class EventItem(
        val event: Event,
        val seatsLeft: Int,
        val myPassId: String? // null if not reserved
    )

    private fun seatsLeftFor(eventId: String): Int {
        val issued = passDao.getPasses().count { it.eventId == eventId && it.status == "Active" }
        val cap = eventDao.getEventById(eventId)?.capacity ?: 0
        return (cap - issued).coerceAtLeast(0)
    }

    fun browseEvents(category: String? = null, query: String? = null): List<EventItem> {
        val all = eventDao.getAllEvents()
            .filter { category == null || it.category.equals(category, true) }
            .filter { query.isNullOrBlank() ||
                    it.title.contains(query!!, true) || it.description.contains(query!!, true) }

        val myPasses = passDao.getPasses().associateBy { it.eventId }
        return all.map { e ->
            EventItem(
                event = e,
                seatsLeft = seatsLeftFor(e.id),
                myPassId = myPasses[e.id]?.id
            )
        }
    }

    fun getUpcomingEvents(limit: Int = 3): List<Event> {
        val nowD = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return eventDao.getAllEvents().filter { it.date >= nowD }.sortedWith(
            compareBy<Event> { it.date }.thenBy { it.start }
        ).take(limit)
    }

    data class PassRow(val pass: EventPass, val event: Event)

    fun getMyPasses(): List<PassRow> {
        val events = eventDao.getAllEvents().associateBy { it.id }
        return passDao.getPasses()
            .filter { it.status == "Active" }
            .mapNotNull { p -> events[p.eventId]?.let { PassRow(p, it) } }
            .sortedWith(compareBy({ it.event.date }, { it.event.start }))
    }

    fun rsvp(eventId: String): Result<String> {
        val seats = seatsLeftFor(eventId)
        if (seats <= 0) return Result.failure(IllegalStateException("No seats left"))
        if (passDao.getPasses().any { it.eventId == eventId && it.status == "Active" })
            return Result.failure(IllegalStateException("Pass already issued"))
        passDao.issuePass(eventId)
        return Result.success("Pass issued")
    }

    fun cancelPass(passId: String): Result<String> {
        val pass = passDao.getPasses().firstOrNull { it.id == passId }
            ?: return Result.failure(IllegalArgumentException("Pass not found"))
        passDao.cancelPass(passId)
        return Result.success("Reservation cancelled for ${pass.code}")
    }

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



