package com.example.my_project.project.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class DataModel : ViewModel() {
    val imageUri: MutableLiveData<String>  by lazy {
        MutableLiveData<String>()
    }

}