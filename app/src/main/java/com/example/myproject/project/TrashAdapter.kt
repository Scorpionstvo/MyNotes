package com.example.myproject.project

import android.view.*
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.currentnote.R
import com.example.myproject.project.util.DateFormatter
import kotlin.collections.ArrayList

class TrashAdapter(_transferChoice: TransferChoice) :
    RecyclerView.Adapter<TrashAdapter.TrashHolder>() {
    private val trashList = ArrayList<Note>()
    private val transferChoice = _transferChoice
    private var isVisible = false
    private var checked = ArrayList<Boolean>()
    private val checkList = ArrayList<CheckBox>()

    interface TransferChoice {

        fun clickCheck()
        fun onLongClickElement()

    }

    class TrashHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val tvTitle: TextView = item.findViewById(R.id.tvTitleNote)
        private val tvContent: TextView = item.findViewById(R.id.tvContentNote)
        private val tvTime: TextView = item.findViewById(R.id.tvTime)
        val checkBox: CheckBox = item.findViewById(R.id.checkRec)
        private val pin: ImageView = item.findViewById(R.id.imTop)
        private val cardView: CardView = item.findViewById(R.id.cardView)

        fun initNote(note: Note) {
            tvTitle.text = note.title
            tvContent.text = note.content
            tvTime.text = DateFormatter.dateFormat(note.editTime)
            if (note.isTop) {
                pin.visibility = View.VISIBLE
            } else pin.visibility = View.GONE
            if (note.wallpaperName != null) {
                val wallpaper: Wallpaper = Wallpaper.valueOf(note.wallpaperName)
                if (wallpaper != Wallpaper.INITIAL) {
                    tvTitle.setTextColor(wallpaper.textColor)
                    tvContent.setTextColor(wallpaper.textColor)
                    cardView.setBackgroundResource(wallpaper.primaryBackground)
                    tvTime.setTextColor(wallpaper.textColor)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_note_item, parent, false)
        return TrashHolder(view)
    }

    override fun onBindViewHolder(holder: TrashHolder, position: Int) {
        if (isVisible) {
            checkList.add(holder.checkBox)
            holder.checkBox.visibility = View.VISIBLE
            holder.checkBox.isChecked = checked[position]
        } else {
            holder.checkBox.visibility = View.GONE
        }


        holder.checkBox.setOnClickListener {
            checked[position] = !checked[position]
            transferChoice.clickCheck()
        }

        holder.initNote(trashList[position])
        for (i in 0 until itemCount) {
            checked.add(false)
        }

        holder.itemView.setOnClickListener {
            if (isVisible) {
                checked[position] = !checked[position]
                holder.checkBox.isChecked = checked[position]
                transferChoice.clickCheck()
            }
        }

        holder.itemView.setOnLongClickListener {
            transferChoice.onLongClickElement()
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
        for (i in 0 until trashList.size) {
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
        return trashList.size
    }

    fun updateAdapter(listItems: List<Note>) {
        trashList.clear()
        trashList.addAll(listItems)
        notifyDataSetChanged()
    }


}