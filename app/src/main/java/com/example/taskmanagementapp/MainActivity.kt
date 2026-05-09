package com.example.taskmanagementapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Route to Login or Home depending on whether a user_id is stored in SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId > 0) {
            // user is logged in -> go to Home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // not logged in -> go to Login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
        return
    }
}