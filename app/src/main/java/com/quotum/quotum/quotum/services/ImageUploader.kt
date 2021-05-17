package com.quotum.quotum.quotum.services

import android.content.Context
import android.os.AsyncTask
import com.quotum.quotum.quotum.localdatabase.LocalDB.getUserToken
import com.quotum.quotum.quotum.network.QuotumClient.BASE_URL
import org.json.JSONObject
import java.io.File

open class ImageUploader(private val context: Context) :
    AsyncTask<String?, Void?, ImageUploader.Result>() {

    override fun doInBackground(vararg paths: String?): Result {
        val filePath = paths[0]
        return executeOnCurrent(filePath)
    }

    fun executeOnCurrent(filePath: String?): Result {
        var status = Result(DOWNLOAD_ERROR, filePath)
        try {
            //upload3(new File("/sdcard/mypassport.jpg"));
            val file = File(filePath)
            if (file.exists()) status = upload3(file)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return status
    }

    override fun onPostExecute(integer: Result) {
        super.onPostExecute(integer)
        onReceiveResult(integer)
    }

    private fun onReceiveResult(status: Result) {}
    private fun upload3(file: File): Result {
        try {
            val multipartUtility = MultipartUtility(
                BASE_URL + "/api/users/uploadMedia?access_token="
                        + getUserToken(context), "utf-8"
            )
            multipartUtility.addFilePart("photo", file)
            val response = multipartUtility.finish()
            return if (response.code == 200) {
                val responseBody = response.responseBody
                responseBody?.let { parseResponse(file, it) }
                    ?: Result(DOWNLOAD_ERROR, file.absolutePath)
            } else {
                Result(DOWNLOAD_ERROR, file.absolutePath)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return Result(DOWNLOAD_ERROR, file.absolutePath)
    }

    private fun parseResponse(file: File, responseBody: String): Result {
        try {
            val responseObj = JSONObject(responseBody)
            return if (responseObj.has("result")) {
                val result = responseObj.optString("result", "")
                Result(DOWNLOAD_SUCCESS, result)
            } else {
                Result(DOWNLOAD_ERROR, file.absolutePath)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return Result(DOWNLOAD_ERROR, file.absolutePath)
    }

    class Result(var status: Int, var result: String?)
    companion object {
        const val DOWNLOAD_SUCCESS = 2
        const val DOWNLOAD_ERROR = 3
    }
}