package com.example.myproject.project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.currentnote.R
import com.example.myproject.project.data.AdapterItemModel
import com.example.myproject.project.data.Note
import com.example.myproject.project.util.DateFormatter
import com.example.myproject.project.wallpaper.Wallpaper
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class NoteAdapter(private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<NoteAdapter.NoteHolder>() {
    private val noteList = ArrayList<AdapterItemModel>()
    private var isVisible = false
    private var checkedId = HashSet<Int>()

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

        fun initNote(adapterItem: AdapterItemModel) {
            if (adapterItem.note.title.isEmpty()) tvTitle.visibility = View.GONE
            else {
                tvTitle.visibility = View.VISIBLE
                tvTitle.text = adapterItem.note.title
            }
            if (adapterItem.note.content.isEmpty()) {
                tvContent.visibility = View.GONE
            } else {
                tvContent.visibility = View.VISIBLE
                tvContent.text = adapterItem.note.content
            }
            tvTime.text = DateFormatter.dateFormat(adapterItem.note.editTime)
            if (adapterItem.note.isTop) {
                pin.visibility = View.VISIBLE
            } else pin.visibility = View.GONE

            if (adapterItem.note.wallpaperName != null) {
                val wallpaper: Wallpaper = Wallpaper.valueOf(adapterItem.note.wallpaperName!!)
                tvTitle.setTextColor(wallpaper.textColor)
                tvContent.setTextColor(wallpaper.textColor)
                cardView.setBackgroundResource(wallpaper.primaryBackground)
                tvTime.setTextColor(wallpaper.textColor)
            }

                if (checkBox.visibility == View.GONE) {
                    checkBox.isChecked = false
                }
                else
                 checkBox.isChecked = adapterItem.isChecked

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
       holder.checkBox.visibility = if (isVisible) View.VISIBLE else View.GONE
      //  if (holder.checkBox.isChecked) checkedId.add(noteList[holder.absoluteAdapterPosition].note.id)
     //   holder.checkBox.isChecked = noteList[holder.absoluteAdapterPosition].isChecked
       // if (holder.checkBox.isChecked) checkedId.add(noteList[holder.absoluteAdapterPosition].note.id) else checkedId.remove(noteList[holder.absoluteAdapterPosition].note.id)

        holder.initNote(noteList[position])
     //   if (holder.checkBox.isChecked) checkedId.add(noteList[holder.absoluteAdapterPosition].note.id)
    //    else checkedId.remove(noteList[holder.absoluteAdapterPosition].note.id)
        holder.checkBox.setOnClickListener {
            val note = noteList[holder.absoluteAdapterPosition].note
         //
            itemClickListener.onClickItem(note)

        }

        holder.itemView.setOnClickListener {
            val note = noteList[holder.absoluteAdapterPosition].note
         //   if (!holder.checkBox.isChecked) checkedId.add(note.id) else checkedId.remove(note.id)
          itemClickListener.onClickItem(note)
            holder.checkBox.toggle()

        }

        holder.itemView.setOnLongClickListener {
            itemClickListener.onLongClickItem()
            true
        }
    }

    fun allChecked(isChecked: Boolean) {
        if (!isChecked) {
            //       checkedNotes.clear()
        } else {
            for (i in noteList) {
                //     if (checkedNotes.contains(i)) continue
                //    else checkedNotes.add(i.note)
            }
        }
        notifyDataSetChanged()
    }

    fun getCheckedId(): HashSet<Int> {
        val checked = HashSet<Int>()
        for (i in 0 until noteList.size) {
            if (noteList[i].isChecked) checked.add(noteList[i].note.id)
        }
   //     Log.d("gggg", "$checked")
        return checked
    }

    fun getCheckedItemCount(): Int {
        return checkedId.size
    }

    fun isShowCheckBox(show: Boolean) {
        isVisible = show
        notifyDataSetChanged()
    }

    fun setCheckedId(newCheckedId: HashSet<Int>) {
        checkedId = newCheckedId
    }


    fun updateAdapter(listItems: ArrayList<AdapterItemModel>) {
        val diffCallback = NotesDiffCallback(noteList, listItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        noteList.clear()
        noteList.addAll(listItems)
        diffResult.dispatchUpdatesTo(this)
     //   Log.d("ggg", "update!!!! $noteList")
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

}