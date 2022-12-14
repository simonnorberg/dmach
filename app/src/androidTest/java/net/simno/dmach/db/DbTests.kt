package net.simno.dmach.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.simno.dmach.data.defaultPatch
import net.simno.dmach.db.PatchRepository.Companion.toEntity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DbTests {

    @Rule
    @JvmField
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PatchDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    fun setup() {
        deleteDatabase()
    }

    @After
    fun tearDown() {
        deleteDatabase()
    }

    private fun deleteDatabase() {
        val context = ApplicationProvider.getApplicationContext() as Context
        context.deleteDatabase(PatchDatabase.NAME)
    }

    private fun createSqliteOpenHelper(): SupportSQLiteOpenHelper {
        val callback = object : SupportSQLiteOpenHelper.Callback(PatchTable.VERSION) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(PatchTable.CREATE_TABLE)
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            }

            override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            }
        }
        val configuration = SupportSQLiteOpenHelper.Configuration
            .builder(ApplicationProvider.getApplicationContext())
            .name(PatchDatabase.NAME)
            .callback(callback)
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(configuration)
    }

    @Test
    fun dbMigration() {
        val sqliteOpenHelper = createSqliteOpenHelper()

        val patch = defaultPatch()
        val entity = runBlocking { patch.toEntity(patch.title) }

        val values = contentValuesOf(
            PatchTable.TITLE to entity.title,
            PatchTable.SEQUENCE to entity.sequence,
            PatchTable.CHANNELS to entity.channels,
            PatchTable.SELECTED to entity.selected,
            PatchTable.TEMPO to entity.tempo,
            PatchTable.SWING to entity.swing
        )

        val db = sqliteOpenHelper.writableDatabase
        db.insert(PatchTable.TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE, values)
        db.close()

        migrationTestHelper.runMigrationsAndValidate(
            PatchDatabase.NAME,
            4,
            true,
            PatchDatabase.MIGRATION_2_3,
            PatchDatabase.MIGRATION_3_4
        )

        val patchDatabase = DbModule.providePatchDatabase(ApplicationProvider.getApplicationContext())
        migrationTestHelper.closeWhenFinished(patchDatabase)

        val migratedPatch = runBlocking { PatchRepository(patchDatabase.patchDao()).activePatch().first() }
        assertThat(migratedPatch).isEqualTo(patch)
    }

    @Test
    fun dbDefaultPatch() {
        val patchDatabase = DbModule.providePatchDatabase(ApplicationProvider.getApplicationContext())
        migrationTestHelper.closeWhenFinished(patchDatabase)

        val defaultPatch = runBlocking { PatchRepository(patchDatabase.patchDao()).activePatch().first() }
        assertThat(defaultPatch).isEqualTo(defaultPatch())
    }
}
