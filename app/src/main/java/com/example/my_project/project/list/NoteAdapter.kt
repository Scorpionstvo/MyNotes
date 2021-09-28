package com.example.my_project.project.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.currentnote.R
import com.example.my_project.project.note.Note
import com.example.my_project.project.util.DateFormatter
import com.example.my_project.project.wallpaper.Wallpaper
import kotlin.collections.ArrayList


class NoteAdapter(_showDetail: ShowDetail) : RecyclerView.Adapter<NoteAdapter.NoteHolder>() {
    private val noteList = ArrayList<Note>()
    private val showDetail = _showDetail
    private var isVisible = false
    private var checked = ArrayList<Boolean>()
    private val checkList = ArrayList<CheckBox>()

    interface ShowDetail {
        fun onClickElement(note: Note? = null, position: Int = 0)

        fun onLongClickElement()
    }

    class NoteHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val cardView: CardView = item.findViewById(R.id.cardView)
        private val tvTitle: TextView = item.findViewById(R.id.tvTitleNote)
        private val tvContent: TextView = item.findViewById(R.id.tvContentNote)
        private val tvTime: TextView = item.findViewById(R.id.tvTime)
        val checkBox: CheckBox = item.findViewById(R.id.checkRec)
        private val pin: ImageView = item.findViewById(R.id.imTop)


        fun initNote(note: Note) {
            tvTitle.text = note.title
            tvContent.text = note.content
            tvTime.text = DateFormatter.dateFormat(note.editTime)
            if (note.isTop) {
                pin.visibility = View.VISIBLE
            } else pin.visibility = View.GONE

            if (note.wallpaperName != null) {
                val wallpaper: Wallpaper = Wallpaper.valueOf(note.wallpaperName)
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

        if (isVisible) {
            checkList.add(holder.checkBox)
            holder.checkBox.visibility = View.VISIBLE
            holder.checkBox.isChecked = checked[position]
        } else {
            holder.checkBox.visibility = View.GONE
        }

        holder.checkBox.setOnClickListener {
            checked[position] = !checked[position]
            showDetail.onClickElement()
        }

        holder.initNote(noteList[position])
        for (i in 0 until itemCount) {
            checked.add(false)
        }
        holder.itemView.setOnClickListener {
            val title = noteList[holder.absoluteAdapterPosition].title
            val content = noteList[holder.absoluteAdapterPosition].content
            val time = noteList[holder.absoluteAdapterPosition].editTime
            val id = noteList[holder.absoluteAdapterPosition].id
            val elementPosition = holder.absoluteAdapterPosition
            val isTop = noteList[holder.absoluteAdapterPosition].isTop
            val wallpaperName = noteList[holder.absoluteAdapterPosition].wallpaperName
            val note = Note(title, content, id, time, isTop, wallpaperName)
            showDetail.onClickElement(note, elementPosition)
            if (isVisible) {
                checked[position] = !checked[position]
                holder.checkBox.isChecked = checked[position]
                showDetail.onClickElement()
            }
        }

        holder.itemView.setOnLongClickListener {
            showDetail.onLongClickElement()
            true
        }

    }

    fun allChecked(isChecked: Boolean) {
        for (i in checked.indices) {
            checked[i] = isChecked
        }
        notifyDataSetChanged()
    }

    fun getCheckedId(): ArrayList<Int> {
        val selectedElements = ArrayList<Int>()
        for (i in 0 until noteList.size) {
            if (checked[i]) selectedElements.add(i)
        }
        return selectedElements
    }


    fun isShowCheckBox(show: Boolean) {
        isVisible = show
        if (isVisible) {
            checked.fill(false)
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return noteList.size
    }

    fun updateAdapter(listItems: List<Note>) {
        noteList.clear()
        noteList.addAll(listItems)
        notifyDataSetChanged()
    }

}