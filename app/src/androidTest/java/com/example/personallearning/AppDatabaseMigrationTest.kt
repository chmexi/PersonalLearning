package com.example.personallearning

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.personallearning.data.local.AppDatabase
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @Test
    fun migration56AddsAnalysisColumns() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseFile = File(context.getDatabasePath("migration-v5.db").path)
        databaseFile.parentFile?.mkdirs()
        databaseFile.delete()
        val seed = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        seed.execSQL("CREATE TABLE daohen_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date TEXT NOT NULL, q1 TEXT NOT NULL DEFAULT '', q2 TEXT NOT NULL DEFAULT '', q3 TEXT NOT NULL DEFAULT '', q4 TEXT NOT NULL DEFAULT '', q5 TEXT NOT NULL DEFAULT '', q6 TEXT NOT NULL DEFAULT '', q7 TEXT NOT NULL DEFAULT '', syncStatus INTEGER NOT NULL DEFAULT 0, serverRevision INTEGER NOT NULL DEFAULT 0, tags TEXT NOT NULL DEFAULT '', actionStatus INTEGER NOT NULL DEFAULT 0, actionNote TEXT NOT NULL DEFAULT '')")
        seed.execSQL("CREATE TABLE express_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date TEXT NOT NULL, retelling TEXT NOT NULL, keywords TEXT NOT NULL, observation TEXT NOT NULL, listening TEXT NOT NULL, compression TEXT NOT NULL, duration INTEGER NOT NULL, syncStatus INTEGER NOT NULL)")
        seed.version = 5
        seed.close()

        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseFile.name)
            .addMigrations(AppDatabase.MIGRATION_5_6)
            .build()
        val columns = database.openHelper.writableDatabase.readColumns("daohen_entries")
        database.close()
        databaseFile.delete()
        assertTrue(columns.containsAll(listOf("transcript", "facts", "emotions", "stone", "betterChoice", "aiQuestion", "analysisSource", "analyzedAt")))
    }
}

private fun SupportSQLiteDatabase.readColumns(table: String): Set<String> {
    val result = linkedSetOf<String>()
    query("PRAGMA table_info($table)").use { cursor ->
        val nameIndex = cursor.getColumnIndexOrThrow("name")
        while (cursor.moveToNext()) result += cursor.getString(nameIndex)
    }
    return result
}
