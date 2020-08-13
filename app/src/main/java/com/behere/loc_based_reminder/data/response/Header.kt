package com.behere.loc_based_reminder.data.response

data class Header(
    val columns: List<String>,
    val description: String,
    val resultCode: String,
    val resultMsg: String,
    val stdrYm: String
)