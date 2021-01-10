package net.simno.dmach.db

object PatchTable {
    const val VERSION = 2
    const val TABLE_NAME = "patch"
    private const val ID = "_id"
    const val TITLE = "title"
    const val SEQUENCE = "sequence"
    const val CHANNELS = "channels"
    const val SELECTED = "selected"
    const val TEMPO = "tempo"
    const val SWING = "swing"

    const val CREATE_TABLE = "create table " + TABLE_NAME +
        "(" +
        ID + " integer primary key autoincrement, " +
        TITLE + " text unique not null, " +
        SEQUENCE + " text not null, " +
        CHANNELS + " text not null, " +
        SELECTED + " integer not null, " +
        TEMPO + " integer not null, " +
        SWING + " integer not null " +
        ");"
}
