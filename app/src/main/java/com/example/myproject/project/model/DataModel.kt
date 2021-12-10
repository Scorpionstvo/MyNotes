package com.example.myproject.project.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myproject.project.data.AdapterItemModel
import com.example.myproject.project.DbCommunicator
import com.example.myproject.project.data.Note
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

open class DataModel : ViewModel() {
    private val dbCommunicator = DbCommunicator.newInstance()


    val noteItemList: MutableLiveData<ArrayList<AdapterItemModel>> by lazy {
        MutableLiveData<ArrayList<AdapterItemModel>>()
    }

    val checkedId: MutableLiveData<HashSet<Int>> by lazy {
        MutableLiveData<HashSet<Int>>()
    }

    fun getAdapterItemList(text: String, type: String): ArrayList<AdapterItemModel> {
        viewModelScope.launch {

            val noteList = dbCommunicator.getNoteList(text, type)
            Log.d("jjjj", " note ${noteItemList.value}")
            val adapterItemList = ArrayList<AdapterItemModel>()
            for (i in 0 until noteList.size) {
                adapterItemList.add(AdapterItemModel(noteList[i], check(noteList[i].id)))
            }
            noteItemList.value = adapterItemList
        }
        Log.d("jjjj", " no ${noteItemList.value}")
        return noteItemList.value!!
    }


    fun deleteNote(id: Int) {
        dbCommunicator.deleteNotes(id)
    }

    fun updateNote(note: Note) {
        dbCommunicator.updateNotes(note)
    }


    fun insertNote(note: Note) {
        dbCommunicator.insertNote(note)
    }


    fun allChecked(check: Boolean) {
        if (checkedId.value == null) checkedId.value = HashSet()
        if (check) {
            for (i in noteItemList.value!!) {
                checkedId.value!!.add(i.note.id)
            }
        } else {
            checkedId.value!!.clear()
        }
    }

    fun getCheckedId(): HashSet<Int> {
        val a = HashSet<Int>()
        return if (checkedId.value == null) {
            a
        } else checkedId.value!!
    }

    fun updateCheckedList(id: Int) {
        if (checkedId.value == null) checkedId.value = HashSet()
        if (checkedId.value!!.contains(id)) checkedId.value?.remove(id) else checkedId.value?.add(id)
    }

    private fun check(id: Int): Boolean {
        return if (checkedId.value == null) {
            false
        } else checkedId.value!!.contains(id)
    }

    fun closeBd() {
        dbCommunicator.closeBd()
    }
}





