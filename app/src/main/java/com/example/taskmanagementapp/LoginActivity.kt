package com.example.taskmanagementapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmanagementapp.network.LoginResponse
import com.example.taskmanagementapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private var actionButtonTextCache: CharSequence? = null
    private var isLoading = false
    private var isLogin = true
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
        val usernameEdit = findViewById<TextInputEditText>(R.id.username_edit)
        val passwordEdit = findViewById<TextInputEditText>(R.id.password_edit)
        progressBar = findViewById(R.id.login_button_progress)

        // initial UI state
        updateUi()

        switchText.setOnClickListener {
            isLogin = !isLogin
            updateUi()
        }

        actionButton.setOnClickListener {
            val username = usernameEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLogin) {
                // show loading inside the button and call login
                setLoading(true, actionButton)
                performLogin(username, password)
            } else {
                // perform registration via the same API (action=register)
                setLoading(true, actionButton)
                performRegister(username, password, actionButton)
            }
        }

        // updateUi already called above
    }

    private fun performLogin(username: String, password: String) {
        // ensure the progress indicator is visible (caller also sets it)
        progressBar.visibility = View.VISIBLE

        RetrofitClient.instance.login(username, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                // hide loading
                setLoading(false, findViewById(R.id.auth_action_button))

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.status == "success") {
                        // Store user_id in SharedPreferences
                        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("user_id", loginResponse.userId ?: -1)
                            apply()
                        }

                        Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                        
                        // Navigate to HomeActivity
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        val errorMsg = loginResponse?.message ?: "Login failed"
                        Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoading(false, findViewById(R.id.auth_action_button))
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun performRegister(username: String, password: String, actionButton: MaterialButton) {
        // ensure spinner visible
        progressBar.visibility = View.VISIBLE

        RetrofitClient.instance.login(username, password, "register").enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                // hide loading
                setLoading(false, actionButton)

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null && registerResponse.status == "success") {
                        Toast.makeText(this@LoginActivity, "${registerResponse.message}. Please Login again to continue!", Toast.LENGTH_LONG).show()
                        // switch to login mode so user can sign in
                        isLogin = true
                        updateUi()
                    } else {
                        val errorMsg = registerResponse?.message ?: "Registration failed"
                        Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoading(false, actionButton)
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoading(show: Boolean, actionButton: MaterialButton) {
        if (show == isLoading) return
        isLoading = show
        if (show) {
            // cache current text and show spinner
            actionButtonTextCache = actionButton.text
            actionButton.text = ""
            actionButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
        } else {
            // restore text and hide spinner
            actionButton.isEnabled = true
            progressBar.visibility = View.GONE
            // restore the proper text according to mode
            updateUi()
        }
    }

    private fun updateUi() {
        // find views and update labels according to current mode
        val titleText = findViewById<TextView>(R.id.login_title)
        val switchText = findViewById<TextView>(R.id.auth_switch_text)
        val actionButton = findViewById<MaterialButton>(R.id.auth_action_button)

        val titleRes = if (isLogin) R.string.auth_title_login else R.string.auth_title_register
        val actionRes = if (isLogin) R.string.auth_action_login else R.string.auth_action_register
        val switchRes = if (isLogin) R.string.auth_switch_to_register else R.string.auth_switch_to_login

        titleText.setText(titleRes)
        actionButton.setText(actionRes)
        switchText.setText(switchRes)
    }
}
