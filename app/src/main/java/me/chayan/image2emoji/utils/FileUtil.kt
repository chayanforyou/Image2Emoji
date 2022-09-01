package me.chayan.image2emoji.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.chayan.image2emoji.R
import me.chayan.image2emoji.widget.ProgressDialog
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object FileUtil {

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private lateinit var saveFormat: String
    private var saveQuality: Int = 0

    fun saveNow(bitmap: Bitmap, context: Context, button: Button) {

        val sharedPreferences = context.getSharedPreferences("i2e", Context.MODE_PRIVATE)
        this.saveFormat = sharedPreferences.getString("save_format", "jpg") ?: "jpg"
        this.saveQuality = sharedPreferences.getInt("save_quality", 100)

        var path = Environment.DIRECTORY_PICTURES + "/Image2Emoji"
        val filename = "Image2Emoji${System.currentTimeMillis()}.$saveFormat"
        val progress: ProgressDialog by lazy { ProgressDialog(context) }

        uiScope.executeAsyncTask(
            onPreExecute = {
                progress.setMessage(context.getString(R.string.saving))
                progress.setCancelable(false)
                progress.show()
            }, doInBackground = {
                var fos: OutputStream? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver?.also { resolver ->
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
                            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, path)
                        }

                        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        fos = imageUri?.let { resolver.openOutputStream(it) }
                    }
                } else {
                    path = Environment.getExternalStorageDirectory().absolutePath + "/Image2Emoji"
                    val imagesDir = File(path)
                    if (!imagesDir.exists()) imagesDir.mkdirs()
                    val image = File(imagesDir, filename)
                    fos = FileOutputStream(image)
                    MediaScannerConnection.scanFile(context, arrayOf(image.path), arrayOf("image/*"), null)
                }

                fos?.use {
                    if (saveFormat == "jpg") {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, saveQuality, it)
                    }
                    if (saveFormat == "png") {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
            }, onPostExecute = {
                progress.dismiss()
                button.setBackgroundResource(R.drawable.btn_green)
                Toast.makeText(context, "${context.getString(R.string.save_to)} $path", Toast.LENGTH_LONG).show()
            }
        )
    }
}