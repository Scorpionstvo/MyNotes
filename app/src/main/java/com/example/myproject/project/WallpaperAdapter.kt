package com.example.myproject.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.currentnote.R
import kotlin.collections.ArrayList

class WallpaperAdapter(_tryOnWallpaper: TryOnWallpaper, _set: ArrayList<Wallpaper>) :
    RecyclerView.Adapter<WallpaperAdapter.WallpaperHolder>() {
    private val list = _set
    private val tryOnWallpaper = _tryOnWallpaper
    private val checked = ArrayList<Int>()

    interface TryOnWallpaper {
        fun onClickElement(wallpaper: Wallpaper)
    }

    class WallpaperHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val image = item.findViewById<CardView>(R.id.cvWallpaper)

        fun setData(w: Wallpaper) {
            image.setBackgroundResource(w.primaryBackground)
            //   primaryCardView.setBackgroundResource(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.wallpaper_item, parent, false)
        return WallpaperHolder(view)
    }

    override fun onBindViewHolder(holder: WallpaperHolder, position: Int) {
        val primaryCardView = holder.itemView.findViewById<CardView>(R.id.cardViewWall)
        holder.setData(list[position])
        if (!checked.contains(position)) primaryCardView.setBackgroundResource(0)
        else primaryCardView.setBackgroundResource(R.drawable.card_view_frame)
        //  if (checked[position])  primaryCardView.setBackgroundResource(R.drawable.card_view_frame)
        //    else  primaryCardView.setBackgroundResource(0)
        //    for (i in checked.indices) {
        //         checked[i] = false
        //   }


        holder.itemView.setOnClickListener {

            tryOnWallpaper.onClickElement(list[position])

            primaryCardView.setBackgroundResource(R.drawable.card_view_frame)
            if (checked.isEmpty()) checked.add(position)
            else {
                val a = checked.get(0)
                checked.clear()
                checked.add(position)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


}