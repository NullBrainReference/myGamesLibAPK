package com.example.mygameslibapk.laravelclient

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("sanctum_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("sanctum_token", null)
    }

    fun clearToken() {
        prefs.edit().remove("sanctum_token").apply()
    }
}