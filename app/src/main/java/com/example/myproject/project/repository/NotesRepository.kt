package com.example.myproject.project.repository

import com.example.myproject.project.data.Note

interface NotesRepository {

    fun getNoteList(text: String, type: String): ArrayList<Note>

    fun insertNote(note: Note)

    fun updateNote(note: Note)

    fun deleteNote(id: Int)
    
}
