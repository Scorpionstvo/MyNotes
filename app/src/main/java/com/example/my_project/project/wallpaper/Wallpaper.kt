package com.example.my_project.project.wallpaper

import com.example.currentnote.R

enum class Wallpaper(val primaryBackground: Int, val textColor: Int, val secondaryBackground: Int, val padding: Int) {
    INITIAL(0,0, R.color.grey_700, 0),
    BLUE(R.drawable.wallpaper_blue, R.color.blue_700, R.color.blue_200,0),
    PINK(R.drawable.wallpaper_pink, R.color.pink_700, R.color.pink_200,0),
    GREEN(R.drawable.wallpaper_green, R.color.green_700, R.color.green_200,0),
    YELLOW(R.drawable.wallpaper_yellow, R.color.yellow_700, R.color.yellow_200,0),
    NIGHT_SKY(R.drawable.wallpaper_night_sky, R.color.white, R.color.black,0),
    PLANTS(R.drawable.wallpaper_plant, R.color.black, R.color.white, 200),
    SWEETS(R.drawable.wallpaper_sweets, R.color.blue_700, R.color.blue_200, 200),
    SEA(R.drawable.wallpaper_sea, R.color.black, R.color.sea_sand, 0),
    WATERCOLOR(R.drawable.wallpaper_aquarelle, R.color.black, R.color.purple_200,0)
}