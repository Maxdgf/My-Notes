package com.example.mynotes

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val dbHelper = DBHelper(this)

    private lateinit var btnOpenNoteCreationDialog: ImageButton
    private lateinit var contentArea: RecyclerView
    private lateinit var nameOfNote: EditText
    private lateinit var contentOfNote: EditText
    private lateinit var btnDeleteAllNotes: ImageButton
    lateinit var valuesList: ArrayList<NoteData>
    lateinit var searchFilteredList: ArrayList<NoteData>
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var noData: LinearLayout
    private lateinit var nameFont: Spinner
    private lateinit var textFont: Spinner
    private lateinit var textAlign: Spinner
    private lateinit var textColor: Spinner
    private lateinit var foreText: Spinner
    private lateinit var searchString: SearchView
    private lateinit var nfont: TextView
    private lateinit var cfont: TextView
    private lateinit var textColors: TextView
    private lateinit var txtalign: TextView
    private lateinit var bgColorViewer: ImageView
    private lateinit var fgColorViewer: ImageView
    lateinit var txtCounter: TextView
    lateinit var notesCount: TextView
    private lateinit var menuBtn: ImageButton

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_main)

        valuesList = ArrayList()

        searchFilteredList = ArrayList()

        btnOpenNoteCreationDialog = findViewById(R.id.btnCreateNote)
        contentArea = findViewById(R.id.notesView)
        btnDeleteAllNotes = findViewById(R.id.deleteAllNotes)
        noData = findViewById(R.id.noDataProvider)
        searchString = findViewById(R.id.searcher)
        notesCount = findViewById(R.id.notesCount)
        menuBtn = findViewById(R.id.btnMenu)

        recyclerViewAdapter = RecyclerViewAdapter(this, valuesList)
        contentArea.layoutManager = LinearLayoutManager(this)
        contentArea.adapter = recyclerViewAdapter

        val yes = getString(R.string.yes)
        val no = getString(R.string.no)

        val database = dbHelper.readableDatabase
        val projection = arrayOf("Id", "NoteName", "NoteContent", "DateNow", "Place", "NameFont", "ContentFont", "TextAlign", "TextFgColor")

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
            var txtColFg: String?

            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("NoteName"))
                content = cursor.getString(cursor.getColumnIndexOrThrow("NoteContent"))
                date = cursor.getString(cursor.getColumnIndexOrThrow("DateNow"))
                place = cursor.getInt(cursor.getColumnIndexOrThrow("Place"))
                nFont = cursor.getString(cursor.getColumnIndexOrThrow("NameFont"))
                cFont = cursor.getString(cursor.getColumnIndexOrThrow("ContentFont"))
                txtAlign = cursor.getString(cursor.getColumnIndexOrThrow("TextAlign"))
                txtColFg = cursor.getString(cursor.getColumnIndexOrThrow("TextFgColor"))

                valuesList.add(NoteData(name, "Id: $id", content, date, place, nFont, cFont, txtAlign, txtColFg))
                recyclerViewAdapter.notifyDataSetChanged()
            }

            cursor.close()
            database.close()

        } catch (e: Exception) {
            println("Oh no!!! fetching data process is crashed:(((")
            e.printStackTrace()
        }

        val welcome = getString(R.string.welcome)
        val toast = Toast.makeText(this, welcome, Toast.LENGTH_LONG)
        toast.show()

        fun dropDownMenu() {
            val menu = PopupMenu(this@MainActivity, menuBtn)
            menu.menuInflater.inflate(R.menu.menu_items, menu.menu)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_quit -> {
                        val activity = MainActivity()
                        activity.finish()
                        System.exit(0)
                        true
                    }

                    R.id.menu_about -> {
                        val builder = AlertDialog.Builder(this)
                        val inflater = layoutInflater
                        val about = getString(R.string.about)
                        builder.setTitle(about)
                        val dialog = inflater.inflate(R.layout.about, null)
                        builder.setView(dialog)

                        val close = getString(R.string.close)
                        builder.setPositiveButton(close) {dialog, which ->
                            dialog.dismiss()
                        }

                        builder.show()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            menu.show()
        }

        menuBtn.setOnClickListener {
            dropDownMenu()
        }

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
                val notes = getString(R.string.notes)
                notesCount.text = "$notes $count"
                mainHandler.postDelayed(this, 1500)
            }
        })

        fun createDeleteAllNotesDialog() {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val message = getString(R.string.delete_all_notes)
            searchString.clearFocus()
            searchString.isIconified = true
            builder.setTitle(message)
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

            builder.setPositiveButton(yes) { dialog, which ->
                deleteNotes()
            }

            builder.setNegativeButton(no) { dialog, which ->
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
                        val message = getString(R.string.found_notes)
                        notesCount.text = "$message $count ($query)"
                    }
                }
            }
        })

        fun isOverPlace(spannable: Spannable, start: Int, end: Int): Boolean {
            val spans = spannable.getSpans(start, end, Any::class.java)
            return spans.isNotEmpty()
        }

        fun removeOverPlace(spannable: Spannable, start: Int, end: Int) {
            val spans = spannable.getSpans(start, end, Any::class.java)
            for (span in spans) {
                spannable.removeSpan(span)
            }
        }

        fun createNoteDialog() {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val message = getString(R.string.create_note)
            searchString.clearFocus()
            searchString.isIconified = true
            builder.setTitle(message)
            val dialog = inflater.inflate(R.layout.edit_text_dialog, null)
            nameOfNote = dialog.findViewById(R.id.inputNameNote)
            contentOfNote = dialog.findViewById(R.id.inputContentNote)
            nameFont = dialog.findViewById(R.id.nameFontSpin)
            textFont = dialog.findViewById(R.id.textFontSpin)
            textAlign = dialog.findViewById(R.id.textAlignSpin)
            textColor = dialog.findViewById(R.id.colorTextSpin)
            foreText = dialog.findViewById(R.id.colorTextSpin2)
            txtalign = dialog.findViewById(R.id.alignText)
            cfont = dialog.findViewById(R.id.cFontView)
            nfont = dialog.findViewById(R.id.nFontView)
            textColors = dialog.findViewById(R.id.textColorsView)
            txtCounter = dialog.findViewById(R.id.symbolsCounter)
            bgColorViewer = dialog.findViewById(R.id.textBgColorView)
            fgColorViewer = dialog.findViewById(R.id.textFgColorView)

            nameFont.onItemSelectedListener = this
            textFont.onItemSelectedListener = this
            textAlign.onItemSelectedListener = this
            textColor.onItemSelectedListener = this
            foreText.onItemSelectedListener = this

            val fonts1 = arrayOf<String?>("sans-serif", "monospace", "serif")
            val fonts2 = arrayOf<String?>("sans-serif", "monospace", "serif")
            val aligns = arrayOf<String?>("left", "center", "right")
            val colors = arrayOf<String?>("yellow", "magenta", "dkgray", "black", "cyan", "green", "blue", "gray", "ltgray", "red", "transparent", "white")
            val textcolors = arrayOf<String?>("black", "magenta", "dkgray", "yellow", "cyan", "green", "blue", "gray", "ltgray", "red", "transparent", "white")

            val spinAdapter1: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, fonts1)
            val spinAdapter2: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, fonts2)
            val alignAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, aligns)
            val colorAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, colors)
            val textColorAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, textcolors)
            spinAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            alignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            textColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            nameFont.adapter = spinAdapter1
            textFont.adapter = spinAdapter2
            textAlign.adapter = alignAdapter
            textColor.adapter = colorAdapter
            foreText.adapter = textColorAdapter

            contentOfNote.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                @SuppressLint("SetTextI18n")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null) {
                        val symCount = s.length
                        val symCountText = s.length.toString()
                        val description = getString(R.string.symbols)
                        txtCounter.text = "$description $symCountText/4000"
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

            contentOfNote.setOnLongClickListener {
                val startText = contentOfNote.selectionStart
                val endText = contentOfNote.selectionEnd

                if (startText < 0 || endText < 0) {
                    return@setOnLongClickListener false
                }

                val spannable = SpannableStringBuilder(contentOfNote.text)
                val txtColor = textColor.selectedItem.toString()

                if (isOverPlace(spannable, startText, endText)) {
                    removeOverPlace(spannable, startText, endText)
                }

                when (txtColor) {
                    "yellow" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.YELLOW), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "magenta" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.MAGENTA), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "dkgray" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.DKGRAY), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "black" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.BLACK), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "cyan" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.CYAN), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "green" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.GREEN), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "blue" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.BLUE), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "gray" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.GRAY), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "ltgray" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.LTGRAY), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "red" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.RED), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "transparent" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.TRANSPARENT), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    "white" -> {
                        spannable.setSpan(BackgroundColorSpan(Color.WHITE), startText, endText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }

                //println("$startText/$endText")

                contentOfNote.text = spannable

                true
            }

            builder.setView(dialog)

            @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
            fun saveData() {
                //println("This is a crete note function")
                try {
                    val database = dbHelper.writableDatabase
                    val idData = (1000..100000).random().toString()
                    var nameData = nameOfNote.text.toString()
                    var contentData = Html.toHtml(contentOfNote.text)
                    val date = SimpleDateFormat("dd.M.yyyy HH:mm:ss")
                    val currentDate = date.format(Date())
                    val placeIndex = recyclerViewAdapter.itemCount
                    val nameFont = nameFont.selectedItem.toString()
                    val contentFont = textFont.selectedItem.toString()
                    val textAlign = textAlign.selectedItem.toString()
                    var textFgColor = foreText.selectedItem.toString()

                    //test output prints:
                    //println("Note name data: $nameData")
                    //println("Note content data: $contentData")
                    //println("Note id data: $idData)
                    //println(isCheckedData)

                    val rndNum = (100..5000).random().toString()
                    val rndNum2 = (100..8000).random().toString()
                    if (nameData.isEmpty()) {
                        val untitled = getString(R.string.untitled)
                        nameData = "$untitled$rndNum"
                    }
                    if (contentData.isEmpty()) {
                        val empty = getString(R.string.empty)
                        contentData = "$empty ($rndNum2)"
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
                        put("TextFgColor", textFgColor)
                    }
                    database.insert("NotesContentTable", null, values)
                    database.close()

                    val elCount = recyclerViewAdapter.itemCount
                    contentArea.scrollToPosition(elCount)

                    valuesList.add(NoteData(nameData, "Id: $idData", contentData, currentDate, placeIndex, nameFont, contentFont, textAlign, textFgColor))
                    recyclerViewAdapter.notifyDataSetChanged()
                    //recyclerViewAdapter.notifyItemInserted(valuesList.size - 1)
                    //println(valuesList)

                } catch (e: Exception) {
                    println("Data base was not created :(((")
                    e.printStackTrace()
                }
            }

            val apply = getString(R.string.apply)
            val cancel = getString(R.string.cancel)

            builder.setPositiveButton(apply) { dialog, which ->
                saveData()
            }

            builder.setNegativeButton(cancel) { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }

        btnOpenNoteCreationDialog.setOnClickListener {
            createNoteDialog()
        }

        btnDeleteAllNotes.setOnClickListener {
            createDeleteAllNotesDialog()
        }
    }

    @SuppressLint("SetTextI18n", "RtlHardcoded")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        val nameFont = nameFont.selectedItem.toString()
        val contentFont = textFont.selectedItem.toString()
        val align = textAlign.selectedItem.toString()
        val textcolor = textColor.selectedItem.toString()
        val foretext = foreText.selectedItem.toString()

        val select1 = getString(R.string.select_content_font)
        val select2 = getString(R.string.select_name_font)
        val select3 = getString(R.string.select_text_align)
        val select4 = getString(R.string.select_bg_or_fg)

        cfont.text = "$select1 ($contentFont)"
        nfont.text = "$select2 ($nameFont)"
        txtalign.text = "$select3 ($align)"
        textColors.text = "$select4 ($textcolor) ($foretext)"

        when (textcolor) {
            "yellow" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "magenta" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.magenta), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "blue" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "red" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "cyan" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.cyan), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "black" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "white" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "gray" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "transparent" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.transparent), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "green" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "ltgray" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.ltgray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "dkgray" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.dkgray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        when (foretext) {
            "yellow" -> {
                contentOfNote.setTextColor(Color.YELLOW)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "magenta" -> {
                contentOfNote.setTextColor(Color.MAGENTA)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.magenta), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "blue" -> {
                contentOfNote.setTextColor(Color.BLUE)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "red" -> {
                contentOfNote.setTextColor(Color.RED)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "cyan" -> {
                contentOfNote.setTextColor(Color.CYAN)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.cyan), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "black" -> {
                contentOfNote.setTextColor(Color.BLACK)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "white" -> {
                contentOfNote.setTextColor(Color.WHITE)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "gray" -> {
                contentOfNote.setTextColor(Color.GRAY)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "transparent" -> {
                contentOfNote.setTextColor(Color.TRANSPARENT)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.transparent), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "green" -> {
                contentOfNote.setTextColor(Color.GREEN)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "ltgray" -> {
                contentOfNote.setTextColor(Color.LTGRAY)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.ltgray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "dkgray" -> {
                contentOfNote.setTextColor(Color.DKGRAY)
                fgColorViewer.setColorFilter(ContextCompat.getColor(this, R.color.dkgray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        when (nameFont) {
            "serif" -> {
                nameOfNote.typeface = Typeface.SERIF
            }
            "sans-serif" -> {
                nameOfNote.typeface = Typeface.SANS_SERIF
            }
            "monospace" -> {
                nameOfNote.typeface = Typeface.MONOSPACE
            }
        }

        when (contentFont) {
            "serif" -> {
                contentOfNote.typeface = Typeface.SERIF
            }
            "sans-serif" -> {
                contentOfNote.typeface = Typeface.SANS_SERIF
            }
            "monospace" -> {
                contentOfNote.typeface = Typeface.MONOSPACE
            }
        }

        when (align) {
            "center" -> {
                contentOfNote.gravity = Gravity.CENTER
            }
            "left" -> {
                contentOfNote.gravity = Gravity.LEFT
            }
            "right" -> {
                contentOfNote.gravity = Gravity.RIGHT
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Create note", Toast.LENGTH_SHORT).show()
    }
}
