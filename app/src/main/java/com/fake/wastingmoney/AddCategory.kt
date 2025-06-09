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

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddCategory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_category)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}