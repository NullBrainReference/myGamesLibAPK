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
    onLogout: () -> Unit = {}
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
            // Projects List Route
            composable(Screen.Projects.route) {
                ProjectsScreen(
                    currentUser = currentUser,
                    onNavigateToVacancies = { projectId, isOwnerOrAdmin ->
                        navController.navigate("projects/$projectId/vacancies/$isOwnerOrAdmin")
                    },
                    onNavigateToApplicants = { projectId ->
                        navController.navigate("projects/$projectId/applicants")
                    }
                )
            }

            // Vacancies Route with correct isOwnerOrAdmin parameter
            composable(
                route = "projects/{projectId}/vacancies/{isOwnerOrAdmin}",
                arguments = listOf(
                    navArgument("projectId") { type = NavType.IntType },
                    navArgument("isOwnerOrAdmin") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId") ?: return@composable
                val isOwnerOrAdmin = backStackEntry.arguments?.getBoolean("isOwnerOrAdmin") ?: false

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
                ApplicantsScreen(projectId = projectId, onBack = { navController.popBackStack() })
            }

            // Messages Screen
            composable(Screen.Messages.route) {
                MessagesScreen(
                    onOpenChat = { userId, userName ->
                        navController.navigate("chat/$userId/$userName")
                    }
                )
            }

            // Friends Screen
            composable(Screen.Friends.route) {
                FriendsScreen(
                    onOpenChat = { userId, userName ->
                        navController.navigate("chat/$userId/$userName")
                    }
                )
            }

            // Direct One-on-One Chat Screen
            composable(
                route = "chat/{userId}/{userName}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("userName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val targetUserId = backStackEntry.arguments?.getInt("userId") ?: return@composable
                val targetUserName = backStackEntry.arguments?.getString("userName") ?: "Chat"

                ChatScreen(
                    targetUserId = targetUserId,
                    targetUserName = targetUserName,
                    currentUserId = currentUser?.id,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    onLogoutSuccess = onLogout
                )
            }
        }
    }
}