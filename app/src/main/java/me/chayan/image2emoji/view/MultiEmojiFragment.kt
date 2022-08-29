package me.chayan.image2emoji.view

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.github.dhaval2404.imagepicker.ImagePicker
import me.chayan.image2emoji.EmoLibrary.emoLibrary
import me.chayan.image2emoji.R
import me.chayan.image2emoji.databinding.FragmentMultiEmojiBinding
import me.chayan.image2emoji.utils.BitmapUtil
import me.chayan.image2emoji.utils.FileUtil.saveNow
import me.chayan.image2emoji.utils.executeAsyncTask
import me.chayan.image2emoji.widget.ProgressDialog
import kotlin.math.abs


class MultiEmojiFragment : Fragment() {

    private lateinit var binding: FragmentMultiEmojiBinding
    private lateinit var progress: ProgressDialog
    private lateinit var photoView: PhotoViewAttacher

    private val step: Int = 32
    private var imageQuality: Int = 0
    private var openedBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiEmojiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoView = PhotoViewAttacher(binding.imageViewMulti)
        photoView.setScaleLevels(1.0f, 5.0f, 10.0f)

        binding.buttonOpenMulti.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .createIntent { intent ->
                    pickImage.launch(intent)
                }
        }

        binding.buttonConvertMulti.setOnClickListener {
            binding.imageViewMulti.setImageBitmap(openedBitmap)
            startConvert()
        }

        binding.buttonSaveMulti.setOnClickListener {
            requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap? = BitmapUtil.getBitmap(
                data?.data, binding.buttonOpenMulti,
                binding.buttonConvertMulti,
                binding.buttonSaveMulti,
                requireContext()
            )

            binding.imageViewMulti.setImageBitmap(bitmap)
            openedBitmap = bitmap
            photoView.update()
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                saveNow(binding.imageViewMulti.drawable.toBitmap(), requireContext(), binding.buttonSaveMulti)
            } else {
                Toast.makeText(context, getString(R.string.no_permissions), Toast.LENGTH_SHORT).show()
            }
        }

    private fun startConvert() {
        imageQuality = binding.qualitySeekMulti.progress
        imageQuality = (imageQuality * 140) / 100

        lifecycleScope.executeAsyncTask(
            onPreExecute = {
                progress = ProgressDialog(context)
                progress.setMessage(getString(R.string.converting))
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                progress.setCancelable(false)
                progress.show()
            }, doInBackground = {
                openedBitmap?.let { multiPixelGenerate(it) }
            }, onPostExecute = {
                progress.dismiss()
                binding.buttonConvertMulti.setBackgroundResource(R.drawable.btn_green)
                binding.buttonSaveMulti.setBackgroundResource(R.drawable.btn_yellow)
                binding.buttonSaveMulti.isEnabled = true
                onNewUiData(it)
            }
        )
    }

    private fun multiPixelGenerate(bitmap: Bitmap): Bitmap {
        var dstWidth = imageQuality
        var dstHeight = imageQuality

        val imageWidth = bitmap.width
        val imageHeight = bitmap.height
        if (imageWidth > imageHeight) {
            dstHeight = imageHeight * imageQuality / imageWidth
        } else {
            dstWidth = imageWidth * imageQuality / imageHeight
        }
        val createScaledBitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false)

        val bitmapWidth = createScaledBitmap.width
        val bitmapHeight = createScaledBitmap.height
        val pixels: MutableList<Int> = ArrayList()
        for (x in 0 until bitmapWidth) {
            for (y in 0 until bitmapHeight) {
                pixels.add(createScaledBitmap.getPixel(x, y))
            }
        }
        val createBitmap = Bitmap.createBitmap(bitmapWidth * step, bitmapHeight * step, Bitmap.Config.ARGB_8888)
        createBitmap.eraseColor(Color.WHITE)

        val width = createBitmap.width
        val height = createBitmap.height
        val canvas = Canvas(createBitmap)

        progress.setMax(pixels.size)

        var lastIndex = 0
        for (left in 0 until width step step) {
            var index = lastIndex
            for (top in 0 until height step step) {
                progress.incrementProgressBy(1)
                canvas.drawBitmap(findSameColor(pixels[index]), left.toFloat(), top.toFloat(), null)
                index += 1
            }
            lastIndex = index
        }
        return createBitmap
    }

    private fun findSameColor(pixel: Int): Bitmap {

        var resourceId = 0
        var lastSimilarColor = 5000

        for (emoji in emoLibrary) {
            val color = abs(Color.red(emoji) - Color.red(pixel)) +
                    abs(Color.green(emoji) - Color.green(pixel)) +
                    abs(Color.blue(emoji) - Color.blue(pixel))
            if (color < lastSimilarColor) {
                if (color == 0) {
                    resourceId = emoji
                    break
                }
                resourceId = emoji
                lastSimilarColor = color
            }
        }

        return Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                resources,
                resources.getIdentifier("drawable/x_0x${resourceId.toString(16)}", "drawable", context?.packageName)
            ), step, step, false
        )
    }

    private fun onNewUiData(bitmap: Bitmap?) {
        activity?.runOnUiThread {
            binding.imageViewMulti.setImageBitmap(bitmap)
            photoView.update()
        }
    }
}