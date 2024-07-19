package com.erdemyesilcicek.foodbook.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Food(

    @ColumnInfo(name = "name")
    var name : String,

    @ColumnInfo(name = "content")
    var content : String,

    @ColumnInfo(name = "photo")
    var photo : ByteArray
){
    @PrimaryKey(autoGenerate = true)
    var id = 0
}