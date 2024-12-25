package com.example.mynotes

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(val context:Context, var dataList:ArrayList<NoteData>): RecyclerView.Adapter<RecyclerViewAdapter.noteDataViewHolder>(), AdapterView.OnItemSelectedListener {

    private lateinit var nameFont: Spinner
    private lateinit var textFont: Spinner
    private lateinit var textAlign: Spinner
    private lateinit var textColor: Spinner
    private lateinit var foreText: Spinner
    private lateinit var cfont: TextView
    private lateinit var nfont: TextView
    private lateinit var txtalign: TextView
    private lateinit var textColors: TextView
    private lateinit var bgColorViewer: ImageView
    private lateinit var fgColorViewer: ImageView
    private lateinit var editContent: EditText
    private lateinit var editName: EditText
    private lateinit var txtCounter: TextView

    inner class noteDataViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val noteName = view.findViewById<TextView>(R.id.noteNameView)
        val noteId = view.findViewById<TextView>(R.id.noteIdView)
        val noteContent = view.findViewById<TextView>(R.id.contentNoteView)
        val btnViewNote = view.findViewById<ImageButton>(R.id.viewThisNote)
        val btnDeleteNote = view.findViewById<ImageButton>(R.id.deleteThisNote)
        val btnEditNote = view.findViewById<ImageButton>(R.id.editThisNote)
        val btnCopyNote = view.findViewById<ImageButton>(R.id.copyThisNote)
        var font1 = ""
        var font2 = ""
        var align = ""
        var placeIdString = ""
        var placeId = 0
        var txtfgCol = ""
        val dateView = view.findViewById<TextView>(R.id.noteDateView)
        val noteNum = view.findViewById<TextView>(R.id.noteNumber)
        val lengthView = view.findViewById<TextView>(R.id.sizeViewer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): noteDataViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.note_view_widget, parent, false)
        return noteDataViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: noteDataViewHolder, position: Int) {

        val dbHelper = DBHelper(context)

        val list = dataList[position]

        holder.noteName.text = list.nameNote
        holder.noteId.text = list.idNote
        holder.noteContent.text = Html.fromHtml(list.contentNote)
        holder.align = list.textAlign
        holder.dateView.text = list.dateData
        holder.placeId = list.place
        holder.placeIdString = list.place.toString()
        holder.noteNum.text = holder.placeIdString
        holder.font1 = list.fontName
        holder.font2 = list.fontContent
        holder.txtfgCol = list.fgTextColor

        val sizeTxt = context.getString(R.string.size)
        val length = holder.noteContent.length().toString()
        holder.lengthView.text = "$sizeTxt $length"


        when (holder.txtfgCol) {
            "yellow" -> {
                holder.noteContent.setTextColor(Color.YELLOW)
            }
            "red" -> {
                holder.noteContent.setTextColor(Color.RED)
            }
            "magenta" -> {
                holder.noteContent.setTextColor(Color.MAGENTA)
            }
            "cyan" -> {
                holder.noteContent.setTextColor(Color.CYAN)
            }
            "black" -> {
                holder.noteContent.setTextColor(Color.BLACK)
            }
            "white" -> {
                holder.noteContent.setTextColor(Color.WHITE)
            }
            "green" -> {
                holder.noteContent.setTextColor(Color.GREEN)
            }
            "gray" -> {
                holder.noteContent.setTextColor(Color.GRAY)
            }
            "ltgray" -> {
                holder.noteContent.setTextColor(Color.LTGRAY)
            }
            "dkgray" -> {
                holder.noteContent.setTextColor(Color.DKGRAY)
            }
        }

        when (holder.align) {
            "center" -> {
                holder.noteContent.gravity = Gravity.CENTER
            }
            "left" -> {
                holder.noteContent.gravity = Gravity.LEFT
            }
            "right" -> {
                holder.noteContent.gravity = Gravity.RIGHT
            }
        }

        when (holder.font1) {
            "monospace" -> {
                holder.noteName.typeface = Typeface.MONOSPACE
            }
            "serif" -> {
                holder.noteName.typeface = Typeface.SERIF
            }
            "sans-serif" -> {
                holder.noteName.typeface = Typeface.SANS_SERIF
            }
            else -> {
                holder.noteName.typeface = Typeface.SERIF
            }
        }

        when (holder.font2) {
            "monospace" -> {
                holder.noteContent.typeface = Typeface.MONOSPACE
            }
            "serif" -> {
                holder.noteContent.typeface = Typeface.SERIF
            }
            "sans-serif" -> {
                holder.noteContent.typeface = Typeface.SANS_SERIF
            }
            else -> {
                holder.noteContent.typeface = Typeface.SERIF
            }
        }

        fun viewNote() {
            val database = dbHelper.readableDatabase
            val layoutInflater: LayoutInflater = LayoutInflater.from(context)
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            //val name = holder.noteName.text
            val dialog = inflater.inflate(R.layout.note_viewer_window, null)
            val noteWin = dialog.findViewById<TextView>(R.id.viewNoteWindow)
            builder.setView(dialog)
            //val content = holder.noteContent.text
            //noteWin.text = content
            noteWin.movementMethod = ScrollingMovementMethod()
            val searchDialog = inflater.inflate(R.layout.activity_main, null)
            val searchString = searchDialog.findViewById<SearchView>(R.id.searcher)
            searchString.clearFocus()
            searchString.setQuery("", false)
            searchString.isIconified = true
            val idData = holder.noteId.text
            val id = idData.drop(4)

            val projection = arrayOf("Id", "NoteName", "NoteContent", "ContentFont", "TextAlign", "TextFgColor")

            val selection = "Id = $id"
            val cursor = database.query("NotesContentTable", projection, selection, null, null, null, null)

            var cFont = ""
            var txtAlign = ""
            var txtColFg = ""
            var name = ""
            var content = ""

            while (cursor.moveToNext()) {
                cFont = cursor.getString(cursor.getColumnIndexOrThrow("ContentFont"))
                txtAlign = cursor.getString(cursor.getColumnIndexOrThrow("TextAlign"))
                txtColFg = cursor.getString(cursor.getColumnIndexOrThrow("TextFgColor"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("NoteName"))
                content = cursor.getString(cursor.getColumnIndexOrThrow("NoteContent"))

                when (txtAlign) {
                    "center" -> {
                        noteWin.gravity = Gravity.CENTER
                    }
                    "left" -> {
                        noteWin.gravity = Gravity.LEFT
                    }
                    "right" -> {
                        noteWin.gravity = Gravity.RIGHT
                    }
                }

                when (cFont) {
                    "monospace" -> {
                        noteWin.typeface = Typeface.MONOSPACE
                    }
                    "serif" -> {
                        noteWin.typeface = Typeface.SERIF
                    }
                    "sans-serif" -> {
                        noteWin.typeface = Typeface.SANS_SERIF
                    }
                    else -> {
                        noteWin.typeface = Typeface.SERIF
                    }
                }

                when (txtColFg) {
                    "yellow" -> {
                        noteWin.setTextColor(Color.YELLOW)
                    }
                    "red" -> {
                        noteWin.setTextColor(Color.RED)
                    }
                    "magenta" -> {
                        noteWin.setTextColor(Color.MAGENTA)
                    }
                    "cyan" -> {
                        noteWin.setTextColor(Color.CYAN)
                    }
                    "black" -> {
                        noteWin.setTextColor(Color.BLACK)
                    }
                    "white" -> {
                        noteWin.setTextColor(Color.WHITE)
                    }
                    "green" -> {
                        noteWin.setTextColor(Color.GREEN)
                    }
                    "gray" -> {
                        noteWin.setTextColor(Color.GRAY)
                    }
                    "ltgray" -> {
                        noteWin.setTextColor(Color.LTGRAY)
                    }
                    "dkgray" -> {
                        noteWin.setTextColor(Color.DKGRAY)
                    }
                }

                val view = context.getString(R.string.view_note)
                builder.setTitle("$view '$name'")
                noteWin.text = Html.fromHtml(content)
            }

            cursor.close()
            database.close()

            val close = context.getString(R.string.close)
            builder.setNegativeButton(close) { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }

        fun deleteNote() {
            val layoutInflater: LayoutInflater = LayoutInflater.from(context)
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            val name = holder.noteName.text
            val delete = context.getString(R.string.delete_note)
            builder.setTitle("$delete '$name' ?")
            val dialog = inflater.inflate(R.layout.apply_delete_note, null)
            val searchDialog = inflater.inflate(R.layout.activity_main, null)
            val searchString = searchDialog.findViewById<SearchView>(R.id.searcher)
            searchString.clearFocus()
            searchString.setQuery("", false)
            searchString.isIconified = true
            builder.setView(dialog)

            fun searchToDelete() {
                val database = dbHelper.writableDatabase
                //val id = holder.noteId.text.toString()
                //val name = holder.noteName.text.toString()
                //val content = holder.noteContent.text.toString()
                //val param = holder.checkedParam
                val idText = holder.noteId.text.toString()
                val id = idText.drop(4)
                //val deleteNoteQuery = "DELETE FROM NotesContentTable WHERE Id IN ('$id') OR NoteName IN ('$name') OR NoteContent IN ('$content') OR IsChecked IN ('$param')" !deletes all values equals id, name, param, content!
                val deleteNoteQuery = "DELETE FROM NotesContentTable WHERE Id = $id" //deletes line specified Id column from table
                database.execSQL(deleteNoteQuery)

                val pos = holder.adapterPosition
                dataList.removeAt(pos)
                notifyItemRemoved(holder.adapterPosition)

                database.close()
            }

            val yes = context.getString(R.string.yes)
            val no = context.getString(R.string.no)

            builder.setPositiveButton(yes) { dialog, which ->
                searchToDelete()
            }

            builder.setNegativeButton(no) { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }

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

        fun editNote() {
            val layoutInflater: LayoutInflater = LayoutInflater.from(context)
            val builder = AlertDialog.Builder(context)
            val edit = context.getString(R.string.edit_note)
            builder.setTitle(edit)
            val inflater = layoutInflater
            val dialog = inflater.inflate(R.layout.edit_note_dialog, null)
            val searchDialog = inflater.inflate(R.layout.activity_main, null)
            val searchString = searchDialog.findViewById<SearchView>(R.id.searcher)
            searchString.clearFocus()
            searchString.setQuery("", false)
            searchString.isIconified = true
            editName = dialog.findViewById(R.id.editName)
            editContent = dialog.findViewById(R.id.editContent)
            val idData = holder.noteId.text
            val id = idData.drop(4)

            nameFont = dialog.findViewById(R.id.nameFontSpin)
            textFont = dialog.findViewById(R.id.textFontSpin)
            textAlign = dialog.findViewById(R.id.textAlignSpin)
            textColor = dialog.findViewById(R.id.colorTextSpin)
            foreText = dialog.findViewById(R.id.colorTextSpin2)
            txtalign = dialog.findViewById(R.id.alignText)
            cfont = dialog.findViewById(R.id.cFontView)
            nfont = dialog.findViewById(R.id.nFontView)
            textColors = dialog.findViewById(R.id.textColorsView)
            //val txtCounter = dialog.findViewById<TextView>(R.id.symbolsCounter)
            bgColorViewer = dialog.findViewById(R.id.textBgColorView)
            fgColorViewer = dialog.findViewById(R.id.textFgColorView)
            txtCounter = dialog.findViewById(R.id.symbolsCounter)

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

            val spinAdapter1: ArrayAdapter<*> = ArrayAdapter<Any?>(context, android.R.layout.simple_spinner_item, fonts1)
            val spinAdapter2: ArrayAdapter<*> = ArrayAdapter<Any?>(context, android.R.layout.simple_spinner_item, fonts2)
            val alignAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(context, android.R.layout.simple_spinner_item, aligns)
            val colorAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(context, android.R.layout.simple_spinner_item, colors)
            val textColorAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(context, android.R.layout.simple_spinner_item, textcolors)
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

            val database = dbHelper.readableDatabase
            val projection = arrayOf("NoteName", "NoteContent", "NameFont", "ContentFont", "TextAlign", "TextFgColor")
            //val sortOrder = "Id, NoteName, NoteContent ASC"

            val selection = "Id = $id"
            val cursor = database.query("NotesContentTable", projection, selection, null, null, null, null)

            var nFont: String?
            var cFont: String?
            var txtAlign: String?
            var txtColFg: String?
            var content: String?
            var name: String?

            while (cursor.moveToNext()) {
                content = cursor.getString(cursor.getColumnIndexOrThrow("NoteContent"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("NoteName"))
                nFont = cursor.getString(cursor.getColumnIndexOrThrow("NameFont"))
                cFont = cursor.getString(cursor.getColumnIndexOrThrow("ContentFont"))
                txtAlign = cursor.getString(cursor.getColumnIndexOrThrow("TextAlign"))
                txtColFg = cursor.getString(cursor.getColumnIndexOrThrow("TextFgColor"))

                when (txtColFg) {
                    "yellow" -> {
                        foreText.setSelection(3)
                        editContent.setTextColor(Color.YELLOW)
                    }
                    "red" -> {
                        foreText.setSelection(9)
                        editContent.setTextColor(Color.RED)
                    }
                    "magenta" -> {
                        foreText.setSelection(1)
                        editContent.setTextColor(Color.MAGENTA)
                    }
                    "cyan" -> {
                        foreText.setSelection(4)
                        editContent.setTextColor(Color.CYAN)
                    }
                    "black" -> {
                        foreText.setSelection(0)
                        editContent.setTextColor(Color.BLACK)
                    }
                    "white" -> {
                        foreText.setSelection(11)
                        editContent.setTextColor(Color.WHITE)
                    }
                    "green" -> {
                        foreText.setSelection(5)
                        editContent.setTextColor(Color.GREEN)
                    }
                    "gray" -> {
                        foreText.setSelection(7)
                        editContent.setTextColor(Color.GRAY)
                    }
                    "ltgray" -> {
                        foreText.setSelection(8)
                        editContent.setTextColor(Color.LTGRAY)
                    }
                    "dkgray" -> {
                        foreText.setSelection(2)
                        editContent.setTextColor(Color.DKGRAY)
                    }
                    "blue" -> {
                        foreText.setSelection(6)
                        editContent.setTextColor(Color.BLUE)
                    }
                    "transparent" -> {
                        foreText.setSelection(10)
                        editContent.setTextColor(Color.TRANSPARENT)
                    }
                }

                when (txtAlign) {
                    "center" -> {
                        textAlign.setSelection(1)
                        editContent.gravity = Gravity.CENTER
                    }
                    "left" -> {
                        textAlign.setSelection(0)
                        editContent.gravity = Gravity.LEFT
                    }
                    "right" -> {
                        textAlign.setSelection(2)
                        editContent.gravity = Gravity.RIGHT
                    }
                }

                when (nFont) {
                    "serif" -> {
                        nameFont.setSelection(2)
                        editName.typeface = Typeface.SERIF
                    }
                    "monospace" -> {
                        nameFont.setSelection(1)
                        editName.typeface = Typeface.MONOSPACE
                    }
                    "sans-serif" -> {
                        nameFont.setSelection(0)
                        editName.typeface = Typeface.SANS_SERIF
                    }
                    else -> {
                        nameFont.setSelection(2)
                        editName.typeface = Typeface.SERIF
                    }
                }

                when (cFont) {
                    "serif" -> {
                        textFont.setSelection(2)
                        editContent.typeface = Typeface.SERIF
                    }
                    "monospace" -> {
                        textFont.setSelection(1)
                        editContent.typeface = Typeface.MONOSPACE
                    }
                    "sans-serif" -> {
                        textFont.setSelection(0)
                        editContent.typeface = Typeface.SANS_SERIF
                    }
                    else -> {
                        textFont.setSelection(2)
                        editContent.typeface = Typeface.SERIF
                    }
                }

                editName.setText(name, TextView.BufferType.EDITABLE)
                editContent.setText(Html.fromHtml(content), TextView.BufferType.EDITABLE)
            }

            cursor.close()
            database.close()

            editContent.setOnLongClickListener {
                val startText = editContent.selectionStart
                val endText = editContent.selectionEnd

                if (startText < 0 || endText < 0) {
                    return@setOnLongClickListener false
                }

                val spannable = SpannableStringBuilder(editContent.text)
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

                editContent.text = spannable

                true
            }

            val string = editContent.text.toString()
            val count = string.length.toString()
            val symbols = context.getString(R.string.symbols)
            txtCounter.text = "$symbols $count/4000"

            editContent.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                @SuppressLint("SetTextI18n")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null) {
                        val symCount = s.length
                        val symCountText = s.length.toString()
                        txtCounter.text = "$symbols $symCountText/4000"
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

            fun editAndUpgrade() {
                val database = dbHelper.writableDatabase
                val name = editName.text
                val content = Html.toHtml(editContent.text)
                val idData = holder.noteId.text
                val id = idData.drop(4)
                val sizeTxt = editContent.length().toString()
                val size = context.getString(R.string.size)
                holder.lengthView.text = "$size $sizeTxt"

                val textAlign = textAlign.selectedItem.toString()
                when (textAlign) {
                    "left" -> {
                        holder.noteContent.gravity = Gravity.LEFT
                    }
                    "center" -> {
                        holder.noteContent.gravity = Gravity.CENTER
                    }
                    "right" -> {
                        holder.noteContent.gravity = Gravity.RIGHT
                    }
                }

                val foreColor = foreText.selectedItem.toString()
                when (foreColor) {
                    "yellow" -> {
                        holder.noteContent.setTextColor(Color.YELLOW)
                    }
                    "red" -> {
                        holder.noteContent.setTextColor(Color.RED)
                    }
                    "cyan" -> {
                        holder.noteContent.setTextColor(Color.CYAN)
                    }
                    "green" -> {
                        holder.noteContent.setTextColor(Color.GREEN)
                    }
                    "magenta" -> {
                        holder.noteContent.setTextColor(Color.MAGENTA)
                    }
                    "white" -> {
                        holder.noteContent.setTextColor(Color.WHITE)
                    }
                    "black" -> {
                        holder.noteContent.setTextColor(Color.BLACK)
                    }
                    "gray" -> {
                        holder.noteContent.setTextColor(Color.GRAY)
                    }
                    "blue" -> {
                        holder.noteContent.setTextColor(Color.BLUE)
                    }
                    "transparent" -> {
                        holder.noteContent.setTextColor(Color.TRANSPARENT)
                    }
                    "ltgray" -> {
                        holder.noteContent.setTextColor(Color.LTGRAY)
                    }
                    "dkgray" -> {
                        holder.noteContent.setTextColor(Color.DKGRAY)
                    }
                }

                val contentFont = textFont.selectedItem.toString()
                when (contentFont) {
                    "serif" -> {
                        holder.noteContent.typeface = Typeface.SERIF
                    }
                    "monospace" -> {
                        holder.noteContent.typeface = Typeface.MONOSPACE
                    }
                    "sans-serif" -> {
                        holder.noteContent.typeface = Typeface.SANS_SERIF
                    }
                }

                val nameFont = nameFont.selectedItem.toString()
                when (nameFont) {
                    "serif" -> {
                        holder.noteName.typeface = Typeface.SERIF
                    }
                    "monospace" -> {
                        holder.noteName.typeface = Typeface.MONOSPACE
                    }
                    "sans-serif" -> {
                        holder.noteName.typeface = Typeface.SANS_SERIF
                    }
                }

                holder.noteName.text = name
                holder.noteContent.text = Html.fromHtml(content)
                println(Html.fromHtml(content))
                val updateNoteQuery = "UPDATE NotesContentTable SET NoteName = '$name', NoteContent = '$content', TextAlign = '$textAlign', TextFgColor = '$foreColor', ContentFont = '$contentFont', NameFont = '$nameFont' WHERE Id = '$id'"
                database.execSQL(updateNoteQuery)
                database.close()
            }

            val cancel = context.getString(R.string.cancel)
            val apply = context.getString(R.string.apply)

            builder.setNegativeButton(cancel) { dialog, which ->
                dialog.dismiss()
            }

            builder.setPositiveButton(apply) { dialog, which ->
                editAndUpgrade()
            }

            builder.show()
        }

        fun copyNote() {
            val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData: ClipData
            val contentToCopy = holder.noteContent.text

            clipData = ClipData.newPlainText("text", contentToCopy)
            clipboardManager.setPrimaryClip(clipData)
        }

        holder.btnViewNote.setOnClickListener {
            viewNote()
        }

        holder.btnDeleteNote.setOnClickListener {
            deleteNote()
        }

        holder.btnEditNote.setOnClickListener {
            editNote()
        }

        holder.btnCopyNote.setOnClickListener {
            copyNote()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: ArrayList<NoteData>) {
        dataList = newList
        notifyDataSetChanged()
    }

    @SuppressLint("RtlHardcoded", "SetTextI18n")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val nameFont = nameFont.selectedItem
        val contentFont = textFont.selectedItem.toString()
        val align = textAlign.selectedItem.toString()
        val textcolor = textColor.selectedItem.toString()
        val foretext = foreText.selectedItem.toString()

        val select1 = context.getString(R.string.select_content_font)
        val select2 = context.getString(R.string.select_name_font)
        val select3 = context.getString(R.string.select_text_align)
        val select4 = context.getString(R.string.select_bg_or_fg)

        cfont.text = "$select1 ($contentFont)"
        nfont.text = "$select2 ($nameFont)"
        txtalign.text = "$select3 ($align)"
        textColors.text = "$select4 ($textcolor) ($foretext)"

        when (textcolor) {
            "yellow" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "magenta" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.magenta), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "blue" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "red" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "cyan" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.cyan), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "black" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "white" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "gray" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "transparent" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.transparent), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "green" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "ltgray" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.ltgray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "dkgray" -> {
                bgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.dkgray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        when (foretext) {
            "yellow" -> {
                editContent.setTextColor(Color.YELLOW)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "magenta" -> {
                editContent.setTextColor(Color.MAGENTA)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.magenta), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "blue" -> {
                editContent.setTextColor(Color.BLUE)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "red" -> {
                editContent.setTextColor(Color.RED)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "cyan" -> {
                editContent.setTextColor(Color.CYAN)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.cyan), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "black" -> {
                editContent.setTextColor(Color.BLACK)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "white" -> {
                editContent.setTextColor(Color.WHITE)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "gray" -> {
                editContent.setTextColor(Color.GRAY)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "transparent" -> {
                editContent.setTextColor(Color.TRANSPARENT)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.transparent), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "green" -> {
                editContent.setTextColor(Color.GREEN)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "ltgray" -> {
                editContent.setTextColor(Color.LTGRAY)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.ltgray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            "dkgray" -> {
                editContent.setTextColor(Color.DKGRAY)
                fgColorViewer.setColorFilter(ContextCompat.getColor(context, R.color.dkgray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        when (nameFont) {
            "serif" -> {
                editName.typeface = Typeface.SERIF
            }
            "sans-serif" -> {
                editName.typeface = Typeface.SANS_SERIF
            }
            "monospace" -> {
                editName.typeface = Typeface.MONOSPACE
            }
        }

        when (contentFont) {
            "serif" -> {
                editContent.typeface = Typeface.SERIF
            }
            "sans-serif" -> {
                editContent.typeface = Typeface.SANS_SERIF
            }
            "monospace" -> {
                editContent.typeface = Typeface.MONOSPACE
            }
        }

        when (align) {
            "center" -> {
                editContent.gravity = Gravity.CENTER
            }
            "left" -> {
                editContent.gravity = Gravity.LEFT
            }
            "right" -> {
                editContent.gravity = Gravity.RIGHT
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(context, "Edit note", Toast.LENGTH_SHORT).show()
    }
}
