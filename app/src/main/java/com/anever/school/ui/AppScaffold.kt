package com.anever.school.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.anever.school.ui.navigation.AppDest
import com.anever.school.ui.screens.AssignmentDetailsScreen
import com.anever.school.ui.screens.AssignmentsScreen
import com.anever.school.ui.screens.AttendanceScreen
import com.anever.school.ui.screens.ClassDetailsScreen
import com.anever.school.ui.screens.ClassesScreen
import com.anever.school.ui.screens.EventsScreen
import com.anever.school.ui.screens.ExamsScreen
import com.anever.school.ui.screens.HomeScreen
import com.anever.school.ui.screens.LibraryScreen
import com.anever.school.ui.screens.LostFoundScreen
import com.anever.school.ui.screens.MoreScreen
import com.anever.school.ui.screens.NoticeDetailsScreen
import com.anever.school.ui.screens.NoticesScreen
import com.anever.school.ui.screens.TransportScreen
import com.anever.school.ui.theme.SchoolTheme

@Composable
fun AppScaffold() {
    val navController = rememberNavController()

    val tabs = listOf(
        BottomTab(AppDest.Home.route, "Home", Icons.Default.Home),
        BottomTab(AppDest.Classes.route, "Classes", Icons.Default.School),
        BottomTab(AppDest.Assignments.route, "Lessons", Icons.Default.Assignment),
        BottomTab(AppDest.Exams.route, "Exams", Icons.Default.Event),
        BottomTab(AppDest.More.route, "More", Icons.Default.Menu),
    )
    SchoolTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val currentDest by navController.currentBackStackEntryAsState()
                    val currentRoute = currentDest?.destination?.route
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { inner ->
            NavHost(
                navController = navController,
                startDestination = AppDest.Home.route,
                modifier = Modifier.padding(inner)
            ) {
                composable(AppDest.Home.route) {
                    HomeScreen(
                        onOpenClass = { classId ->
                            navController.navigate(AppDest.ClassDetails.routeWithArg(classId))
                        },
                        onOpenAssignment = { assignmentId ->
                            navController.navigate(
                                AppDest.AssignmentDetails.routeWithArg(
                                    assignmentId
                                )
                            )
                        },
                        onOpenExamSchedule = {
                            navController.navigate(AppDest.Exams.route)
                        },
                        onOpenNotices = {
                            navController.navigate(AppDest.Notices.route) // placeholder: notices in More
                        },
                        onOpenEvents = { navController.navigate(AppDest.Events.route) },
                        onOpenLostFound = { navController.navigate(AppDest.LostFound.route) }
                    )
                }
                composable(AppDest.Classes.route) {
                    ClassesScreen(onOpenClass = { id ->
                        navController.navigate(AppDest.ClassDetails.routeWithArg(id))
                    })
                }
                composable(AppDest.Assignments.route) {
                    AssignmentsScreen(onOpenAssignment = { id ->
                        navController.navigate(AppDest.AssignmentDetails.routeWithArg(id))
                    })
                }
                composable(AppDest.Exams.route) { ExamsScreen() }
                composable(AppDest.More.route) {
                    MoreScreen(
                        onOpenAttendance = { navController.navigate(AppDest.Attendance.route) },
                        onOpenNotices = { navController.navigate(AppDest.Notices.route) },
                        onOpenTransport = { navController.navigate(AppDest.Transport.route) },
                        onOpenLibrary = { navController.navigate(AppDest.Library.route) },
                        onOpenEvents = { navController.navigate(AppDest.Events.route) },
                        onOpenLostFound = { navController.navigate(AppDest.LostFound.route) }
                    )

                }

                composable(
                    route = AppDest.ClassDetails.routePattern,
                    arguments = listOf(navArgument("classId") { type = NavType.StringType })
                ) { backStack ->
                    val classId = backStack.arguments?.getString("classId")!!
                    ClassDetailsScreen(classId)
                }
                composable(
                    route = AppDest.AssignmentDetails.routePattern,
                    arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
                ) { backStack ->
                    val assignmentId = backStack.arguments?.getString("assignmentId")!!
                    AssignmentDetailsScreen(assignmentId)
                }
                // inside NavHost { ... }
                composable(AppDest.Attendance.route) { AttendanceScreen() }

                composable(AppDest.Notices.route) {
                    NoticesScreen(onOpenDetails = { id ->
                        navController.navigate(AppDest.NoticeDetails.routeWithArg(id))
                    })
                }
                composable(
                    route = AppDest.NoticeDetails.routePattern,
                    arguments = listOf(navArgument("noticeId") { type = NavType.StringType })
                ) {
                    val id = it.arguments?.getString("noticeId")!!
                    NoticeDetailsScreen(id)
                }

                composable(AppDest.Transport.route) { TransportScreen() }
                composable(AppDest.Library.route) { LibraryScreen() }
                composable(AppDest.Events.route) { EventsScreen() }
                composable(AppDest.LostFound.route) { LostFoundScreen() }


            }
        }
    }
}

data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)