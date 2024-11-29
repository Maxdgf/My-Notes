package com.example.mynotes

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val dbHelper = DBHelper(this)

    lateinit var btnOpenNoteCreationDialog: Button
    lateinit var contentArea: RecyclerView
    lateinit var nameOfNote: EditText
    lateinit var contentOfNote: EditText
    lateinit var achievmentChecker: CheckBox
    lateinit var btnDeleteAllNotes: Button
    lateinit var valuesList: ArrayList<NoteData>
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    lateinit var noData: LinearLayout
    //lateinit var btnsearchNote: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        valuesList = ArrayList()

        btnOpenNoteCreationDialog = findViewById(R.id.btnCreateNote)
        contentArea = findViewById(R.id.notesView)
        btnDeleteAllNotes = findViewById(R.id.deleteAllNotes)
        noData = findViewById(R.id.noDataProvider)
        //btnsearchNote = findViewById(R.id.searchNote)

        recyclerViewAdapter = RecyclerViewAdapter(this, valuesList)
        contentArea.layoutManager = LinearLayoutManager(this)
        contentArea.adapter = recyclerViewAdapter

        getData()

        val toast = Toast.makeText(this, "Welcome to My Notes!", Toast.LENGTH_LONG)
        toast.show()

        fun checkItemCount() {
            if (recyclerViewAdapter.itemCount == 0) {
                noData.visibility = View.VISIBLE
            } else {
                noData.visibility = View.INVISIBLE
            }
        }

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                checkItemCount()
                mainHandler.postDelayed(this, 1000)
            }
        })

        fun createDeleteAllNotesDialog() {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Do you want to delete all notes?")
            val dialog = inflater.inflate(R.layout.apply_all_delete_dialog, null)
            builder.setView(dialog)

            fun deleteNotes() {
                val database = dbHelper.writableDatabase
                val deleteQuery = "DELETE FROM NotesContentTable"
                database.execSQL(deleteQuery)
                database.close()
                valuesList.clear()
                recyclerViewAdapter.notifyDataSetChanged()
            }

            builder.setPositiveButton("Yes") { dialog, which ->
                deleteNotes()
            }

            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }

        /*fun searchNote() {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Search note")
            val dialog = inflater.inflate(R.layout.search_note_dialog, null)
            builder.setView(dialog)

            fun searchingEvent() {

            }

            builder.setPositiveButton("Search") { dialog, which ->
                searchingEvent()
            }

            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }*/

        /*btnsearchNote.setOnClickListener {
            searchNote()
        }*/

        fun createNoteDialog() {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Create note")
            val dialog = inflater.inflate(R.layout.edit_text_dialog, null)
            nameOfNote = dialog.findViewById(R.id.inputNameNote)
            contentOfNote = dialog.findViewById(R.id.inputContentNote)
            achievmentChecker = dialog.findViewById(R.id.createAchievementChecker)
            builder.setView(dialog)

            @SuppressLint("NotifyDataSetChanged")
            fun saveData() {
                //println("This is a crete note function")
                try {
                    val database = dbHelper.writableDatabase
                    val idData = (1000..100000).random().toString()
                    var nameData = nameOfNote.text.toString()
                    var contentData = contentOfNote.text.toString()
                    var isChecked = "No"
                    var checkBoxValue = "false"
                    val date = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val currentDate = date.format(Date())
                    //test output prints:
                    //println("Note name data: $nameData")
                    //println("Note content data: $contentData")
                    //println("Note id data: $idData)
                    //println(isCheckedData)


                    if (achievmentChecker.isChecked) {
                        isChecked = "Yes"
                        val toast = Toast.makeText(this, "note '$nameData' was created with a achievment checker!", Toast.LENGTH_LONG)
                        toast.show()
                    } else {
                        isChecked = "No"
                        val toast = Toast.makeText(this, "note '$nameData' was created without a achievment checker!", Toast.LENGTH_LONG)
                        toast.show()
                    }

                    val rndNum = (100..5000).random().toString()
                    val rndNum2 = (100..8000).random().toString()
                    if (nameData.isEmpty()) {
                        nameData = "Untitled$rndNum"
                    }
                    if (contentData.isEmpty()) {
                        contentData = "Empty! error code ($rndNum2)"
                    }

                    val values = ContentValues().apply {
                        put("Id", idData)
                        put("NoteName", nameData)
                        put("NoteContent", contentData)
                        put("IsChecked", isChecked)
                        put("CheckValue", checkBoxValue)
                        put("DateNow", currentDate)
                    }
                    database.insert("NotesContentTable", null, values)
                    database.close()

                    valuesList.add(NoteData(nameData, "Id: $idData", contentData, isChecked, checkBoxValue, currentDate))
                    recyclerViewAdapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    println("Data base was not created :(((")
                    e.printStackTrace()
                }
            }

            builder.setPositiveButton("Apply data") { dialog, which ->
                saveData()
            }

            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }

        btnOpenNoteCreationDialog.setOnClickListener {
            //println("This is a call of create note function")
            createNoteDialog()
        }

        btnDeleteAllNotes.setOnClickListener {
            createDeleteAllNotesDialog()
        }
    }

    fun getData() {
        val database = dbHelper.readableDatabase
        val projection = arrayOf("Id", "NoteName", "NoteContent", "IsChecked", "CheckValue", "DateNow")
        //val sortOrder = "Id, NoteName, NoteContent ASC"

        val cursor = database.query("NotesContentTable", projection, null, null, null, null, null)

        try {
            var id: Int? = null
            var name: String? = null
            var content: String? = null
            var checked: String? = null
            var checkValue: String? = null
            var date: String? = null

            var idList: MutableList<Int> = mutableListOf()
            var nameList: MutableList<String> = mutableListOf()
            var contentList: MutableList<String> = mutableListOf()
            var checkedList: MutableList<String> = mutableListOf()
            var checkList: MutableList<String> = mutableListOf()
            var dateList: MutableList<String> = mutableListOf()

            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("NoteName"))
                content = cursor.getString(cursor.getColumnIndexOrThrow("NoteContent"))
                checked = cursor.getString(cursor.getColumnIndexOrThrow("IsChecked"))
                checkValue = cursor.getString(cursor.getColumnIndexOrThrow("CheckValue"))
                date = cursor.getString(cursor.getColumnIndexOrThrow("DateNow"))
                //test output prints:
                //println("This is note id: $id")
                //println("This is note name: $name")
                //println("This is note content: $content")
                idList = mutableListOf<Int>()
                idList.add(id)
                //println(idList)
                nameList = mutableListOf<String>()
                nameList.add(name)
                //println(nameList)
                contentList = mutableListOf<String>()
                contentList.add(content)
                //println(contentList)
                checkedList = mutableListOf<String>()
                checkedList.add(checked)

                checkList = mutableListOf<String>()
                checkList.add(checkValue)

                dateList = mutableListOf<String>()
                dateList.add(date)

                val appLog = "My_Notes[fetch data]"

                for (id in idList) {
                    //println(id)
                    Log.d("$appLog{value: id}", id.toString())
                }

                for (name in nameList) {
                    //println(name)
                    Log.d("$appLog{value: name}", name)
                }

                for (content in contentList) {
                    //println(content)
                    Log.d("$appLog{value: content}", content)
                }

                for (checked in checkedList) {
                    //println(checked)
                    Log.d("$appLog{value: checked}", checked)
                }

                for (checkValue in checkList) {
                    //println(checkValue)
                    Log.d("$appLog{value: checkValue}", checkValue)
                }

                for (date in dateList) {
                    //println(date)
                    Log.d("$appLog{value: date}", date)
                }

                valuesList.add(NoteData(name, "Id: $id", content, checked, checkValue, date))
                recyclerViewAdapter.notifyDataSetChanged()
            }

            cursor.close()
            database.close()

        } catch (e: Exception) {
            println("Oh no!!! fetching data process is crashed:(((")
            e.printStackTrace()
        }
    }
}
