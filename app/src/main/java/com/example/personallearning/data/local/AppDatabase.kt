package com.example.personallearning.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.data.model.ExpressEntry

@Database(entities = [DaoHenEntry::class, ExpressEntry::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun daoHenDao(): DaoHenDao
    abstract fun expressDao(): ExpressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS express_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        retelling TEXT NOT NULL,
                        keywords TEXT NOT NULL,
                        observation TEXT NOT NULL,
                        listening TEXT NOT NULL,
                        compression TEXT NOT NULL,
                        duration INTEGER NOT NULL,
                        syncStatus INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val fields = listOf("q1", "q2", "q3", "q4", "q5", "q6", "q7")
                fields.forEach { field ->
                    db.execSQL(
                        """
                        UPDATE daohen_entries
                        SET $field = COALESCE(
                            (
                                SELECT source.$field
                                FROM daohen_entries AS source
                                WHERE source.date = daohen_entries.date
                                  AND source.$field != ''
                                ORDER BY source.id DESC
                                LIMIT 1
                            ),
                            $field
                        )
                        WHERE id IN (
                            SELECT MAX(id)
                            FROM daohen_entries
                            GROUP BY date
                        )
                        """.trimIndent()
                    )
                }

                db.execSQL(
                    """
                    UPDATE daohen_entries
                    SET syncStatus = CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM daohen_entries AS source
                            WHERE source.date = daohen_entries.date
                              AND source.syncStatus = 0
                        ) THEN 0
                        ELSE syncStatus
                    END
                    WHERE id IN (
                        SELECT MAX(id)
                        FROM daohen_entries
                        GROUP BY date
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    DELETE FROM daohen_entries
                    WHERE id NOT IN (
                        SELECT MAX(id)
                        FROM daohen_entries
                        GROUP BY date
                    )
                    """.trimIndent()
                )

                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daohen_entries_date ON daohen_entries(date)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE daohen_entries ADD COLUMN serverRevision INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daohen_entries ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE daohen_entries ADD COLUMN actionStatus INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE daohen_entries ADD COLUMN actionNote TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf("transcript", "facts", "emotions", "stone", "betterChoice", "aiQuestion", "analysisSource", "analyzedAt").forEach { field ->
                    db.execSQL("ALTER TABLE daohen_entries ADD COLUMN $field TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_learning.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
