package com.behere.loc_based_reminder.data.response

data class Header(
    val description: String,
    val columns: List<String>,
    val stdrYm: String,
    val resultCode: String,
    val resultMsg: String
)