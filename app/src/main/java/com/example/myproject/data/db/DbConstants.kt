package com.example.myproject.data.db

import android.provider.BaseColumns

object DbConstants : BaseColumns {
    const val NOTES_TABLE = "my_notes"
    const val TYPE = "type"
    const val TITLE = "title"
    const val CONTENT = "content"
    const val EDIT_TIME = "edit_time"
    const val IS_TOP = "is_top"
    const val WALLPAPER = "wallpaper"
    const val REMOVAL_TIME = "removal_time"
    const val IS_CHECKED = "is_checked"

    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "NotesDb.db"

    const val CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS $NOTES_TABLE (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, $TYPE TEXT CHECK( TYPE IN ('IS_NORMAL', 'IS_HIDDEN', 'IS_TRASHED') ) NOT NULL DEFAULT 'IS_NORMAL', $TITLE TEXT, $CONTENT TEXT, $EDIT_TIME TEXT, $IS_TOP INTEGER, $WALLPAPER TEXT, $REMOVAL_TIME INTEGER)"

    const val DELETE_NOTES_TABLE = "DROP TABLE IF EXISTS $NOTES_TABLE"

}
