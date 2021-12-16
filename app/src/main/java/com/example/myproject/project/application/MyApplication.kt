package com.example.myproject.project.application

import android.app.Application
import com.example.myproject.data.db.DbHelper
import com.example.myproject.data.db.DbManager

class MyApplication : Application() {

    companion object {
        lateinit var dbManager: DbManager
    }

    override fun onCreate() {
        dbManager = DbManager.newInstance(DbHelper(this))
        super.onCreate()
    }

}
