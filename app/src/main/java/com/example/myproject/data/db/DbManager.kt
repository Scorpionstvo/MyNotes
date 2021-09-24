package com.example.myproject.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import com.example.myproject.project.note.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DbManager(context: Context) {
    private val dbHelper = DbCreator(context)
    var db: SQLiteDatabase? = null

    fun openDb() {
        db = dbHelper.writableDatabase
    }

    fun insertToTable(note: Note) {
        val isTop = if (note.isTop) 1 else 0
        val values = ContentValues().apply {
            put(DbConstants.TITLE, note.title)
            put(DbConstants.CONTENT, note.content)
            put(DbConstants.EDIT_TIME, note.editTime)
            put(DbConstants.IS_TOP, isTop)
            put(DbConstants.WALLPAPER, note.wallpaperName)
        }
        db?.insert(DbConstants.NOTES_TABLE, null, values)
    }

    fun updateToHiddenTable(note: Note) {
        val isTop = if (note.isTop) 1 else 0
        val selection = BaseColumns._ID + "=${note.id}"
        val values = ContentValues().apply {
            put(DbConstants.HIDDEN_TITLE, note.title)
            put(DbConstants.HIDDEN_CONTENT, note.content)
            put(DbConstants.HIDDEN_EDIT_TIME, note.editTime)
            put(DbConstants.HIDDEN_IS_TOP, isTop)
            put(DbConstants.HIDDEN_WALLPAPER, note.wallpaperName)
        }
        db?.update(DbConstants.HIDDEN_TABLE, values, selection, null)
    }

    fun updateItem(note: Note) {
        val isTop = if (note.isTop) 1 else 0
        val selection = BaseColumns._ID + "=${note.id}"
        val values = ContentValues().apply {
            put(DbConstants.TITLE, note.title)
            put(DbConstants.CONTENT, note.content)
            put(DbConstants.EDIT_TIME, note.editTime)
            put(DbConstants.IS_TOP, isTop)
            put(DbConstants.WALLPAPER, note.wallpaperName)
        }
        db?.update(DbConstants.NOTES_TABLE, values, selection, null)
    }

    suspend fun readDataFromNotesTable(searchText: String): ArrayList<Note> =
        withContext(Dispatchers.IO) {
            val dataList = ArrayList<Note>()
            val selection = "${DbConstants.CONTENT} || ${DbConstants.TITLE} like ? "

            val cursor = db?.query(
                    DbConstants.NOTES_TABLE,
                null,
                selection,
                arrayOf("%$searchText%"),
                null,
                null,
                null
            )

            while (cursor?.moveToNext()!!) {
                val titleData = cursor.getString(cursor.getColumnIndex(DbConstants.TITLE))
                val contentData = cursor.getString(cursor.getColumnIndex(DbConstants.CONTENT))
                val dataId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
                val editTime = cursor.getString(cursor.getColumnIndex(DbConstants.EDIT_TIME))
                val isTopInt = cursor.getInt(cursor.getColumnIndex(DbConstants.IS_TOP))
                val isTop = isTopInt == 1
                val wallpaperName = cursor.getString(cursor.getColumnIndex(DbConstants.WALLPAPER))
                dataList.add(Note(titleData, contentData, dataId, editTime, isTop, wallpaperName))
            }
            cursor.close()
            return@withContext dataList
        }


    fun removeItemFromHiddenFragment(note: Note) {
        insertToTrashTable(note)
        val selection = BaseColumns._ID + "=${note.id}"
        db?.delete(DbConstants.HIDDEN_TABLE, selection, null)
    }

    fun removeItem(note: Note) {
        insertToTrashTable(note)
        val selection = BaseColumns._ID + "=${note.id}"
        db?.delete(DbConstants.NOTES_TABLE, selection, null)
    }

    private fun insertToTrashTable(note: Note) {
        val isTop = if (note.isTop) 1 else 0
        val removalTime: Long = System.currentTimeMillis()
        val values = ContentValues().apply {
            put(DbConstants.TRASH_TITLE, note.title)
            put(DbConstants.TRASH_CONTENT, note.content)
            put(DbConstants.TRASH_EDIT_TIME, note.editTime)
            put(DbConstants.TRASH_IS_TOP, isTop)
            put(DbConstants.TRASH_WALLPAPER, note.wallpaperName)
            put(DbConstants.REMOVAL_TIME, removalTime)
        }
        db?.insert(DbConstants.TRASH_TABLE, null, values)
    }

    fun moveToPersonalFolder(note: Note) {
        val values = ContentValues().apply {
            put(DbConstants.HIDDEN_TITLE, note.title)
            put(DbConstants.HIDDEN_CONTENT, note.content)
            put(DbConstants.HIDDEN_EDIT_TIME, note.editTime)
            put(DbConstants.HIDDEN_IS_TOP, note.isTop)
            put(DbConstants.HIDDEN_WALLPAPER, note.wallpaperName)
        }

        db?.insert(DbConstants.HIDDEN_TABLE, null, values)
        removeItem(note)
    }


    suspend fun readDataFromHiddenTable(searchText: String): ArrayList<Note> =
        withContext(Dispatchers.IO) {
            val dataList = ArrayList<Note>()
            val selection = "${DbConstants.HIDDEN_TITLE} || ${DbConstants.HIDDEN_CONTENT} like?"
            val cursor = db?.query(
                    DbConstants.HIDDEN_TABLE,
                null,
                selection,
                arrayOf("%$searchText%"),
                null,
                null,
                null
            )

            while (cursor?.moveToNext()!!) {
                val titleData = cursor.getString(cursor.getColumnIndex(DbConstants.HIDDEN_TITLE))
                val contentData = cursor.getString(cursor.getColumnIndex(DbConstants.HIDDEN_CONTENT))
                val dataId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
                val editTime = cursor.getString(cursor.getColumnIndex(DbConstants.HIDDEN_EDIT_TIME))
                val isTopInt = cursor.getInt(cursor.getColumnIndex(DbConstants.HIDDEN_IS_TOP))
                val isTop = isTopInt == 1
                val wallpaperName = cursor.getString(cursor.getColumnIndex(DbConstants.HIDDEN_WALLPAPER))
                val note = Note(titleData, contentData, dataId, editTime, isTop, wallpaperName)
                dataList.add(note)
            }
            cursor.close()
            return@withContext dataList
        }


    suspend fun readDataFromTrashTable(searchText: String): ArrayList<Note> =
        withContext(Dispatchers.IO) {
            val dataList = ArrayList<Note>()
            val selection = "${DbConstants.TRASH_CONTENT} || ${DbConstants.TRASH_TITLE} like?"
            val cursor = db?.query(
                    DbConstants.TRASH_TABLE,
                null,
                selection,
                arrayOf("%$searchText%"),
                null,
                null,
                null
            )

            while (cursor?.moveToNext()!!) {
                val titleData = cursor.getString(cursor.getColumnIndex(DbConstants.TRASH_TITLE))
                val contentData = cursor.getString(cursor.getColumnIndex(DbConstants.TRASH_CONTENT))
                val dataId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
                val editTime = cursor.getString(cursor.getColumnIndex(DbConstants.TRASH_EDIT_TIME))
                val isTopInt = cursor.getInt(cursor.getColumnIndex(DbConstants.TRASH_IS_TOP))
                val isTop = isTopInt == 1
                val wallpaperName = cursor.getString(cursor.getColumnIndex(DbConstants.TRASH_WALLPAPER))
                val removalTime = cursor.getLong(cursor.getColumnIndex(DbConstants.REMOVAL_TIME))
                val note =
                    Note(titleData, contentData, dataId, editTime, isTop, wallpaperName, removalTime)
                dataList.add(note)
            }
            cursor.close()
            return@withContext dataList
        }

    fun emptyTrash() {
        db?.delete(DbConstants.TRASH_TABLE, null, null)
    }

    fun removeItemFromTrashCan(id: String) {
        val selection = BaseColumns._ID + "=$id"
        db?.delete(DbConstants.TRASH_TABLE, selection, null)
    }

    fun closeDb() {
        dbHelper.close()
    }
}
