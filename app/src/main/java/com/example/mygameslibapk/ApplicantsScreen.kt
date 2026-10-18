package com.example.mygameslibapk

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygameslibapk.laravelclient.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantsScreen(
    projectId: Int,
    onBack: () -> Unit,
    viewModel: ApplicantsViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTicketToReview by remember { mutableStateOf<PositionTicketDto?>(null) }

    LaunchedEffect(projectId) {
        viewModel.loadApplicants(projectId)
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
                title = { Text("Applicant Tickets") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = viewModel.uiState) {
                is ApplicantsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ApplicantsUiState.Error -> Text(text = state.message, modifier = Modifier.align(Alignment.Center))
                is ApplicantsUiState.Success -> {
                    if (state.tickets.isEmpty()) {
                        Text("No applicant tickets submitted yet.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.tickets) { ticket ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = ticket.user?.name ?: "User #${ticket.userId}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            AssistChip(
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        when (ticket.success) {
                                                            true -> "Accepted"
                                                            false -> "Declined"
                                                            null -> "Pending"
                                                        }
                                                    )
                                                }
                                            )
                                        }

                                        Text(
                                            text = "Position: ${ticket.position?.title ?: "Vacant Role"}",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Reason: ${ticket.reason}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        if (ticket.success == null) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { selectedTicketToReview = ticket },
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Review Application")
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

    // Review Ticket Dialog
    selectedTicketToReview?.let { ticket ->
        var reviewReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedTicketToReview = null },
            title = { Text("Review ${ticket.user?.name ?: "Applicant"}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pitch: ${ticket.reason}", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = reviewReason,
                        onValueChange = { reviewReason = it },
                        label = { Text("Explanation / Feedback") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.reviewTicket(ticket.id, projectId, "accept", reviewReason) {
                                selectedTicketToReview = null
                            }
                        },
                        enabled = reviewReason.trim().length >= 5
                    ) {
                        Text("Accept")
                    }
                    Button(
                        onClick = {
                            viewModel.reviewTicket(ticket.id, projectId, "decline", reviewReason) {
                                selectedTicketToReview = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = reviewReason.trim().length >= 5
                    ) {
                        Text("Decline")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTicketToReview = null }) { Text("Cancel") }
            }
        )
    }
}