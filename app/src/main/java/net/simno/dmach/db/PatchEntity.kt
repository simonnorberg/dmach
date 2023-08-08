package net.simno.dmach.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patch",
    indices = [
        Index("title", unique = true)
    ]
)
data class PatchEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Int?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "sequence") val sequence: String,
    @ColumnInfo(name = "muted") val muted: String,
    @ColumnInfo(name = "channels") val channels: String,
    @ColumnInfo(name = "selected") val selected: Int,
    @ColumnInfo(name = "tempo") val tempo: Int,
    @ColumnInfo(name = "swing") val swing: Int,
    @ColumnInfo(name = "steps") val steps: Int,
    @ColumnInfo(name = "active") val active: Boolean
)
