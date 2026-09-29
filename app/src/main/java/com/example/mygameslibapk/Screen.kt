package com.example.mygameslibapk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Projects : Screen("projects", "Projects", Icons.Default.Work)
    object Messages : Screen("messages", "Messages", Icons.Default.Chat)
    object Friends : Screen("friends", "Friends", Icons.Default.Group)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}