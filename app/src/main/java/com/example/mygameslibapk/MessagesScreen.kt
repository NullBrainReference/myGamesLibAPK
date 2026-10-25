package com.example.mygameslibapk

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygameslibapk.laravelclient.MessagesUiState
import com.example.mygameslibapk.laravelclient.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onOpenChat: (userId: Int, userName: String) -> Unit,
    viewModel: MessagesViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Direct Messages") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = viewModel.uiState) {
                is MessagesUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is MessagesUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                is MessagesUiState.Success -> {
                    if (state.conversations.isEmpty()) {
                        Text("No conversations or friends found.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.conversations) { contact ->
                                ListItem(
                                    modifier = Modifier.clickable { onOpenChat(contact.id, contact.name) },
                                    headlineContent = { Text(contact.name, fontWeight = FontWeight.Bold) },
                                    supportingContent = {
                                        Text(
                                            text = contact.lastMessage?.body ?: "Tap to start conversation",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingContent = {
                                        Icon(Icons.Default.AccountCircle, contentDescription = "User", modifier = Modifier.size(40.dp))
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}