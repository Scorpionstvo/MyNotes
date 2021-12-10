package com.example.myproject.project.data

data class AdapterItemModel(private val _note: Note, private val _isChecked: Boolean)   {
    val note = _note
    val isChecked = _isChecked


}