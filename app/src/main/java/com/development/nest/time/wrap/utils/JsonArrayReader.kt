package com.development.nest.time.wrap.utils

import android.content.res.Resources
import org.json.JSONArray
import java.io.InputStream

object JsonArrayReader {
    fun readJsonFromResources(resources: Resources, resourceId: Int): JSONArray {
        val inputStream: InputStream = resources.openRawResource(resourceId)
        val jsonBuffer = ByteArray(inputStream.available())
        inputStream.read(jsonBuffer)
        inputStream.close()
        val jsonString=String(jsonBuffer)
        val jsonArray = JSONArray(jsonString)
        return jsonArray
    }
}