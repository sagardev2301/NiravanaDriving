package com.niravanadriving.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.niravanadriving.app.ui.screens.home.HomeScreen
import com.niravanadriving.app.ui.screens.learner.LearnerScreen
import com.niravanadriving.app.ui.screens.login.LoginScreen
import com.niravanadriving.app.ui.screens.profile.ProfileScreen
import com.niravanadriving.app.ui.screens.schedule.AddEditLessonScreen
import com.niravanadriving.app.ui.screens.schedule.ScheduleScreen
import com.niravanadriving.app.ui.viewmodel.HomeViewModel
import com.niravanadriving.app.ui.viewmodel.ScheduleViewModel
import com.niravanadriving.app.ui.viewmodel.LearnerViewModel
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object HomeRoute

@Serializable
object ScheduleRoute

@Serializable
object LearnerRoute

@Serializable
object ProfileRoute

@Serializable
object AddLearnerRoute

@Serializable
data class AddEditLessonRoute(val lessonId: String? = null)

data class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    val label: String
)

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(HomeRoute, Icons.Default.Home, "Home"),
    TopLevelDestination(ScheduleRoute, Icons.Default.DateRange, "Schedule"),
    TopLevelDestination(LearnerRoute, Icons.Default.Face, "Learner"),
    TopLevelDestination(ProfileRoute, Icons.Default.Person, "Profile")
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Initialize ViewModels once at the AppNavigation level to ensure they persist across tab switches
    val homeViewModel: HomeViewModel = viewModel { HomeViewModel() }
    val scheduleViewModel: ScheduleViewModel = viewModel { ScheduleViewModel() }
    val learnerViewModel: LearnerViewModel = viewModel { LearnerViewModel() }

    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        TOP_LEVEL_DESTINATIONS.any { dest.hasRoute(it.route::class) }
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NirvanaBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LoginRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<LoginRoute> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable<HomeRoute> {
                HomeScreen(homeViewModel)
            }
            composable<ScheduleRoute> {
                ScheduleScreen(
                    viewModel = scheduleViewModel,
                    onAddLesson = { navController.navigate(AddEditLessonRoute(null)) },
                    onEditLesson = { id -> navController.navigate(AddEditLessonRoute(id)) }
                )
            }
            composable<LearnerRoute> {
                LearnerScreen(learnerViewModel, onAddLearner = { navController.navigate(AddLearnerRoute) })
            }
            composable<ProfileRoute> {
                ProfileScreen()
            }
            composable<AddLearnerRoute> {
                com.niravanadriving.app.ui.screens.learner.AddLearnerScreen(onBack = { navController.popBackStack() })
            }
            composable<AddEditLessonRoute> { backStackEntry ->
                val route: AddEditLessonRoute = backStackEntry.toRoute()
                AddEditLessonScreen(
                    viewModel = scheduleViewModel,
                    lessonId = route.lessonId,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun NirvanaBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        TOP_LEVEL_DESTINATIONS.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(destination.icon, contentDescription = destination.label)
                },
                label = {
                    Text(destination.label)
                }
            )
        }
    }
}
