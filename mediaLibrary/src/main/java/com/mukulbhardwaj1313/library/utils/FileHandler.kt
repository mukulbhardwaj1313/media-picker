package com.mukulbhardwaj1313.library.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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

    fun contentUriToVideoFile(context: Context,uri: Uri, name:String?): File {
        val myFile = createNewFile(context, isVideo = true, name = name)
        val inputStream = context.contentResolver.openInputStream(uri)
        val myOutputStream = FileOutputStream(myFile)
        val maxBufferSize = 1 * 1024 * 1024
        val bytesAvailable = inputStream!!.available()
        val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
        val buffer = ByteArray(bufferSize)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            myOutputStream.write(buffer, 0, read)
        }
        inputStream.close()
        myOutputStream.close()

        return myFile
    }


    fun contentUriToImageFile(context: Context,uri: Uri, name:String?): File {
        val bitmap =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        val resizedSignature: Bitmap = Bitmap.createBitmap(bitmap)
        val quality = if (bitmap.byteCount > 5145728) 50 else 80
        resizedSignature.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        val myFile = createNewFile(context, name = name)
        val outputStream = FileOutputStream(myFile)
        outputStream.write(byteArrayOutputStream.toByteArray())
        outputStream.flush()
        outputStream.close()

        return myFile
    }


}