package com.example.my_project.project.application
import android.app.Application
import com.example.my_project.data.db.DbManager

class MyApplication : Application() {

    companion object {
        lateinit var dbManager: DbManager
    }

    override fun onCreate() {
        super.onCreate()
        dbManager = DbManager.newInstance(this)
    }

}