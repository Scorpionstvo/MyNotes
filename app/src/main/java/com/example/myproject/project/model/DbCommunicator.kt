package com.example.myproject.project.model

import com.example.myproject.project.application.MyApplication
import com.example.myproject.project.data.Note
import com.example.myproject.project.repository.NotesRepository

class DbCommunicator : NotesRepository {
    private val dbManager = MyApplication.dbManager

    companion object {
        fun newInstance() = DbCommunicator()

    }

    fun openDb() {
        dbManager.openDb()
    }

    override fun getNoteList(text: String, type: String): ArrayList<Note> {
        return dbManager.readDataFromTable(text, type)
    }


    override fun updateNote(note: Note) {
        dbManager.updateItem(note)
    }

    override fun deleteNote(id: Int) {
        dbManager.removeItem(id)
    }

    override fun insertNote(note: Note) {
        dbManager.insertToTable(note)
    }

    fun closeBd() {
        dbManager.closeDb()
    }
}
