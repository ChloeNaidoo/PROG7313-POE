package com.fake.wastingmoney

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.fake.wastingmoney.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import android.widget.Spinner
import android.Manifest
import android.os.Build
import com.fake.wastingmoney.utils.ReminderReceiver

class AddExpense : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var btnDatePicker: Button
    private lateinit var btnUploadDocument: Button
    private lateinit var btnSaveExpense: Button
    private lateinit var menuIcon: LinearLayout
    private lateinit var spinnerCurrency: Spinner
    private lateinit var btnSetReminder: Button

    private lateinit var auth: FirebaseAuth

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var selectedDocumentUri: Uri? = null
    private var tempCameraFile: File? = null

    // Variable to store the time for scheduling after permission is granted
    private var pendingReminderTimeInMillis: Long = -1

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1001
        private const val CAMERA_PERMISSION_CODE = 1002
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1003 // New request code
    }

    private val exchangeRates = mapOf(
        "ZAR" to 1.0,
        "USD" to 18.5,
        "EUR" to 20.1,
        "GBP" to 23.5,
        "JPY" to 0.12,
        "AUD" to 12.0,
        "CAD" to 11.5,
        "CHF" to 20.8,
        "CNY" to 2.5,
        "INR" to 0.22
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_add_expense)
            initializeFirebase()
            initializeViews()
            setupCategoryDropdown()
            setupDatePicker()
            setupClickListeners()
            setupMenuListener()
            setupCurrencySpinner()
            setupReminderButton()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading AddExpense: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
    }

    private fun initializeViews() {
        try {
            etAmount = findViewById(R.id.et_amount)
            etDescription = findViewById(R.id.et_description)
            spinnerCategory = findViewById(R.id.spinner_category)
            btnDatePicker = findViewById(R.id.btn_date_picker)
            btnUploadDocument = findViewById(R.id.btn_upload_document)
            btnSaveExpense = findViewById(R.id.btn_save_expense)
            menuIcon = findViewById(R.id.menuIcon)
            spinnerCurrency = findViewById(R.id.spinner_currency)
            btnSetReminder = findViewById(R.id.btn_set_reminder)
        } catch (e: Exception) {
            throw Exception("Failed to initialize views. Please check your layout file has all required IDs: ${e.message}")
        }
    }

    private fun setupCurrencySpinner() {
        val currencyAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.currencies,
            android.R.layout.simple_spinner_item
        )
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = currencyAdapter
    }

    private fun setupReminderButton() {
        btnSetReminder.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun setupMenuListener() {
        menuIcon.setOnClickListener {
            showMenuDialog()
        }
    }

    private fun showMenuDialog() {
        val menuOptions = arrayOf(
            "🏠 Home",
            "📊 Dashboard",
            "💰 Add Income",
            "💸 Add Expense",
            "📂 Categories",
            "📝 Transactions",
            "🚪 Logout"
        )

        AlertDialog.Builder(this)
            .setTitle("Navigation Menu")
            .setItems(menuOptions) { _, which ->
                when (which) {
                    0 -> navigateToHome()
                    1 -> navigateToDashboard()
                    2 -> navigateToAddIncome()
                    3 -> Toast.makeText(this, "You are already on Add Expense", Toast.LENGTH_SHORT).show()
                    4 -> navigateToCategories()
                    5 -> navigateToTransactions()
                    6 -> logout()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, Home::class.java))
        finish()
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun navigateToAddIncome() {
        startActivity(Intent(this, AddIncome::class.java))
        finish()
    }

    private fun navigateToCategories() {
        startActivity(Intent(this, Categories::class.java))
        finish()
    }

    private fun navigateToTransactions() {
        startActivity(Intent(this, Transaction::class.java))
        finish()
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupClickListeners() {
        btnSaveExpense.setOnClickListener {
            saveExpenseData()
        }

        btnUploadDocument.setOnClickListener {
            showUploadOptions()
        }
    }

    private fun showDateTimePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        pendingReminderTimeInMillis = calendar.timeInMillis
                        requestNotificationPermissionAndSchedule() // Request permission first
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun requestNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission already granted, schedule the notification
                scheduleNotification(pendingReminderTimeInMillis)
            } else {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // No permission needed for older Android versions, schedule directly
            scheduleNotification(pendingReminderTimeInMillis)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, schedule the notification
            scheduleNotification(pendingReminderTimeInMillis)
        } else {
            // Permission denied
            Toast.makeText(this, "Notification permission denied. Cannot set reminder.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleNotification(timeInMillis: Long) {
        if (timeInMillis == -1L) {
            Toast.makeText(this, "Reminder time not set.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        Toast.makeText(this, "Reminder set for ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(timeInMillis))}", Toast.LENGTH_LONG).show()
        pendingReminderTimeInMillis = -1 // Reset after scheduling
    }

    private fun showUploadOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Select Document")

        AlertDialog.Builder(this)
            .setTitle("Select Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> selectImage()
                    2 -> selectDocument()
                }
            }
            .show()
    }

    private fun takePhoto() {
        if (checkCameraPermission()) {
            try {
                tempCameraFile = createInternalCameraFile()
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    tempCameraFile!!
                )
                cameraLauncher.launch(photoUri)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestCameraPermission()
        }
    }

    private fun selectImage() {
        try {
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening image picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectDocument() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                documentPickerLauncher.launch("*/*")
            } else {
                if (checkStoragePermission()) {
                    documentPickerLauncher.launch("*/*")
                } else {
                    requestStoragePermission()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening file picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createInternalCameraFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "EXPENSE_${timeStamp}.jpg"
        val storageDir = File(filesDir, "camera_photos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, imageFileName)
    }

    private fun copyFileToInternalStorage(sourceUri: Uri, callback: (Uri?) -> Unit) {
        try {
            val inputStream = contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                callback(null)
                return
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "document_${timeStamp}"
            val extension = getFileExtensionFromUri(sourceUri)
            val fullFileName = if (extension.isNotEmpty()) "${fileName}.${extension}" else fileName

            val internalDir = File(filesDir, "documents")
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }

            val internalFile = File(internalDir, fullFileName)
            val outputStream = FileOutputStream(internalFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            callback(Uri.fromFile(internalFile))
        } catch (e: Exception) {
            Toast.makeText(this, "Error copying file: ${e.message}", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun getFileExtensionFromUri(uri: Uri): String {
        return try {
            val mimeType = contentResolver.getType(uri)
            when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/gif" -> "gif"
                "application/pdf" -> "pdf"
                "application/msword" -> "doc"
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
                else -> {
                    val path = uri.path
                    if (path != null && path.contains(".")) {
                        path.substringAfterLast(".")
                    } else {
                        "file"
                    }
                }
            }
        } catch (e: Exception) {
            "file"
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    documentPickerLauncher.launch("*/*")
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            // The new notification permission request is handled by registerForActivityResult,
            // so we don't need a specific case here for NOTIFICATION_PERMISSION_REQUEST_CODE
        }
    }

    private fun setupCategoryDropdown() {
        val categories = listOf(
            "LIGHTS", "CLOTHES", "CAR", "TOILETRIES",
            "Food", "Transport", "Entertainment", "Utilities",
            "Healthcare", "Shopping", "Bills", "Others"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        spinnerCategory.setAdapter(adapter)
        spinnerCategory.threshold = 1
    }

    private fun setupDatePicker() {
        btnDatePicker.text = dateFormat.format(calendar.time)

        btnDatePicker.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                btnDatePicker.text = dateFormat.format(calendar.time)
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun saveExpenseData() {
        if (!validateInputs()) {
            return
        }

        val amountStr = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = spinnerCategory.text.toString().trim()
        val date = btnDatePicker.text.toString()
        val selectedCurrency = spinnerCurrency.selectedItem.toString()
        val conversionRate = exchangeRates[selectedCurrency] ?: 1.0
        val amount = amountStr.toDouble() * conversionRate

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Please login to save expense", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            amount = amount,
            description = description,
            category = category,
            date = date,
            timestamp = System.currentTimeMillis(),
            documentBase64 = null
        )

        if (selectedDocumentUri != null) {
            uploadDocumentThenSave(uid, expense)
        } else {
            saveToFirebase(uid, expense)
        }
    }

    private fun validateInputs(): Boolean {
        val amountStr = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val category = spinnerCategory.text.toString().trim()

        if (amountStr.isEmpty()) {
            etAmount.error = "Amount is required"
            etAmount.requestFocus()
            return false
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            etAmount.error = "Enter a valid amount greater than 0"
            etAmount.requestFocus()
            return false
        }

        if (amount > 1000000) {
            etAmount.error = "Amount seems too large. Please verify."
            etAmount.requestFocus()
            return false
        }

        if (description.isEmpty()) {
            etDescription.error = "Description is required"
            etDescription.requestFocus()
            return false
        }

        if (description.length < 3) {
            etDescription.error = "Description must be at least 3 characters"
            etDescription.requestFocus()
            return false
        }

        if (category.isEmpty()) {
            spinnerCategory.error = "Please select or enter a category"
            spinnerCategory.requestFocus()
            return false
        }

        etAmount.error = null
        etDescription.error = null
        spinnerCategory.error = null

        return true
    }

    private fun uploadDocumentThenSave(uid: String, expense: Expense) {
        if (selectedDocumentUri == null) {
            Toast.makeText(this, "No document selected", Toast.LENGTH_SHORT).show()
            resetButtonState()
            return
        }

        try {
            val inputStream = contentResolver.openInputStream(selectedDocumentUri!!)
            if (inputStream == null) {
                Toast.makeText(this, "Failed to open selected file", Toast.LENGTH_SHORT).show()
                resetButtonState()
                return
            }

            val bytes = inputStream.readBytes()
            inputStream.close()

            val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)

            val expenseWithDoc = expense.copy(documentBase64 = base64String)

            saveToFirebase(uid, expenseWithDoc)

        } catch (e: Exception) {
            Toast.makeText(this, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            resetButtonState()
        }
    }

    private fun getFileExtensionFromFile(file: File): String {
        val fileName = file.name
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".")
        } else {
            "file"
        }
    }

    private fun saveToFirebase(uid: String, expense: Expense) {
        val dbRef = FirebaseDatabase.getInstance().getReference("expenses").child(uid)
        val key = dbRef.push().key

        if (key == null) {
            Toast.makeText(this, "Failed to generate database key", Toast.LENGTH_SHORT).show()
            resetButtonState()
            return
        }

        btnSaveExpense.isEnabled = false
        btnSaveExpense.text = "Saving..."

        dbRef.child(key).setValue(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show()

                cleanupTempFiles()
                clearForm()
                updateBudgetData(expense.category, expense.amount)
                setResult(RESULT_OK)
                redirectToTransactions()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to save expense: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnCompleteListener {
                resetButtonState()
            }
    }

    private fun cleanupTempFiles() {
        try {
            selectedDocumentUri?.let { uri ->
                if (uri.scheme == "file") {
                    File(uri.path!!).delete()
                }
            }
            tempCameraFile?.delete()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    private fun updateBudgetData(category: String, amount: Double) {
        val dashboardCategories = listOf("LIGHTS", "CLOTHES", "CAR", "TOILETRIES")

        if (category.uppercase() in dashboardCategories) {
            val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date()).lowercase()
            // You can implement this similar to your original logic
            // but simplified without the complex timeout handling
        }
    }

    private fun resetButtonState() {
        btnSaveExpense.isEnabled = true
        btnSaveExpense.text = "ADD EXPENSE"
    }

    private fun redirectToTransactions() {
        val intent = Intent(this, Transaction::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun clearForm() {
        etAmount.text.clear()
        etDescription.text.clear()
        spinnerCategory.setText("", false)
        calendar.time = Calendar.getInstance().time
        btnDatePicker.text = dateFormat.format(calendar.time)
        btnUploadDocument.text = "Upload Document (Optional)"
        btnUploadDocument.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        selectedDocumentUri = null
        tempCameraFile = null

        etAmount.clearFocus()
        etDescription.clearFocus()
        spinnerCategory.clearFocus()
    }

    override fun onBackPressed() {
        val hasUnsavedData = etAmount.text.isNotEmpty() ||
                etDescription.text.isNotEmpty() ||
                spinnerCategory.text.isNotEmpty() ||
                selectedDocumentUri != null

        if (hasUnsavedData) {
            AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Are you sure you want to go back?")
                .setPositiveButton("Yes") { _, _ ->
                    cleanupTempFiles()
                    super.onBackPressed()
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            cleanupTempFiles()
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupTempFiles()
    }

    private val documentPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            copyFileToInternalStorage(uri) { internalUri ->
                if (internalUri != null) {
                    selectedDocumentUri = internalUri
                    btnUploadDocument.text = "Document Selected ✓"
                    btnUploadDocument.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                    Toast.makeText(this, "Document selected successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to process selected document", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No document selected", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            copyFileToInternalStorage(uri) { internalUri ->
                if (internalUri != null) {
                    selectedDocumentUri = internalUri
                    btnUploadDocument.text = "Image Selected ✓"
                    btnUploadDocument.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                    Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to process selected image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            selectedDocumentUri = Uri.fromFile(tempCameraFile)
            btnUploadDocument.text = "Photo Taken ✓"
            btnUploadDocument.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
            Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show()
        } else {
            selectedDocumentUri = null
            tempCameraFile?.delete()
            tempCameraFile = null
            Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }
}