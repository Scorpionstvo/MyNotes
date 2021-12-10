package com.example.myproject.data.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import com.example.myproject.project.data.Note

class DbManager(private val dbHelper: DbHelper) {
    private var db: SQLiteDatabase? = null

    companion object {
        fun newInstance(dbHelper: DbHelper) = DbManager(dbHelper)
    }

    fun openDb() {

        db = dbHelper.writableDatabase

    }

    fun insertToTable(note: Note) {
        val isTop = if (note.isTop) 1 else 0
        val values = ContentValues().apply {
            put(DbConstants.TYPE, note.typeName)
            put(DbConstants.TITLE, note.title)
            put(DbConstants.CONTENT, note.content)
            put(DbConstants.EDIT_TIME, note.editTime)
            put(DbConstants.IS_TOP, isTop)
            put(DbConstants.WALLPAPER, note.wallpaperName)
            put(DbConstants.REMOVAL_TIME, note.removalTime)
        }
        db?.insert(DbConstants.NOTES_TABLE, null, values)
    }

    fun updateItem(note: Note) {
        val isTop = if (note.isTop) 1 else 0
        val selection = BaseColumns._ID + "=${note.id}"
        val values = ContentValues().apply {
            put(DbConstants.TYPE, note.typeName)
            put(DbConstants.TITLE, note.title)
            put(DbConstants.CONTENT, note.content)
            put(DbConstants.EDIT_TIME, note.editTime)
            put(DbConstants.IS_TOP, isTop)
            put(DbConstants.WALLPAPER, note.wallpaperName)
            put(DbConstants.REMOVAL_TIME, note.removalTime)
        }
        db?.update(DbConstants.NOTES_TABLE, values, selection, null)
    }

    fun readDataFromTable(searchText: String, typeName: String): ArrayList<Note> {

        val dataList = ArrayList<Note>()
        val selection =
            "${DbConstants.CONTENT} || ${DbConstants.TITLE} like ? AND ${DbConstants.TYPE} like ?"
        val order = "${DbConstants.IS_TOP}  DESC, ${DbConstants.EDIT_TIME} DESC"

        val cursor = db?.query(
            DbConstants.NOTES_TABLE,
            null,
            selection,
            arrayOf("%$searchText%", typeName),
            null,
            null,
            order
        )

        while (cursor?.moveToNext()!!) {
            val type = cursor.getString(cursor.getColumnIndex(DbConstants.TYPE))
            val title = cursor.getString(cursor.getColumnIndex(DbConstants.TITLE))
            val content = cursor.getString(cursor.getColumnIndex(DbConstants.CONTENT))
            val id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
            val editTime = cursor.getString(cursor.getColumnIndex(DbConstants.EDIT_TIME))
            val isTopInt = cursor.getInt(cursor.getColumnIndex(DbConstants.IS_TOP))
            val isTop = isTopInt == 1
            val wallpaper = cursor.getString(cursor.getColumnIndex(DbConstants.WALLPAPER))
            val removalTime = cursor.getLong(cursor.getColumnIndex(DbConstants.REMOVAL_TIME))
            dataList.add(
                Note(
                    type,
                    title,
                    content,
                    id,
                    editTime,
                    isTop,
                    wallpaper,
                    removalTime
                )
            )
        }
        cursor.close()
        return dataList
    }


    fun removeItem(id: Int) {
        val selection = BaseColumns._ID + "=$id"
        db?.delete(DbConstants.NOTES_TABLE, selection, null)
    }

    fun closeDb() {
        dbHelper.close()
    }
}
