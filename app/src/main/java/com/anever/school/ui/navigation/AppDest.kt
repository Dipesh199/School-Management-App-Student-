package com.anever.school.ui.navigation


sealed class AppDest(val route: String) {
    data object Home : AppDest("home")
    data object Classes : AppDest("classes")
    data object Assignments : AppDest("assignments")
    data object Exams : AppDest("exams")
    data object More : AppDest("more")
    data object Attendance : AppDest("attendance")
    data object Notices : AppDest("notices")


    data object ClassDetails : AppDest("class/{classId}") {
        const val routePattern = "class/{classId}"
        fun routeWithArg(id: String) = "class/$id"
    }

    data object AssignmentDetails : AppDest("assignment/{assignmentId}") {
        const val routePattern = "assignment/{assignmentId}"
        fun routeWithArg(id: String) = "assignment/$id"
    }

    data object NoticeDetails : AppDest("notice/{noticeId}") {     // ⬅️ NEW
        const val routePattern = "notice/{noticeId}"
        fun routeWithArg(id: String) = "notice/$id"
    }
}
