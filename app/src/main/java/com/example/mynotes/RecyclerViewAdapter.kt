package com.example.mynotes

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(val context:Context, var dataList:ArrayList<NoteData>): RecyclerView.Adapter<RecyclerViewAdapter.noteDataViewHolder>() {

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
        val dateView = view.findViewById<TextView>(R.id.noteDateView)
        val noteNum = view.findViewById<TextView>(R.id.noteNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): noteDataViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.note_view_widget, parent, false)
        return noteDataViewHolder(view)
    }

    @SuppressLint("RtlHardcoded")
    override fun onBindViewHolder(holder: noteDataViewHolder, position: Int) {

        val dbHelper = DBHelper(context)

        val list = dataList[position]

        holder.noteName.text = list.nameNote
        holder.noteId.text = list.idNote
        holder.noteContent.text = list.contentNote
        holder.align = list.textAlign.uppercase()
        holder.dateView.text = list.dateData
        holder.placeId = list.place
        holder.placeIdString = list.place.toString()
        holder.noteNum.text = holder.placeIdString
        holder.font1 = list.fontName.uppercase()
        holder.font2 = list.fontContent.uppercase()

        when (holder.align) {
            "CENTER" -> {
                holder.noteContent.gravity = Gravity.CENTER
            }
            "LEFT" -> {
                holder.noteContent.gravity = Gravity.LEFT
            }
            "RIGHT" -> {
                holder.noteContent.gravity = Gravity.RIGHT
            }
        }

        when (holder.font1) {
            "MONOSPACE" -> {
                holder.noteName.typeface = Typeface.MONOSPACE
            }
            "SERIF" -> {
                holder.noteName.typeface = Typeface.SERIF
            }
            "SANS-SERIF" -> {
                holder.noteName.typeface = Typeface.SANS_SERIF
            }
            else -> {
                holder.noteName.typeface = Typeface.SERIF
            }
        }

        when (holder.font2) {
            "MONOSPACE" -> {
                holder.noteContent.typeface = Typeface.MONOSPACE
            }
            "SERIF" -> {
                holder.noteContent.typeface = Typeface.SERIF
            }
            "SANS-SERIF" -> {
                holder.noteContent.typeface = Typeface.SANS_SERIF
            }
            else -> {
                holder.noteContent.typeface = Typeface.SERIF
            }
        }

        fun viewNote() {
            val layoutInflater: LayoutInflater = LayoutInflater.from(context)
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            val name = holder.noteName.text
            builder.setTitle("View note '$name'")
            val dialog = inflater.inflate(R.layout.note_viewer_window, null)
            val noteWin = dialog.findViewById<TextView>(R.id.viewNoteWindow)
            builder.setView(dialog)

            val content = holder.noteContent.text

            noteWin.text = content
            noteWin.movementMethod = ScrollingMovementMethod()

            builder.setNegativeButton("Close") { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }

        fun deleteNote() {
            val layoutInflater: LayoutInflater = LayoutInflater.from(context)
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            val name = holder.noteName.text
            builder.setTitle("Delete note '$name' ?")
            val dialog = inflater.inflate(R.layout.apply_delete_note, null)
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

            builder.setPositiveButton("Yes") { dialog, which ->
                searchToDelete()
            }

            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }

        fun editNote() {
            val layoutInflater: LayoutInflater = LayoutInflater.from(context)
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            val nameofNote = holder.noteName.text.toString()
            val contentofNote = holder.noteContent.text.toString()
            builder.setTitle("Edit note '$nameofNote'")
            val dialog = inflater.inflate(R.layout.edit_note_dialog, null)
            val editName = dialog.findViewById<EditText>(R.id.editName)
            val editContent = dialog.findViewById<EditText>(R.id.editContent)
            editName.setText(nameofNote, TextView.BufferType.EDITABLE)
            editContent.setText(contentofNote, TextView.BufferType.EDITABLE)

            when (holder.font1) {
                "SERIF" -> {
                    editName.typeface = Typeface.SERIF
                }
                "MONOSPACE" -> {
                    editName.typeface = Typeface.MONOSPACE
                }
                "SANS-SERIF" -> {
                    editName.typeface = Typeface.SANS_SERIF
                }
                else -> {
                    editName.typeface = Typeface.SERIF
                }
            }

            when (holder.font2) {
                "SERIF" -> {
                    editContent.typeface = Typeface.SERIF
                }
                "MONOSPACE" -> {
                    editContent.typeface = Typeface.MONOSPACE
                }
                "SANS-SERIF" -> {
                    editContent.typeface = Typeface.SANS_SERIF
                }
                else -> {
                    editContent.typeface = Typeface.SERIF
                }
            }

            builder.setView(dialog)

            fun editAndUpgrade() {
                val database = dbHelper.writableDatabase
                val name = editName.text
                val content = editContent.text
                val idData = holder.noteId.text
                val id = idData.drop(4)
                holder.noteName.text = name
                holder.noteContent.text = content
                //val updateNoteQuery = "UPDATE NotesContentTable SET NoteName = REPLACE(NoteName, '$nameofNote', '$name'), NoteContent = REPLACE(NoteContent, '$contentofNote', '$name') WHERE NoteName LIKE '%$nameofNote%' OR NoteContent LIKE '%$contentofNote%'"
                val updateNoteQuery = "UPDATE NotesContentTable SET NoteName = REPLACE(NoteName, '$nameofNote', '$name'), NoteContent = REPLACE(NoteContent, '$contentofNote', '$content') WHERE Id LIKE '%$id%'"
                database.execSQL(updateNoteQuery)
                database.close()
            }

            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

            builder.setPositiveButton("Apply") { dialog, which ->
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
}
