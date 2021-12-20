package com.example.myproject.project.data

data class AdapterItemModel(val note: Note, val isChecked: Boolean) : Comparable<AdapterItemModel> {

    override fun compareTo(other: AdapterItemModel): Int {
        return this.note.compareTo(other.note)
    }

}
