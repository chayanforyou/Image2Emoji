package me.chayan.image2emoji.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Button
import me.chayan.image2emoji.R
import java.io.FileNotFoundException
import kotlin.math.ceil
import kotlin.math.max

object BitmapUtil {
    fun getBitmap(
        uri: Uri?,
        button: Button,
        button2: Button,
        button3: Button,
        context: Context
    ): Bitmap? {
        var bitmap: Bitmap? = null
        if (uri != null) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)
                val size = max(options.outHeight, options.outWidth).toDouble()
                val sampleSize = ceil(if (size > 3000.0) size / 3000.0 else 0.0).toInt()
                val openInputStream = context.contentResolver.openInputStream(uri)
                options.inJustDecodeBounds = false
                options.inSampleSize = sampleSize
                val decodeStream = BitmapFactory.decodeStream(openInputStream, null, options)
                bitmap = decodeStream
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                button.setBackgroundResource(R.drawable.btn_green)
                button2.setBackgroundResource(R.drawable.btn_yellow)
                button3.setBackgroundResource(R.drawable.btn_yellow)
                button3.isEnabled = false
                button2.isEnabled = true
                return null
            }
        }
        button.setBackgroundResource(R.drawable.btn_green)
        button2.setBackgroundResource(R.drawable.btn_yellow)
        button3.setBackgroundResource(R.drawable.btn_yellow)
        button3.isEnabled = false
        button2.isEnabled = true
        return bitmap
    }
}