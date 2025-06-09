package com.fake.wastingmoney

/*
 // Code Attribution:
 // Author: IIE
 // Source: Programming 3C Module Manual [PM1]
 // Student numbers: ST10145067, ST10081245, ST10264009, ST10368647, ST10397162

 // Code Attribution:
 // Author: IIE
 // Source: GitHub source code repository for the module [PM2]
 // URL: https://github.com/iie-PROG7313
 // [Accessed 25 September 2024]
 // Student numbers: ST10145067, ST10081245, ST10264009, ST10368647, ST10397162

 // Code Attribution:
 // Author: JetBrains
 // Source: Kotlin Documentation [PM3]
 // URL: https://kotlinlang.org/docs/home.html
 // [Accessed 25 September 2024]
 // Student numbers: ST10145067, ST10081245, ST10264009, ST10368647, ST10397162
 */

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.fake.wastingmoney.database.DatabaseHelper
import com.fake.wastingmoney.utils.StreakAchievementDialog
import com.fake.wastingmoney.utils.WelcomeConfettiDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterLink: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper

    // TAG for logging
    private val TAG = "MainActivity"

    // Define a constant for the Intent extra key
    companion object {
        const val EXTRA_SHOW_WELCOME_CONFETTI = "show_welcome_confetti"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        try {
            FirebaseApp.initializeApp(this)
            auth = FirebaseAuth.getInstance()
            dbHelper = DatabaseHelper(this)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase or Database initialization error", e)
            Toast.makeText(this, "Initialization failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }

        // Redirect if already logged in with Firebase Auth
        if (auth.currentUser != null) {
            auth.currentUser?.email?.let { email ->
                val localUserId = dbHelper.getUserIdByUsername(email)
                if (localUserId != null) {
                    checkAndIncrementStreak(localUserId, false) // Don't show confetti if already logged in
                } else {
                    Log.w(TAG, "Firebase user logged in, but no corresponding local SQLite user found for email: $email")
                }
            }
            goToDashboard(false) // No confetti on automatic re-login
            return
        }

        setupClickListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        val firebaseUserEmail = auth.currentUser?.email
                        var showConfetti = false // Default to false

                        if (firebaseUserEmail != null) {
                            var localUserId = dbHelper.getUserIdByUsername(firebaseUserEmail)
                            if (localUserId == null) {
                                localUserId = dbHelper.addUser(firebaseUserEmail, password)
                                if (localUserId == -1L) {
                                    Log.e(TAG, "Failed to add new Firebase user to local SQLite DB.")
                                    Toast.makeText(this, "Error initializing local user data.", Toast.LENGTH_LONG).show()
                                    goToDashboard(false) // Don't show confetti on error
                                    return@addOnCompleteListener
                                }
                                Log.d(TAG, "Firebase user '$firebaseUserEmail' added to local SQLite with ID: $localUserId")
                                showConfetti = true // Set to true if it's truly a first-time local setup via this path
                            }

                            // Now call checkAndIncrementStreak. The logic within it will determine if it's a 'first streak'.
                            // We don't pass 'showConfetti' here directly, as checkAndIncrementStreak handles it internally
                            // by setting the flag for goToDashboard based on actual streak data.
                            checkAndIncrementStreak(localUserId, showConfetti)
                        }
                        // goToDashboard is now called from within checkAndIncrementStreak or directly if localUserId is null.
                        // We will pass the 'showConfetti' flag from checkAndIncrementStreak to goToDashboard.

                    } else {
                        val exception = task.exception
                        val errorMessage = when (exception) {
                            is FirebaseAuthException -> {
                                when (exception.errorCode) {
                                    "ERROR_INVALID_EMAIL" -> "Invalid email"
                                    "ERROR_WRONG_PASSWORD" -> "Wrong password"
                                    "ERROR_USER_NOT_FOUND" -> "User not found"
                                    else -> "Login failed: ${exception.localizedMessage}"
                                }
                            }
                            else -> "Login failed: ${exception?.localizedMessage}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e(TAG, exception?.message ?: "Unknown login error", exception)
                    }
                }
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showWelcomeConfettiDialog() {
        val dialog = WelcomeConfettiDialog()
        dialog.show(supportFragmentManager, "WelcomeConfettiDialog")
    }

    private fun showStreakAchievementDialog() {
        val dialog = StreakAchievementDialog()
        dialog.show(supportFragmentManager, "StreakAchievementDialog")
    }

    // Modified to use local SQLite user ID and pass showConfetti flag
    private fun checkAndIncrementStreak(userId: Long, initialLoginShowConfetti: Boolean) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val todayFormatted = sdf.format(today.time)

        val streakData = dbHelper.getStreakData(userId)

        var lastLoginDateString: String? = null
        var streakCount: Long = 0L
        var shouldShowConfetti = initialLoginShowConfetti // Start with the flag passed from login

        if (streakData != null) {
            streakCount = streakData.first
            lastLoginDateString = streakData.second
        }

        if (streakData == null || lastLoginDateString == null) {
            // Scenario: No streak data exists for this user in SQLite.
            streakCount = 1L
            val insertResult = dbHelper.insertInitialStreakData(userId, todayFormatted, streakCount)
            if (insertResult != -1L) {
                Log.d(TAG, "First login detected (SQLite), streak set to 1. Flagging for welcome confetti.")
                shouldShowConfetti = true // Definitely show confetti if this is the first streak entry
            } else {
                Log.e(TAG, "Error inserting initial streak data to SQLite for user: $userId")
                Toast.makeText(this, "Failed to initialize streak data locally.", Toast.LENGTH_SHORT).show()
                shouldShowConfetti = false // Don't show if there was an error
            }
            goToDashboard(shouldShowConfetti) // Go to dashboard, passing the confetti flag
        } else {
            // Scenario: Streak data exists in SQLite
            val lastLoginDate = sdf.parse(lastLoginDateString) ?: Date(0)

            val todayDateOnly = Calendar.getInstance().apply { time = sdf.parse(todayFormatted)!! }
            val lastLoginDateOnly = Calendar.getInstance().apply { time = lastLoginDate }

            val diffInMillis = todayDateOnly.timeInMillis - lastLoginDateOnly.timeInMillis
            val diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)

            if (diffInDays == 1L) {
                // Logged in yesterday, streak continues
                streakCount++
                val affectedRows = dbHelper.updateStreakData(userId, streakCount, todayFormatted)
                if (affectedRows > 0) {
                    Log.d(TAG, "Streak continued (SQLite). Current streak: $streakCount days.")
                    Toast.makeText(this, "Your current streak: $streakCount days", Toast.LENGTH_SHORT).show()
                    if (streakCount == 7L) {
                        showStreakAchievementDialog() // This dialog can still be shown directly
                        Log.d(TAG, "7-Day Streak Achieved! Showing dialog.")
                    }
                } else {
                    Log.e(TAG, "Error updating streak data in SQLite for user: $userId")
                }
            } else if (diffInDays > 1L) {
                // Streak broken
                streakCount = 1L // Reset streak
                val affectedRows = dbHelper.updateStreakData(userId, streakCount, todayFormatted)
                if (affectedRows > 0) {
                    Log.d(TAG, "Streak broken (SQLite). Reset to 1 day.")
                    Toast.makeText(this, "Your current streak: $streakCount days", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Error resetting streak data in SQLite for user: $userId")
                }
            } else { // diffInDays == 0L, user logged in multiple times today
                Log.d(TAG, "User already logged in today (SQLite). Streak: $streakCount days. No update needed.")
                Toast.makeText(this, "Your current streak: $streakCount days", Toast.LENGTH_SHORT).show()
            }
            // If it's not the first streak, confetti is not shown
            goToDashboard(false) // No confetti for regular logins
        }
    }

    // Modified to accept a boolean flag for showing confetti
    private fun goToDashboard(showConfetti: Boolean) {
        val intent = Intent(this, Home::class.java).apply {
            // Add the flag to the intent
            putExtra(EXTRA_SHOW_WELCOME_CONFETTI, showConfetti)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish() // Close MainActivity
    }
}