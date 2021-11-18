package com.example.myproject.data.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import com.example.myproject.project.note.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DbManager(private val dbHelper: DbHelper) {
    private var db: SQLiteDatabase? = null

    companion object {
        fun newInstance(dbHelper: DbHelper) = DbManager(dbHelper)
    }

    fun openDb() {

        db = dbHelper.writableDatabase
        Log.d("DDDDDD", "OPEN : $db, ${db?.isDbLockedByCurrentThread}")

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

    suspend fun readDataFromTable(searchText: String, typeName: String): ArrayList<Note> =
        withContext(Dispatchers.IO) {
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
            return@withContext dataList
        }


    fun removeItem(note: Note) {
        val selection = BaseColumns._ID + "=${note.id}"
        db?.delete(DbConstants.NOTES_TABLE, selection, null)
    }

    fun emptyList(typeName: String) {
        db?.delete(
            DbConstants.NOTES_TABLE,
            "${DbConstants.TYPE} = ?",
            arrayOf(typeName)
        )
    }

    fun closeDb() {
        Log.d("DDDDDD", "CLOSE : $db, ${db?.isDbLockedByCurrentThread}")

        dbHelper.close()
    }
}
