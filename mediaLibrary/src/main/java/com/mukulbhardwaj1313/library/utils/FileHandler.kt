package com.mukulbhardwaj1313.library.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileHandler {

    fun getAttachmentDir(context: Context): File {
        return context.getDir("attachment", AppCompatActivity.MODE_PRIVATE)
    }
    fun createNewFile(context: Context, name:String?=null, isVideo: Boolean = false): File {


        val nameFinal = name ?: SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val ext = if (isVideo) ".mp4" else ".jpg"

        return File(getAttachmentDir(context), nameFinal+ext)
//            return File.createTempFile(name, ext)
    }

    fun getAllSavedFiles(context: Context):List<String>{
        val fileList = File(getAttachmentDir(context).path).listFiles()
        val list = ArrayList<String>()
        if (fileList!=null && fileList.isNotEmpty()){
            fileList.iterator().forEach {
                list.add(it.path)
            }
        }
        return list
    }

    fun deleteFile(path: String){
        val file = File(path)
        if (file.exists()) {
            if (file.delete()) {
                println("file Deleted :" + file.path)
            } else {
                println("file not Deleted :" + file.path)
            }
        }
    }
}