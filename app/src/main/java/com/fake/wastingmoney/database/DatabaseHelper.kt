package com.fake.wastingmoney.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
<<<<<<< HEAD
import com.fake.wastingmoney.model.*
=======
import com.fake.wastingmoney.model.* // Assuming these models (User, Category, etc.) are in model package
>>>>>>> 0542f51 (final)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BudgetTracker.db"
<<<<<<< HEAD
        private const val DATABASE_VERSION = 1
=======
        private const val DATABASE_VERSION = 2 // Increment database version for schema changes!
>>>>>>> 0542f51 (final)

        // Table names
        private const val TABLE_USERS = "users"
        private const val TABLE_CATEGORIES = "categories"
        private const val TABLE_EXPENSES = "expenses"
        private const val TABLE_BUDGET_GOALS = "budget_goals"
<<<<<<< HEAD
=======
        private const val TABLE_STREAKS = "streaks" // NEW TABLE
>>>>>>> 0542f51 (final)

        // Common columns
        private const val KEY_ID = "id"

        // User table columns
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"

        // Category table columns
        private const val KEY_CATEGORY_NAME = "name"
<<<<<<< HEAD
        private const val KEY_USER_ID = "user_id"
=======
        private const val KEY_USER_ID = "user_id" // Re-used for foreign keys
>>>>>>> 0542f51 (final)

        // Expense table columns
        private const val KEY_DATE = "date"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_END_TIME = "end_time"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_CATEGORY_ID = "category_id"
        private const val KEY_PHOTO_PATH = "photo_path"

        // Budget goal table columns
        private const val KEY_MIN_GOAL = "min_monthly_goal"
        private const val KEY_MAX_GOAL = "max_monthly_goal"
        private const val KEY_MONTH = "month"
        private const val KEY_YEAR = "year"
<<<<<<< HEAD
=======

        // Streak table columns
        private const val KEY_STREAK_COUNT = "streak_count" // NEW
        private const val KEY_LAST_LOGIN_DATE = "last_login_date" // NEW
