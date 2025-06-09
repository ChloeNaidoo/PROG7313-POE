package com.fake.wastingmoney

<<<<<<< HEAD
=======
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

>>>>>>> 6a51a25 (code attribution)
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import com.fake.wastingmoney.utils.WelcomeConfettiDialog // Import the dialog class

class Home : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSummaryText: TextView

    private lateinit var cardAddExpense: CardView
    private lateinit var cardCategories: CardView
    private lateinit var cardTransactions: CardView
    private lateinit var cardDashboard: CardView
    private lateinit var btnLogout: Button // Corrected: This is a Button, not a CardView from your previous XML description

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
        btnLogout = findViewById(R.id.cardLogout) // Assuming this is indeed a Button with ID cardLogout

        // Set personalized welcome message if user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val displayName = currentUser.displayName ?: currentUser.email ?: "User"
            tvWelcome.text = "Welcome, $displayName"
        } else {
            tvWelcome.text = "Welcome, User"
        }

        // --- Confetti Logic ---
        // Check if the EXTRA_SHOW_WELCOME_CONFETTI flag is set in the intent
        val showConfetti = intent.getBooleanExtra(MainActivity.EXTRA_SHOW_WELCOME_CONFETTI, false)
        if (showConfetti) {
            showWelcomeConfettiDialog()
        }
        // --- End Confetti Logic ---


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

    // New private method to show the WelcomeConfettiDialog
    private fun showWelcomeConfettiDialog() {
        val dialog = WelcomeConfettiDialog()
        // Use supportFragmentManager to show the dialog
        dialog.show(supportFragmentManager, "WelcomeConfettiDialog")
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}