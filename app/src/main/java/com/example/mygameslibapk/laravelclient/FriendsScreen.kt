package com.example.mygameslibapk

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygameslibapk.laravelclient.FriendsUiState
import com.example.mygameslibapk.laravelclient.FriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onOpenChat: (userId: Int, userName: String) -> Unit,
    viewModel: FriendsViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadFriends()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Friends List & Requests") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = viewModel.uiState) {
                is FriendsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is FriendsUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                is FriendsUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Incoming Friend Requests Section
                        if (state.incoming.isNotEmpty()) {
                            item {
                                Text("Pending Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            items(state.incoming) { user ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(user.name, fontWeight = FontWeight.Bold)
                                        Button(onClick = { viewModel.acceptRequest(user.id) }) {
                                            Text("Accept")
                                        }
                                    }
                                }
                            }
                        }

                        // Friends List Section
                        item {
                            Text("All Friends (${state.friends.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (state.friends.isEmpty()) {
                            item { Text("No friends added yet.") }
                        } else {
                            items(state.friends) { friend ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(friend.name, fontWeight = FontWeight.Bold)
                                            Text(friend.email, style = MaterialTheme.typography.bodySmall)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Chat Button -> Navigates to ChatScreen
                                            Button(onClick = { onOpenChat(friend.id, friend.name) }) {
                                                Icon(Icons.Default.Chat, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Chat")
                                            }

                                            IconButton(onClick = { viewModel.removeFriend(friend.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remove")
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