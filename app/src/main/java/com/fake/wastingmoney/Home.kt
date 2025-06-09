package com.fake.wastingmoney

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
<<<<<<< HEAD
import android.widget.Toast
=======
>>>>>>> 0542f51 (final)
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
<<<<<<< HEAD
=======
import com.fake.wastingmoney.utils.WelcomeConfettiDialog // Import the dialog class
>>>>>>> 0542f51 (final)

class Home : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSummaryText: TextView

    private lateinit var cardAddExpense: CardView
    private lateinit var cardCategories: CardView
    private lateinit var cardTransactions: CardView
    private lateinit var cardDashboard: CardView
<<<<<<< HEAD
    private lateinit var btnLogout: Button
=======
    private lateinit var btnLogout: Button // Corrected: This is a Button, not a CardView from your previous XML description
>>>>>>> 0542f51 (final)

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_home)

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome)
        progressBar = findViewById(R.id.progressBarSummary)
        tvSummaryText = findViewById(R.id.tvSummaryText)

        cardAddExpense = findViewById(R.id.cardAddExpense)
        cardCategories = findViewById(R.id.cardCategories)
        cardTransactions = findViewById(R.id.cardTransactions)
        cardDashboard = findViewById(R.id.cardDashboard)
<<<<<<< HEAD
        btnLogout = findViewById(R.id.cardLogout) // Note: ID is cardLogout in XML but it's a Button
=======
        btnLogout = findViewById(R.id.cardLogout) // Assuming this is indeed a Button with ID cardLogout
>>>>>>> 0542f51 (final)

        // Set personalized welcome message if user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val displayName = currentUser.displayName ?: currentUser.email ?: "User"
            tvWelcome.text = "Welcome, $displayName"
        } else {
            tvWelcome.text = "Welcome, User"
        }

<<<<<<< HEAD
=======
        // --- Confetti Logic ---
        // Check if the EXTRA_SHOW_WELCOME_CONFETTI flag is set in the intent
        val showConfetti = intent.getBooleanExtra(MainActivity.EXTRA_SHOW_WELCOME_CONFETTI, false)
        if (showConfetti) {
            showWelcomeConfettiDialog()
        }
        // --- End Confetti Logic ---


>>>>>>> 0542f51 (final)
        // Click listeners for navigation
        cardAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpense::class.java))
        }

        cardCategories.setOnClickListener {
            startActivity(Intent(this, Categories::class.java))
        }

        cardTransactions.setOnClickListener {
            startActivity(Intent(this, Transaction::class.java))
        }

        cardDashboard.setOnClickListener {
            // If you have DashboardActivity, uncomment the line below:
            startActivity(Intent(this, DashboardActivity::class.java))

            // Temporary placeholder - remove this when you have the actual activity
        }

        btnLogout.setOnClickListener {
            logout()
        }

        // Show loading/progress simulation
        showLoading()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tvSummaryText.visibility = View.GONE

        progressBar.postDelayed({
            progressBar.visibility = View.GONE
            tvSummaryText.visibility = View.VISIBLE
            tvSummaryText.text = "Current Month Summary: R 2,345.00"
        }, 2000)
    }

<<<<<<< HEAD
=======
    // New private method to show the WelcomeConfettiDialog
    private fun showWelcomeConfettiDialog() {
        val dialog = WelcomeConfettiDialog()
        // Use supportFragmentManager to show the dialog
        dialog.show(supportFragmentManager, "WelcomeConfettiDialog")
    }

>>>>>>> 0542f51 (final)
    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}