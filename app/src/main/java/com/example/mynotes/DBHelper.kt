package com.example.mynotes

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NoteDataBox.db"
        private const val DATABASE_VERSION = 4

        private const val TABLE_NAME = "NotesContentTable"
        private const val ID = "Id"
        private const val NOTE_NAME = "NoteName"
        private const val NOTE_CONTENT = "NoteContent"
        private const val IS_CHECKED = "IsChecked"
        private const val CHECKED_VALUE = "CheckValue"
        private const val DATE_TODAY = "DateNow"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($ID INTEGER, $NOTE_NAME TEXT, $NOTE_CONTENT TEXT, $IS_CHECKED TEXT, $CHECKED_VALUE TEXT, $DATE_TODAY TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

}
