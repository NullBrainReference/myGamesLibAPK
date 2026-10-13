package com.example.mygameslibapk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mygameslibapk.laravelclient.LoginScreen
import com.example.mygameslibapk.laravelclient.TokenManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RootApp()
            }
        }
    }
}

@Composable
fun RootApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    // Check if a saved token exists to set the starting screen
    val startDestination = if (tokenManager.getToken() != null) "main" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        // Pop login off the backstack so pressing 'Back' won't return to login
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen()
        }
    }
}