package com.example.cisco.kotlincrudlab.controller

interface ItemRowListener {
    fun modifyItemState(itemObjectId: String, isDone: Boolean)
    fun onItemDelete(itemObjectId: String)
}