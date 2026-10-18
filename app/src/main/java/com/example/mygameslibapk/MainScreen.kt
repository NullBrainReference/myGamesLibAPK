package com.example.mygameslibapk

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mygameslibapk.laravelclient.ProfileViewModel

@Composable
fun MainScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit = {} // <--- Added onLogout parameter
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentUser = profileViewModel.userProfile

    val items = listOf(
        Screen.Projects,
        Screen.Messages,
        Screen.Friends,
        Screen.Profile
    )

    val showBottomBar = items.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Projects.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Projects.route) {
                ProjectsScreen(
                    currentUser = currentUser,
                    onNavigateToVacancies = { projectId ->
                        navController.navigate("projects/$projectId/vacancies")
                    },
                    onNavigateToApplicants = { projectId ->
                        navController.navigate("projects/$projectId/applicants")
                    }
                )
            }

            composable(
                route = "projects/{projectId}/vacancies",
                arguments = listOf(navArgument("projectId") { type = NavType.IntType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId") ?: return@composable
                val isOwnerOrAdmin = currentUser?.role == "admin"

                VacanciesScreen(
                    projectId = projectId,
                    isOwnerOrAdmin = isOwnerOrAdmin,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "projects/{projectId}/applicants",
                arguments = listOf(navArgument("projectId") { type = NavType.IntType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId") ?: return@composable

                ApplicantsScreen(
                    projectId = projectId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Messages.route) { MessagesScreen() }
            composable(Screen.Friends.route) { FriendsScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    onLogoutSuccess = onLogout
                )
            }
        }
    }
}