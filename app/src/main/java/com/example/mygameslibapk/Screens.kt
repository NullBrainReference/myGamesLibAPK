package com.example.mygameslibapk

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygameslibapk.laravelclient.ProjectDto
import com.example.mygameslibapk.laravelclient.ProjectsUiState
import com.example.mygameslibapk.laravelclient.ProjectsViewModel
import com.example.mygameslibapk.laravelclient.UserDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    currentUser: UserDto? = null,
    onNavigateToVacancies: (projectId: Int, isOwnerOrAdmin: Boolean) -> Unit = { _, _ -> },
    onNavigateToApplicants: (projectId: Int) -> Unit = {},
    viewModel: ProjectsViewModel = viewModel()
) {
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Projects Workspace") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = viewModel.uiState) {
                is ProjectsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProjectsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadProjects() }) {
                            Text("Retry")
                        }
                    }
                }
                is ProjectsUiState.Success -> {
                    if (state.projects.isEmpty()) {
                        Text(
                            text = "No project workspaces found.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.projects) { project ->
                                ProjectCard(
                                    project = project,
                                    currentUser = currentUser,
                                    onManageVacancies = {
                                        val isOwnerOrAdmin = currentUser?.role == "admin" || project.owners.any { it.id == currentUser?.id }
                                        onNavigateToVacancies(project.id, isOwnerOrAdmin)
                                    },
                                    onManageApplicants = { onNavigateToApplicants(project.id) },
                                    onOpenChat = {
                                        Toast.makeText(context, "Project Chat coming soon!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: ProjectDto,
    currentUser: UserDto?,
    onManageVacancies: () -> Unit,
    onManageApplicants: () -> Unit,
    onOpenChat: () -> Unit
) {
    // User is authorized if admin or listed in project.owners
    val isOwnerOrAdmin = currentUser?.role == "admin" || project.owners.any { it.id == currentUser?.id }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (project.isPublic) "Public" else "Private") }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = project.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Project Chat Button (Available to all team members)
                OutlinedButton(
                    onClick = onOpenChat,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat", style = MaterialTheme.typography.labelSmall)
                }

                // Hire Positions Button (Owner or Admin only)
                if (isOwnerOrAdmin) {
                    Button(
                        onClick = onManageVacancies,
                        modifier = Modifier.weight(1.3f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "Hire Positions",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hire Positions", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = onManageApplicants,
                        modifier = Modifier.weight(1.1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Applicants", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    // Non-owners can view open vacancies to apply
                    Button(
                        onClick = onManageVacancies,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Vacancies", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Direct Messages", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun FriendsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Friends List & Requests", style = MaterialTheme.typography.headlineSmall)
    }
}

//@Composable
//fun ProfileScreen() {
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Text("User Profile & Settings", style = MaterialTheme.typography.headlineSmall)
//    }
//}