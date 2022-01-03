package com.example.myproject.project.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myproject.project.data.AdapterItemModel
import com.example.myproject.project.data.Note
import com.example.myproject.project.type.Type
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

open class DataModel : ViewModel() {
    private val dbCommunicator = DbCommunicator.newInstance()
    private val checkedIdList = HashSet<Int>()

    val noteItemList: MutableLiveData<ArrayList<AdapterItemModel>> by lazy {
        MutableLiveData<ArrayList<AdapterItemModel>>()
    }

    fun getAdapterItemList(text: String, type: String) {
        val noteList = dbCommunicator.getNoteList(text, type)
        val adapterItemList = ArrayList<AdapterItemModel>()
        for (i in 0 until noteList.size) {
            adapterItemList.add(AdapterItemModel(noteList[i], check(noteList[i].id)))
        }
        noteItemList.value = adapterItemList
    }

    fun openDb() {
        dbCommunicator.openDb()
    }

    fun insertNote(note: Note) {
       dbCommunicator.insertNote(note)
    }

    fun deleteNote(id: Int) {
        dbCommunicator.deleteNote(id)
    }

    fun updateNote(note: Note) {
        dbCommunicator.updateNote(note)
    }

    fun movieToTrash(id: Int) {
        noteItemList.value!!.forEach {
            if (it.note.id == id) {
                it.note.typeName = Type.IS_TRASHED.name
                it.note.removalTime = System.currentTimeMillis()
                updateNote(it.note)
            }
        }
    }

    fun movieToNormal(id: Int) {
        noteItemList.value!!.forEach {
            if (it.note.id == id) {
                it.note.typeName = Type.IS_NORMAL.name
                updateNote(it.note)
            }
        }
    }

    fun movieToHide(id: Int) {
        noteItemList.value!!.forEach {
            if (it.note.id == id) {
                it.note.typeName = Type.IS_HIDDEN.name
                updateNote(it.note)
            }
        }
    }

    fun moveInList(raise: Boolean, id: Int) {
        noteItemList.value!!.forEach {
            if (checkedIdList.contains(it.note.id)) {
                it.note.isTop = raise
                updateNote(it.note)
            }
        }
    }

    fun defineAnchor(): Boolean {
        var isAnchor = false
        for (adapterItemModel in noteItemList.value!!) {
            if (checkedIdList.contains(adapterItemModel.note.id)) {
                if (!adapterItemModel.note.isTop) isAnchor = true
            }

        }
        return isAnchor
    }

    fun allChecked(check: Boolean) {
        val itemListCopy = noteItemList.value
        if (check) {
            for (i in 0 until itemListCopy!!.size) {
                itemListCopy[i] = AdapterItemModel(itemListCopy[i].note, check)
                checkedIdList.add(itemListCopy[i].note.id)
            }

        } else {
            checkedIdList.clear()
            for (i in 0 until itemListCopy!!.size) {
                itemListCopy[i] = AdapterItemModel(itemListCopy[i].note, check)
            }
        }
        noteItemList.value = itemListCopy
    }

    fun getCheckedId(): HashSet<Int> {
        return checkedIdList
    }

    fun updateCheckedList(id: Int) {
        val itemListCopy = noteItemList.value
        if (checkedIdList.contains(id)) checkedIdList.remove(id) else checkedIdList.add(id)
        for (i in 0 until itemListCopy!!.size) {
            if (itemListCopy[i].note.id == id)
                itemListCopy[i] = AdapterItemModel(itemListCopy[i].note, !itemListCopy[i].isChecked)
        }
        noteItemList.value = itemListCopy

    }

    private fun check(id: Int): Boolean {
        return if (checkedIdList.isEmpty()) false else checkedIdList.contains(id)
    }

    fun closeBd() {
        dbCommunicator.closeBd()
    }
}
