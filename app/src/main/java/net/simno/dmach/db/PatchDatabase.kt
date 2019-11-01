package net.simno.dmach.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PatchEntity::class], version = 3)
abstract class PatchDatabase : RoomDatabase() {

    abstract fun patchDao(): PatchDao

    companion object {
        const val NAME = "dmach.db"

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS patch")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE patch ADD active INTEGER NOT NULL DEFAULT 1;")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_patch_title ON patch (title)")
            }
        }
    }
}
