package com.example.myproject.project

import com.example.myproject.project.application.MyApplication
import com.example.myproject.project.data.Note
import com.example.myproject.project.repository.NotesRepository

class DbCommunicator : NotesRepository {
    private val dbManager = MyApplication.dbManager

    companion object {
        fun newInstance() = DbCommunicator()
    }

    override fun getNoteList(text: String, type: String): ArrayList<Note> {
        dbManager.openDb()
           return dbManager.readDataFromTable(text, type)
        }


    override fun updateNotes(note: Note) {
        dbManager.updateItem(note)
    }

    override fun deleteNotes(id: Int) {
        dbManager.removeItem(id)
    }

    override fun insertNote(note: Note) {
        dbManager.insertToTable(note)
    }

    fun closeBd() {
        dbManager.closeDb()
    }
}