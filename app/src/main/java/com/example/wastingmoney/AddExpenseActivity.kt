package com.example.wastingmoney

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.fake.wastingmoney.R
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var spinnerCurrency: Spinner
    private lateinit var btnSetReminder: Button
    private lateinit var btnSaveExpense: Button

    private val calendar = Calendar.getInstance()

    private val exchangeRates = mapOf(
        "ZAR" to 1.0,
        "USD" to 18.5,
        "EUR" to 20.1
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        etAmount = findViewById(R.id.et_amount)
        spinnerCurrency = findViewById(R.id.spinner_currency)
        btnSetReminder = findViewById(R.id.btn_set_reminder)
        btnSaveExpense = findViewById(R.id.btn_save_expense)

        // Setup currency spinner
        val currencyAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.currencies,
            android.R.layout.simple_spinner_item
        )
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = currencyAdapter

        // Set reminder click
        btnSetReminder.setOnClickListener {
            showDateTimePicker()
        }

        // Save expense
        btnSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    private fun showDateTimePicker() {
        val datePicker = DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(this,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        scheduleNotification(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true)
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun scheduleNotification(timeInMillis: Long) {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show()
    }

    private fun saveExpense() {
        val amountStr = etAmount.text.toString()
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCurrency = spinnerCurrency.selectedItem.toString()
        val conversionRate = exchangeRates[selectedCurrency] ?: 1.0
        val amountInZAR = amountStr.toDouble() * conversionRate

        // Save amountInZAR to database here (replace this with your logic)
        Toast.makeText(this, "Saved: R %.2f".format(amountInZAR), Toast.LENGTH_LONG).show()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

    }
}