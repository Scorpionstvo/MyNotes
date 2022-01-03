package com.example.myproject.project

import com.example.myproject.project.data.AdapterItemModel

class NoteItemModelMap(private val id: Int, private val adapterItemModel: AdapterItemModel) : HashMap<Int, AdapterItemModel>() {
    fun getModel() : AdapterItemModel {
       return adapterItemModel
    }
}