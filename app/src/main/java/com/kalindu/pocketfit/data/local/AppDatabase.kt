package com.kalindu.pocketfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kalindu.pocketfit.data.model.ActivitySession

@Database(entities = [ActivitySession::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS activities")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        plannedDurationMinutes INTEGER NOT NULL,
                        stepGoal INTEGER,
                        calorieGoal INTEGER,
                        startTimeMillis INTEGER NOT NULL,
                        endTimeMillis INTEGER,
                        status TEXT NOT NULL,
                        completionReason TEXT,
                        stepBaseline INTEGER,
                        steps INTEGER NOT NULL,
                        calories INTEGER NOT NULL,
                        actualDurationSeconds INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE sessions " +
                        "ADD COLUMN weightUsedKg REAL NOT NULL DEFAULT 70.0"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocketfit_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
