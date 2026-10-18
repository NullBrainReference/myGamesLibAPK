package com.example.mygameslibapk

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygameslibapk.laravelclient.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VacanciesScreen(
    projectId: Int,
    isOwnerOrAdmin: Boolean,
    onBack: () -> Unit,
    viewModel: VacanciesViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedPositionToApply by remember { mutableStateOf<HirePositionDto?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        viewModel.loadVacancies(projectId)
    }

    LaunchedEffect(viewModel.actionMessage) {
        viewModel.actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hire Positions & Vacancies") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isOwnerOrAdmin) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Vacancy")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = viewModel.uiState) {
                is VacanciesUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is VacanciesUiState.Error -> Text(text = state.message, modifier = Modifier.align(Alignment.Center))
                is VacanciesUiState.Success -> {
                    if (state.positions.isEmpty()) {
                        Text("No open positions available.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.positions) { position ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(position.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(position.description, style = MaterialTheme.typography.bodyMedium)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Skills list
                                        if (position.skills.isNotEmpty()) {
                                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                position.skills.forEach { skill ->
                                                    SuggestionChip(onClick = {}, label = { Text(skill.title) })
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Ticket Capacity: ${position.ticketsAmount}",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Button(onClick = { selectedPositionToApply = position }) {
                                                Text("Apply Now")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Apply Ticket Dialog
    selectedPositionToApply?.let { position ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selectedPositionToApply = null },
            title = { Text("Apply for ${position.title}") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Why are you a good fit? (Reason/Pitch)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyForPosition(position.id, projectId, reason) {
                            selectedPositionToApply = null
                        }
                    },
                    enabled = reason.trim().length >= 10
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPositionToApply = null }) { Text("Cancel") }
            }
        )
    }

    // Create Vacancy Dialog
    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var tickets by remember { mutableStateOf("1") }
        var skillsCsv by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Post New Position") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Position Title") })
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                    OutlinedTextField(value = tickets, onValueChange = { tickets = it }, label = { Text("Max Tickets Amount") })
                    OutlinedTextField(value = skillsCsv, onValueChange = { skillsCsv = it }, label = { Text("Skills (comma separated)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val skillsList = skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        viewModel.createPosition(projectId, title, description, tickets.toIntOrNull() ?: 1, skillsList) {
                            showCreateDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && description.isNotBlank()
                ) {
                    Text("Post")
                }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }
}