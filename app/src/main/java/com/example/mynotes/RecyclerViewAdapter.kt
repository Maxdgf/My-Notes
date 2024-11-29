package com.example.mynotes

import android.content.Context
import android.graphics.Color
import android.text.method.ScrollingMovementMethod
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

class RecyclerViewAdapter(val context:Context, val dataList:ArrayList<NoteData>):RecyclerView.Adapter<RecyclerViewAdapter.noteDataViewHolder>() {

    inner class noteDataViewHolder(val view:View): RecyclerView.ViewHolder(view) {
        val noteName = view.findViewById<TextView>(R.id.noteNameView)
        val noteId = view.findViewById<TextView>(R.id.noteIdView)
        val noteContent = view.findViewById<TextView>(R.id.contentNoteView)
        val btnViewNote = view.findViewById<ImageButton>(R.id.viewThisNote)
        val btnDeleteNote = view.findViewById<ImageButton>(R.id.deleteThisNote)
        val btnEditNote = view.findViewById<ImageButton>(R.id.editThisNote)
        var checkedParam = ""
        var checkValue = ""
        val finalCheck = view.findViewById<CheckBox>(R.id.completeChecker)
        val checkImage = view.findViewById<ImageView>(R.id.checkImageIcon)
        val icon = view.findViewById<ImageView>(R.id.noteIcon)
        val dateView = view.findViewById<TextView>(R.id.noteDateView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): noteDataViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.note_view_widget, parent, false)
        return noteDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: noteDataViewHolder, position: Int) {

        val dbHelper = DBHelper(context)

        val list = dataList[position]
        holder.noteName.text = list.nameNote
        holder.noteId.text = list.idNote
        holder.noteContent.text = list.contentNote
        holder.checkedParam = list.Checked
        holder.checkValue = list.checkedValue
        holder.dateView.text = list.dateData
        holder.finalCheck.setOnCheckedChangeListener(null)

        fun checkboxEvent() {
            val database = dbHelper.writableDatabase
            val checkIndekator = holder.finalCheck.isChecked
            val idData = holder.noteId.text
            val id = idData.drop(4)
            val name = holder.noteName.text.toString()
            val positiveMessage = "Congratulations! You have completed the note '$name'"
            val negativeMessage = "Oh no! don't forget to complete the note '$name'"
            if (checkIndekator == true) {
                val query = "UPDATE NotesContentTable SET CheckValue = REPLACE(CheckValue, '${holder.checkValue}', 'true') WHERE Id LIKE '%$id%'"
                database.execSQL(query)
                holder.finalCheck.text = "✓ completed"
                holder.finalCheck.setBackgroundColor(Color.parseColor("#FF4CAF50"))
                val toast = Toast.makeText(context, positiveMessage, Toast.LENGTH_LONG)
                toast.show()
                database.close()
            } else {
                val query = "UPDATE NotesContentTable SET CheckValue = REPLACE(CheckValue, '${holder.checkValue}', 'false') WHERE Id LIKE '%$id%'"
                database.execSQL(query)
                holder.finalCheck.text = "✖ not completed"
                holder.finalCheck.setBackgroundColor(Color.parseColor("#FF0000"))
                val toast = Toast.makeText(context, negativeMessage, Toast.LENGTH_LONG)
                toast.show()
                database.close()
            }
        }

        holder.finalCheck.setOnCheckedChangeListener { buttonView, isCheceked ->
            checkboxEvent()
        }

        fun checkboxChangeAction() {
            if (holder.checkValue.toBoolean()) {
                holder.finalCheck.isChecked = holder.checkValue.toBoolean()
                holder.finalCheck.text = "✓ completed"
                holder.finalCheck.setBackgroundColor(Color.parseColor("#FF4CAF50"))
            } else {
                holder.finalCheck.isChecked = holder.checkValue.toBoolean()
                holder.finalCheck.text = "✖ not completed"
                holder.finalCheck.setBackgroundColor(Color.parseColor("#FF0000"))
            }
        }

        checkboxChangeAction()

        fun checkAction() {
            if (holder.checkedParam == "Yes") {
                holder.finalCheck.visibility = View.VISIBLE
                holder.checkImage.visibility = View.VISIBLE
                holder.icon.visibility = View.INVISIBLE
            } else {
                holder.finalCheck.visibility = View.INVISIBLE
                holder.checkImage.visibility = View.INVISIBLE
                holder.icon.visibility = View.VISIBLE
            }
        }

        checkAction()


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

        holder.btnViewNote.setOnClickListener {
            viewNote()
        }

        holder.btnDeleteNote.setOnClickListener {
            deleteNote()
        }

        holder.btnEditNote.setOnClickListener {
            editNote()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
