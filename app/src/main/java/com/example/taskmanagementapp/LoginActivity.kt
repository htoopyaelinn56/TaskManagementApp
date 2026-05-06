package com.example.taskmanagementapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val titleText = findViewById<TextView>(R.id.login_title)
        val switchText = findViewById<TextView>(R.id.auth_switch_text)
        val actionButton = findViewById<MaterialButton>(R.id.auth_action_button)

        var isLogin = true

        fun updateUi() {
            val titleRes = if (isLogin) R.string.auth_title_login else R.string.auth_title_register
            val actionRes = if (isLogin) R.string.auth_action_login else R.string.auth_action_register
            val switchRes = if (isLogin) R.string.auth_switch_to_register else R.string.auth_switch_to_login
            titleText.setText(titleRes)
            actionButton.setText(actionRes)
            switchText.setText(switchRes)
        }

        switchText.setOnClickListener {
            isLogin = !isLogin
            updateUi()
        }

        updateUi()
    }
}
