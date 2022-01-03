package com.example.myproject.project.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.myproject.project.data.AdapterItemModel

class NotesDiffCallback(
    private val oldList: ArrayList<AdapterItemModel>,
    private val newList: ArrayList<AdapterItemModel>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldNote = oldList[oldItemPosition]
        val newNote = newList[newItemPosition]
        return oldNote.note.id == newNote.note.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldNote = oldList[oldItemPosition]
        val newNote = newList[newItemPosition]
        return oldNote == newNote
    }

}
