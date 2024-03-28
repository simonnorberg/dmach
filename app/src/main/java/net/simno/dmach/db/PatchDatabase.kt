package net.simno.dmach.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PatchEntity::class],
    version = 5,
    exportSchema = true
)
abstract class PatchDatabase : RoomDatabase() {

    abstract fun patchDao(): PatchDao

    companion object {
        const val NAME = "dmach.db"

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patch ADD active INTEGER NOT NULL DEFAULT 1;")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_patch_title ON patch (title)")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patch ADD steps INTEGER NOT NULL DEFAULT 16;")
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patch ADD muted TEXT NOT NULL DEFAULT '[]';")
            }
        }
    }
}
