package com.behere.loc_based_reminder.data.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Item(
    val adongCd: String = "",
    val adongNm: String = "",
    val bizesId: String = "",
    val bizesNm: String = "",
    val bldMngNo: String = "",
    val bldMnno: String = "",
    val bldNm: String = "",
    val bldSlno: String = "",
    val brchNm: String = "",
    val ctprvnCd: String = "",
    val ctprvnNm: String = "",
    val dongNo: String = "",
    val flrNo: String = "",
    val hoNo: String = "",
    val indsLclsCd: String = "",
    val indsLclsNm: String = "",
    val indsMclsCd: String = "",
    val indsMclsNm: String = "",
    val indsSclsCd: String = "",
    val indsSclsNm: String = "",
    val ksicCd: String = "",
    val ksicNm: String = "",
    val lat: String = "",
    val ldongCd: String = "",
    val ldongNm: String = "",
    val lnoAdr: String = "",
    val lnoCd: String = "",
    val lnoMnno: String = "",
    val lnoSlno: String = "",
    val lon: String = "",
    val newZipcd: String = "",
    val oldZipcd: String = "",
    val plotSctCd: String = "",
    val plotSctNm: String = "",
    val rdnm: String = "",
    val rdnmAdr: String = "",
    val rdnmCd: String = "",
    val signguCd: String = "",
    val signguNm: String = ""
) : Parcelable