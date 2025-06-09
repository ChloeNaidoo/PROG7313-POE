package com.fake.wastingmoney.model

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

data class User(
    val id: Long = 0,
    val username: String,
    val password: String )