package com.example.myproject.data.db

import android.provider.BaseColumns

object DbConstants : BaseColumns {
    const val NOTES_TABLE = "my_notes"
    const val TITLE = "title"
    const val CONTENT = "content"
    const val EDIT_TIME = "edit_time"
    const val IS_TOP = "is_top"
    const val WALLPAPER = "wallpaper"


    const val HIDDEN_TABLE = "hidden_notes"
    const val HIDDEN_TITLE = "hidden_title"
    const val HIDDEN_CONTENT = "hidden_content"
    const val HIDDEN_EDIT_TIME = "hidden_edit_time"
    const val HIDDEN_IS_TOP = "hidden_is_top"
    const val HIDDEN_WALLPAPER = "hidden_wallpaper"


    const val TRASH_TABLE = "trash_table"
    const val TRASH_TITLE = "trash_title"
    const val TRASH_CONTENT = "trash_content"
    const val TRASH_EDIT_TIME = "trash_edit_time"
    const val TRASH_IS_TOP = "trash_is_top"
    const val TRASH_WALLPAPER = "trash_wallpaper"
    const val REMOVAL_TIME = "removal_time"

    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "NotesDb.db"

    const val CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS $NOTES_TABLE (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, $TITLE TEXT, $CONTENT TEXT, $EDIT_TIME TEXT, $IS_TOP INTEGER, $WALLPAPER TEXT)"

    const val CREATE_HIDDEN_TABLE = "CREATE TABLE IF NOT EXISTS $HIDDEN_TABLE (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, $HIDDEN_TITLE TEXT, $HIDDEN_CONTENT TEXT, $HIDDEN_EDIT_TIME TEXT, $HIDDEN_IS_TOP INTEGER, $HIDDEN_WALLPAPER TEXT)"


    const val CREATE_TRASH_TABLE = "CREATE TABLE IF NOT EXISTS $TRASH_TABLE (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, $TRASH_TITLE TEXT, $TRASH_CONTENT TEXT, $TRASH_EDIT_TIME TEXT, $TRASH_IS_TOP INTEGER, $TRASH_WALLPAPER TEXT, $REMOVAL_TIME INTEGER)"



    const val DELETE_NOTES_TABLE = "DROP TABLE IF EXISTS $NOTES_TABLE"
    const val DELETE_TRASH_BASKET_TABLE = "DROP TABLE IF EXISTS $TRASH_TABLE"
    const val DELETE_HIDDEN_TABLE = "DROP TABLE IF EXISTS $HIDDEN_TABLE"
}
