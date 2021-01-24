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
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Setting
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
        PatchEntity::class.java.canonicalName,
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
    fun migration() {
        val sqliteOpenHelper = createSqliteOpenHelper()

        val settings = listOf(
            Setting("1", "2", 1, 2, .1f, .2f),
            Setting("3", "4", 3, 4, .3f, .4f),
            Setting("5", "6", 5, 6, .5f, .6f),
            Setting("7", "8", 7, 8, .7f, .8f)
        )
        val patch = Patch(
            title = "test",
            sequence = Patch.RANDOM_SEQUENCE,
            channels = listOf("bd", "sd", "cp", "tt", "cb", "hh").map { Channel(it, settings, 0, 0.5f) },
            selectedChannel = 1,
            tempo = 123,
            swing = 10
        )
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
            3,
            true,
            PatchDatabase.MIGRATION_2_3
        )

        val patchDatabase = DbModule.providePatchDatabase(ApplicationProvider.getApplicationContext())
        migrationTestHelper.closeWhenFinished(patchDatabase)

        val migratedPatch = runBlocking { PatchRepository(patchDatabase.patchDao()).activePatch().first() }
        assertThat(migratedPatch).isEqualTo(patch)
    }

    @Test
    fun defaultPatch() {
        val patchDatabase = DbModule.providePatchDatabase(ApplicationProvider.getApplicationContext())
        migrationTestHelper.closeWhenFinished(patchDatabase)

        val defaultPatch = runBlocking { PatchRepository(patchDatabase.patchDao()).activePatch().first() }

        assertThat(defaultPatch.title).isEqualTo("untitled")
        assertThat(defaultPatch.sequence).isEqualTo(Patch.EMPTY_SEQUENCE)
        assertThat(defaultPatch.channels[0].name).isEqualTo("bd")
        assertThat(defaultPatch.channels[0].settings[0].position.y).isEqualTo(0.49f)
        assertThat(defaultPatch.selectedChannel).isEqualTo(Channel.NONE_ID)
        assertThat(defaultPatch.tempo).isEqualTo(120)
        assertThat(defaultPatch.swing).isEqualTo(0)
    }
}
