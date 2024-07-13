package com.development.nest.time.wrap.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainViewModel : ViewModel() {

    val updatedList = MutableLiveData<Boolean>()

    fun saveBitmap(bitmap: Bitmap?, context: Context?): String {
        val file = createInternalFile(context, "images", ".jpg")
        context?.let {
            bitmap?.let {
                Log.d("saveBitmap", "saveBitmap: ${file.path}")
                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.close()
                    updatedList.postValue(true)
                    Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return file.absolutePath
    }

    fun createInternalFile(
        context: Context?, directory: String, extension: String
    ): File {
        val dir = File(context?.filesDir, directory)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val tsLong = System.currentTimeMillis() / 1000
        val name = if (directory == "images") {
            "IMAGE_"
        } else {
            "VIDEO_"
        }
        val mImageName = "$name$tsLong$extension"
        return File(dir, mImageName)
    }

    private fun getInternalFileDirectory(context: Context?, directory: String): File {
        return File(context?.filesDir, directory)
    }

    fun loadFilesFromStorage(context: Context?, type: String): ArrayList<String> {
        val list = arrayListOf<String>()
        try {
            val files = getInternalFileDirectory(context, type).listFiles()
            files?.let {
                for (item in files) {
                    Log.d("loadImageFromStorage", "loadImageFromStorage: ${item.path}")
                    list.add(item.path)
                }
            }?.run {
                Log.d("loadImageFromStorage", "loadImageFromStorage: No file found")
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        list.reverse()
        return list
    }

}