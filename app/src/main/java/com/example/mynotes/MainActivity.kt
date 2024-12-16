package com.example.mynotes

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.NotActiveException
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val dbHelper = DBHelper(this)

    lateinit var btnOpenNoteCreationDialog: ImageButton
    lateinit var contentArea: RecyclerView
    lateinit var nameOfNote: EditText
    lateinit var contentOfNote: EditText
    lateinit var achievmentChecker: CheckBox
    lateinit var btnDeleteAllNotes: ImageButton
    lateinit var valuesList: ArrayList<NoteData>
    lateinit var searchFilteredList: ArrayList<NoteData>
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    lateinit var upgradedList: ArrayList<NotActiveException>
    lateinit var noData: LinearLayout
    lateinit var nameFont: Spinner
    lateinit var textFont: Spinner
    lateinit var textAlign: Spinner
    lateinit var searchString: SearchView
    lateinit var nfont: TextView
    lateinit var cfont: TextView
    lateinit var txtalign: TextView
    lateinit var txtCounter: TextView
    lateinit var notesCount: TextView

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_main)

        valuesList = ArrayList()

        searchFilteredList = ArrayList()

        upgradedList = ArrayList()

        btnOpenNoteCreationDialog = findViewById(R.id.btnCreateNote)
        contentArea = findViewById(R.id.notesView)
        btnDeleteAllNotes = findViewById(R.id.deleteAllNotes)
        noData = findViewById(R.id.noDataProvider)
        searchString = findViewById(R.id.searcher)
        notesCount = findViewById(R.id.notesCount)

        recyclerViewAdapter = RecyclerViewAdapter(this, valuesList)
        contentArea.layoutManager = LinearLayoutManager(this)
        contentArea.adapter = recyclerViewAdapter
        contentArea.setItemViewCacheSize(100000)

        //valuesList.clear()
        val database = dbHelper.readableDatabase
        val projection = arrayOf("Id", "NoteName", "NoteContent", "DateNow", "Place", "NameFont", "ContentFont", "TextAlign")
        //val sortOrder = "Id, NoteName, NoteContent ASC"

        val cursor = database.query("NotesContentTable", projection, null, null, null, null, null)

        try {
            var id: Int?
            var name: String?
            var content: String?
            var date: String?
            var place: Int?
            var nFont: String?
            var cFont: String?
            var txtAlign: String?

            var idList: MutableList<Int>
            var nameList: MutableList<String>
            var contentList: MutableList<String>
            var dateList: MutableList<String>
            var placeList: MutableList<Int>
            var nFontList: MutableList<String>
            var cFontList: MutableList<String>
            var alignList: MutableList<String>

            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("NoteName"))
                content = cursor.getString(cursor.getColumnIndexOrThrow("NoteContent"))
                date = cursor.getString(cursor.getColumnIndexOrThrow("DateNow"))
                place = cursor.getInt(cursor.getColumnIndexOrThrow("Place"))
                nFont = cursor.getString(cursor.getColumnIndexOrThrow("NameFont"))
                cFont = cursor.getString(cursor.getColumnIndexOrThrow("ContentFont"))
                txtAlign = cursor.getString(cursor.getColumnIndexOrThrow("TextAlign"))
                //test output prints:
                //println("This is note id: $id")
                //println("This is note name: $name")
                //println("This is note content: $content")
                idList = mutableListOf()
                idList.add(id)
                //println(idList)
                nameList = mutableListOf()
                nameList.add(name)
                //println(nameList)
                contentList = mutableListOf()
                contentList.add(content)
                //println(contentList)

                dateList = mutableListOf()
                dateList.add(date)

                placeList = mutableListOf()
                placeList.add(place)

                nFontList = mutableListOf()
                nFontList.add(nFont)

                cFontList = mutableListOf()
                cFontList.add(cFont)

                alignList = mutableListOf()
                alignList.add(txtAlign)

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

                for (date in dateList) {
                    //println(date)
                    Log.d("$appLog{value: date}", date)
                }

                for (place in placeList) {
                    //println(place)
                    Log.d("$appLog{value: place}", place.toString())
                }

                for (nFont in nFontList) {
                    //println(nFont)
                    Log.d("$appLog{value: nameFont}", nFont)
                }

                for (cFont in cFontList) {
                    //println(cFont)
                    Log.d("$appLog{value: contentFont}", cFont)
                }

                for (txtAlign in alignList) {
                    //println(txtAlign)
                    Log.d("$appLog{value: textAlign}", txtAlign)
                }

                valuesList.add(NoteData(name, "Id: $id", content, date, place, nFont, cFont, txtAlign))
                //searchFilteredList.clear()
                recyclerViewAdapter.notifyDataSetChanged()
            }

            cursor.close()
            database.close()

        } catch (e: Exception) {
            println("Oh no!!! fetching data process is crashed:(((")
            e.printStackTrace()
        }

        val toast = Toast.makeText(this, "Welcome to My Notes!", Toast.LENGTH_LONG)
        toast.show()

        fun checkItemCount() {
            if (valuesList.isEmpty()) {
                noData.visibility = View.VISIBLE
            } else {
                noData.visibility = View.INVISIBLE
            }
        }

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object: Runnable {
            override fun run() {
                checkItemCount()
                mainHandler.postDelayed(this, 1000)
            }
        })

        mainHandler.post(object: Runnable {
            override fun run() {
                val count = recyclerViewAdapter.itemCount.toString()
                notesCount.text = "all notes: $count"
                mainHandler.postDelayed(this, 1500)
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
                searchFilteredList.clear()
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

        searchString.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterThis(query)
                return true
                //return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterThis(newText)
                return true
            }

            @SuppressLint("SetTextI18n")
            private fun filterThis(query: String) {
                searchFilteredList.clear()
                valuesList.forEach {
                    if (it.nameNote.contains(query, true)) {
                        searchFilteredList.add(it)
                        recyclerViewAdapter.updateData(searchFilteredList)
                        val count = recyclerViewAdapter.itemCount.toString()
                        notesCount.text = "found notes: $count ($query)"
                    }
                }
            }
        })

        fun createNoteDialog() {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Create note")
            val dialog = inflater.inflate(R.layout.edit_text_dialog, null)
            nameOfNote = dialog.findViewById(R.id.inputNameNote)
            contentOfNote = dialog.findViewById(R.id.inputContentNote)
            nameFont = dialog.findViewById(R.id.nameFontSpin)
            textFont = dialog.findViewById(R.id.textFontSpin)
            textAlign = dialog.findViewById(R.id.textAlignSpin)
            txtalign = dialog.findViewById(R.id.alignText)
            cfont = dialog.findViewById(R.id.cFontView)
            nfont = dialog.findViewById(R.id.nFontView)
            txtCounter = dialog.findViewById(R.id.symbolsCounter)

            nameFont.onItemSelectedListener = this
            textFont.onItemSelectedListener = this
            textAlign.onItemSelectedListener = this

            val fonts1 = arrayOf<String?>("sans-serif", "monospace", "serif")
            val fonts2 = arrayOf<String?>("sans-serif", "monospace", "serif")
            val aligns = arrayOf<String?>("left", "center", "right")

            val spinAdapter1: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, fonts1)
            val spinAdapter2: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, fonts2)
            val alignAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, aligns)
            spinAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            alignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            nameFont.adapter = spinAdapter1
            textFont.adapter = spinAdapter2
            textAlign.adapter = alignAdapter

            contentOfNote.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                @SuppressLint("SetTextI18n")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null) {
                        val symCount = s.length
                        val symCountText = s.length.toString()
                        txtCounter.text = "symbols: $symCountText/4000"
                        //println(symCount)

                        if (symCount == 4000) {
                            txtCounter.setTextColor(Color.parseColor("#FFFF0000"))
                        } else if (symCount >= 2000){
                            txtCounter.setTextColor(Color.parseColor("#FFD400"))
                        } else {
                            txtCounter.setTextColor(Color.parseColor("#4CAF50"))
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })

            builder.setView(dialog)

            @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
            fun saveData() {
                //println("This is a crete note function")
                try {
                    val database = dbHelper.writableDatabase
                    val idData = (1000..100000).random().toString()
                    var nameData = nameOfNote.text.toString()
                    var contentData = contentOfNote.text.toString()
                    val date = SimpleDateFormat("dd.M.yyyy HH:mm:ss")
                    val currentDate = date.format(Date())
                    val placeIndex = recyclerViewAdapter.itemCount
                    val elCount = recyclerViewAdapter.itemCount
                    val nameFont = nameFont.selectedItem.toString()
                    val contentFont = textFont.selectedItem.toString()
                    val textAlign = textAlign.selectedItem.toString()

                    //test output prints:
                    //println("Note name data: $nameData")
                    //println("Note content data: $contentData")
                    //println("Note id data: $idData)
                    //println(isCheckedData)

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
                        put("DateNow", currentDate)
                        put("Place", placeIndex)
                        put("NameFont", nameFont)
                        put("ContentFont", contentFont)
                        put("TextAlign", textAlign)
                    }
                    database.insert("NotesContentTable", null, values)
                    database.close()

                    contentArea.scrollToPosition(elCount)

                    valuesList.add(NoteData(nameData, "Id: $idData", contentData, currentDate, placeIndex, nameFont, contentFont, textAlign))
                    recyclerViewAdapter.notifyDataSetChanged()
                    //println(valuesList)

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

    @SuppressLint("SetTextI18n", "RtlHardcoded")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //val Namedata = nameFont.getItemAtPosition(position).toString()
        //val Contentdata = textFont.getItemAtPosition(position).toString()
        //Toast.makeText(this, " Name font is: $Namedata", Toast.LENGTH_SHORT).show()
        //Toast.makeText(this, "Content font is: $Contentdata", Toast.LENGTH_SHORT).show()

        val nameFont = nameFont.selectedItem.toString()
        val contentFont = textFont.selectedItem.toString()
        val align = textAlign.selectedItem.toString()

        cfont.text = "select a content font: ($contentFont)"
        nfont.text = "select a name font: ($nameFont)"
        txtalign.text = "select a text alignment: ($align)"

        val font1 = nameFont.uppercase()
        val font2 = contentFont.uppercase()
        val txtal = align.uppercase()

        when (font1) {
            "SERIF" -> {
                nameOfNote.typeface = Typeface.SERIF
            }
            "SANS-SERIF" -> {
                nameOfNote.typeface = Typeface.SANS_SERIF
            }
            "MONOSPACE" -> {
                nameOfNote.typeface = Typeface.MONOSPACE
            }
        }

        when (font2) {
            "SERIF" -> {
                contentOfNote.typeface = Typeface.SERIF
            }
            "SANS-SERIF" -> {
                contentOfNote.typeface = Typeface.SANS_SERIF
            }
            "MONOSPACE" -> {
                contentOfNote.typeface = Typeface.MONOSPACE
            }
        }

        when (txtal) {
            "CENTER" -> {
                contentOfNote.gravity = Gravity.CENTER
            }
            "LEFT" -> {
                contentOfNote.gravity = Gravity.LEFT
            }
            "RIGHT" -> {
                contentOfNote.gravity = Gravity.RIGHT
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
