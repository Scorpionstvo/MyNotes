package com.example.myproject.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbCreator(context: Context) :
    SQLiteOpenHelper(context, DbConstants.DATABASE_NAME, null, DbConstants.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(DbConstants.CREATE_NOTES_TABLE)
        db?.execSQL(DbConstants.CREATE_TRASH_TABLE)
        db?.execSQL(DbConstants.CREATE_HIDDEN_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DbConstants.DELETE_NOTES_TABLE)
        db?.execSQL(DbConstants.DELETE_TRASH_BASKET_TABLE)
        db?.execSQL(DbConstants.DELETE_HIDDEN_TABLE)
        onCreate(db)
    }

}