>>>>>>> 0542f51 (final)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_USERNAME TEXT UNIQUE NOT NULL,
                $KEY_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()

        // Create categories table
        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_CATEGORY_NAME TEXT NOT NULL,
                $KEY_USER_ID INTEGER NOT NULL,
                FOREIGN KEY($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_ID)
            )
        """.trimIndent()

        // Create expenses table
        val createExpensesTable = """
            CREATE TABLE $TABLE_EXPENSES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_DATE TEXT NOT NULL,
                $KEY_START_TIME TEXT NOT NULL,
                $KEY_END_TIME TEXT NOT NULL,
                $KEY_DESCRIPTION TEXT NOT NULL,
                $KEY_AMOUNT REAL NOT NULL,
                $KEY_CATEGORY_ID INTEGER NOT NULL,
                $KEY_USER_ID INTEGER NOT NULL,
                $KEY_PHOTO_PATH TEXT,
                FOREIGN KEY($KEY_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($KEY_ID),
                FOREIGN KEY($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_ID)
            )
        """.trimIndent()

        // Create budget goals table
        val createBudgetGoalsTable = """
            CREATE TABLE $TABLE_BUDGET_GOALS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_USER_ID INTEGER NOT NULL,
                $KEY_MIN_GOAL REAL NOT NULL,
                $KEY_MAX_GOAL REAL NOT NULL,
                $KEY_MONTH TEXT NOT NULL,
                $KEY_YEAR INTEGER NOT NULL,
                FOREIGN KEY($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_ID)
            )
        """.trimIndent()

<<<<<<< HEAD
=======
        // Create streaks table (NEW)
        val createStreaksTable = """
            CREATE TABLE $TABLE_STREAKS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_USER_ID INTEGER UNIQUE NOT NULL,
                $KEY_STREAK_COUNT INTEGER NOT NULL DEFAULT 0,
                $KEY_LAST_LOGIN_DATE TEXT NOT NULL,
                FOREIGN KEY($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_ID) ON DELETE CASCADE
            )
        """.trimIndent()

>>>>>>> 0542f51 (final)
        db.execSQL(createUsersTable)
        db.execSQL(createCategoriesTable)
        db.execSQL(createExpensesTable)
        db.execSQL(createBudgetGoalsTable)
<<<<<<< HEAD
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
=======
        db.execSQL(createStreaksTable) // Execute new table creation
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop existing tables in reverse order of foreign key dependencies
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STREAKS") // Drop new table
>>>>>>> 0542f51 (final)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

<<<<<<< HEAD
    // User operations
=======
    // User operations (Existing)
>>>>>>> 0542f51 (final)
    fun addUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USERNAME, username)
            put(KEY_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result
    }

    fun validateUser(username: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_ID, KEY_USERNAME, KEY_PASSWORD),
            "$KEY_USERNAME = ? AND $KEY_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2)
            )
            cursor.close()
            db.close()
            user
        } else {
            cursor.close()
            db.close()
            null
        }
    }

<<<<<<< HEAD
    // Category operations
=======
    /**
     * Retrieves the SQLite user ID for a given username (email).
     * This is crucial for linking Firebase Auth users to local SQLite data.
     */
    fun getUserIdByUsername(username: String): Long? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_ID),
            "$KEY_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        val userId: Long? = if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else {
            null
        }
        cursor.close()
        db.close()
        return userId
    }

    // Category operations (Existing)
>>>>>>> 0542f51 (final)
    fun addCategory(name: String, userId: Long): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_CATEGORY_NAME, name)
            put(KEY_USER_ID, userId)
        }
        val result = db.insert(TABLE_CATEGORIES, null, values)
        db.close()
        return result
    }

    fun getCategoriesForUser(userId: Long): List<Category> {
        val categories = mutableListOf<Category>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(KEY_ID, KEY_CATEGORY_NAME, KEY_USER_ID),
            "$KEY_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, KEY_CATEGORY_NAME
        )

        if (cursor.moveToFirst()) {
            do {
                categories.add(
                    Category(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return categories
    }
<<<<<<< HEAD
=======

    // NEW STREAK OPERATIONS

    /**
     * Retrieves the streak count and last login date for a given user.
     * Returns a Pair<Long, String> (streakCount, lastLoginDate) or null if no data.
     */
    fun getStreakData(userId: Long): Pair<Long, String>? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STREAKS,
            arrayOf(KEY_STREAK_COUNT, KEY_LAST_LOGIN_DATE),
            "$KEY_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        var streakData: Pair<Long, String>? = null
        if (cursor.moveToFirst()) {
            val streakCount = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_STREAK_COUNT))
            val lastLoginDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_LOGIN_DATE))
            streakData = Pair(streakCount, lastLoginDate)
        }
        cursor.close()
        db.close()
        return streakData
    }

    /**
     * Inserts initial streak data for a new user.
     * Returns the row ID of the newly inserted row, or -1 if an error occurred.
     */
    fun insertInitialStreakData(userId: Long, lastLoginDate: String, initialStreakCount: Long = 1L): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_ID, userId)
            put(KEY_STREAK_COUNT, initialStreakCount)
            put(KEY_LAST_LOGIN_DATE, lastLoginDate)
        }
        val result = db.insert(TABLE_STREAKS, null, values)
        db.close()
        return result
    }

    /**
     * Updates existing streak data for a user.
     * Returns the number of rows affected.
     */
    fun updateStreakData(userId: Long, newStreakCount: Long, newLastLoginDate: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_STREAK_COUNT, newStreakCount)
            put(KEY_LAST_LOGIN_DATE, newLastLoginDate)
        }
        val affectedRows = db.update(
            TABLE_STREAKS,
            values,
            "$KEY_USER_ID = ?",
            arrayOf(userId.toString())
        )
        db.close()
        return affectedRows
    }
>>>>>>> 0542f51 (final)
}