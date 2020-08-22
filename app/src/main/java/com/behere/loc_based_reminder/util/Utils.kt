package com.behere.loc_based_reminder.util

import android.content.Context
import java.io.*

fun writeFile(fileName: String, context: Context, content: String) {
    val file = File(context.filesDir, fileName)
    val fileWriter = FileWriter(file)
    val bufferedWriter = BufferedWriter(fileWriter)
    bufferedWriter.write(content)
    bufferedWriter.close()
}

fun readFile(file: File) : String {
    val fileReader = FileReader(file)
    val bufferedReader = BufferedReader(fileReader)
    val stringBuilder = StringBuilder()
    var line = bufferedReader.readLine()
    while (line != null) {
        stringBuilder.append(line).append("\n")
        line = bufferedReader.readLine()
    }
    return stringBuilder.toString()
}