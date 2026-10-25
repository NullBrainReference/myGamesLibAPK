package com.example.mygameslibapk

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
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
    var selectedPositionToExtend by remember { mutableStateOf<HirePositionDto?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSlotRequestDialog by remember { mutableStateOf(false) }

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
                },
                actions = {
                    if (isOwnerOrAdmin) {
                        TextButton(onClick = { showSlotRequestDialog = true }) {
                            Text("Request Slots")
                        }
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
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No open positions available.")
                            if (isOwnerOrAdmin) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { showSlotRequestDialog = true }) {
                                    Text("Request Position Slots from Admin")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.positions) { position ->
                                val takenTickets = position.tickets.size
                                val hasCapacity = takenTickets < position.ticketsAmount

                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(position.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(position.description, style = MaterialTheme.typography.bodyMedium)

                                        Spacer(modifier = Modifier.height(8.dp))

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
                                                "Capacity: $takenTickets / ${position.ticketsAmount}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (hasCapacity) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (isOwnerOrAdmin) {
                                                    OutlinedButton(onClick = { selectedPositionToExtend = position }) {
                                                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Request Tickets", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }

                                                Button(
                                                    onClick = { selectedPositionToApply = position },
                                                    enabled = hasCapacity
                                                ) {
                                                    Text(if (hasCapacity) "Apply Now" else "Full")
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

    // Request Ticket Capacity Extension Dialog
    selectedPositionToExtend?.let { position ->
        var quantity by remember { mutableStateOf("2") }
        var reason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedPositionToExtend = null },
            title = { Text("Extend Tickets for ${position.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Additional Tickets Quantity") })
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason for Admins") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestMoreTickets(projectId, position.id, quantity.toIntOrNull() ?: 1, reason) {
                            selectedPositionToExtend = null
                        }
                    },
                    enabled = reason.trim().length >= 10
                ) {
                    Text("Submit Request")
                }
            },
            dismissButton = { TextButton(onClick = { selectedPositionToExtend = null }) { Text("Cancel") } }
        )
    }

    // Request New Position Slots Dialog
    if (showSlotRequestDialog) {
        var positionTitle by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("1") }
        var reason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSlotRequestDialog = false },
            title = { Text("Request Position Slots") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = positionTitle, onValueChange = { positionTitle = it }, label = { Text("Requested Position Title (e.g. 3D Artist)") })
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason for Admins") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestPositionSlots(projectId, positionTitle, quantity.toIntOrNull() ?: 1, reason) {
                            showSlotRequestDialog = false
                        }
                    },
                    enabled = positionTitle.isNotBlank() && reason.trim().length >= 10
                ) {
                    Text("Submit Slot Request")
                }
            },
            dismissButton = { TextButton(onClick = { showSlotRequestDialog = false }) { Text("Cancel") } }
        )
    }

    // Create Vacancy Direct Dialog
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