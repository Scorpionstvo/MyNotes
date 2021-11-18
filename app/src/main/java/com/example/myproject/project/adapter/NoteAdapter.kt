package com.example.myproject.project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.currentnote.R
import com.example.myproject.project.NotesDiffCallback
import com.example.myproject.project.note.Note
import com.example.myproject.project.util.DateFormatter
import com.example.myproject.project.wallpaper.Wallpaper
import java.util.*
import kotlin.collections.ArrayList

class NoteAdapter(private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<NoteAdapter.NoteHolder>() {
    private val noteList = ArrayList<Note>()
    private var isVisible = false
    private var checked = ArrayList<Boolean>()
    private var checkedNotes = LinkedList<Note>()

    interface ItemClickListener {
        fun onClickItem(note: Note? = null)

        fun onLongClickItem()
    }

    class NoteHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val cardView: CardView = item.findViewById(R.id.cardView)
        private val tvTitle: TextView = item.findViewById(R.id.tvTitleNote)
        private val tvContent: TextView = item.findViewById(R.id.tvContentNote)
        private val tvTime: TextView = item.findViewById(R.id.tvTime)
        val checkBox: CheckBox = item.findViewById(R.id.checkRec)
        private val pin: ImageView = item.findViewById(R.id.imTop)

        fun initNote(note: Note) {
            if (note.title.isEmpty()) tvTitle.visibility = View.GONE
            else {
                tvTitle.visibility = View.VISIBLE
                tvTitle.text = note.title
            }
            if (note.content.isEmpty()) {
                tvContent.visibility = View.GONE
            } else {
                tvContent.visibility = View.VISIBLE
                tvContent.text = note.content
            }
            tvTime.text = DateFormatter.dateFormat(note.editTime)
            if (note.isTop) {
                pin.visibility = View.VISIBLE
            } else pin.visibility = View.GONE

            if (note.wallpaperName != null) {
                val wallpaper: Wallpaper = Wallpaper.valueOf(note.wallpaperName!!)
                tvTitle.setTextColor(wallpaper.textColor)
                tvContent.setTextColor(wallpaper.textColor)
                cardView.setBackgroundResource(wallpaper.primaryBackground)
                tvTime.setTextColor(wallpaper.textColor)
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_note_item, parent, false)
        return NoteHolder(view)
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        bind(holder, position)
    }

    private fun bind(holder: NoteHolder, position: Int) {
        if (isVisible) {
            holder.checkBox.visibility = View.VISIBLE
            holder.checkBox.isChecked = checked[position]
        } else {
            holder.checkBox.visibility = View.GONE
        }

        holder.initNote(noteList[position])
        for (i in 0 until itemCount) {
            checked.add(false)
        }

        holder.checkBox.setOnClickListener {
            val note = noteList[holder.absoluteAdapterPosition]
            checked[position] = !checked[position]
            if (checked[position]) checkedNotes.add(note) else checkedNotes.remove(note)
            itemClickListener.onClickItem()
        }

        holder.itemView.setOnClickListener {
            val note = noteList[holder.absoluteAdapterPosition]
            if (isVisible) {
                checked[position] = !checked[position]
                holder.checkBox.isChecked = checked[position]
                if (checked[position]) checkedNotes.add(note) else checkedNotes.remove(note)
                itemClickListener.onClickItem()
            } else {
                itemClickListener.onClickItem(note)
            }
        }

        holder.itemView.setOnLongClickListener {
            itemClickListener.onLongClickItem()
            true
        }
    }

    fun allChecked(isChecked: Boolean) {
        for (i in checked.indices) {
            checked[i] = isChecked
        }

        if (!isChecked) {
            checkedNotes.clear()
        } else {
            for (i in noteList) {
                if (checkedNotes.contains(i)) continue
                else checkedNotes.add(i)
            }
        }
        notifyDataSetChanged()
    }

    fun getCheckedNotes(): LinkedList<Note> {
        return checkedNotes
    }

    fun getCheckedCount(): Int {
        return checkedNotes.size
    }

    fun isShowCheckBox(show: Boolean) {
        isVisible = show
        if (isVisible) {
            checked.fill(false)
            checkedNotes.clear()
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    fun updateAdapter(listItems: List<Note>) {
        val diffCallback = NotesDiffCallback(noteList, listItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        noteList.clear()
        noteList.addAll(listItems)
        diffResult.dispatchUpdatesTo(this)
    }

}