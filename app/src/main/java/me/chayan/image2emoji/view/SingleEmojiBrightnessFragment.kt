package me.chayan.image2emoji.view

import android.Manifest
import android.app.Activity
import android.graphics.*
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
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import me.chayan.image2emoji.R
import me.chayan.image2emoji.databinding.FragmentSingleEmojiBrightnessBinding
import me.chayan.image2emoji.utils.BitmapUtil
import me.chayan.image2emoji.utils.FileUtil
import me.chayan.image2emoji.utils.KeyboardUtil
import me.chayan.image2emoji.utils.executeAsyncTask
import me.chayan.image2emoji.widget.ProgressDialog


class SingleEmojiBrightnessFragment : Fragment() {

    private lateinit var binding: FragmentSingleEmojiBrightnessBinding
    private lateinit var progress: ProgressDialog
    private lateinit var photoView: PhotoViewAttacher

    private val step: Int = 32
    private var emojiSize: Int = 0
    private var imageQuality: Int = 0
    private var openedBitmap: Bitmap? = null
    private var imageBackColor: Int = View.MEASURED_STATE_MASK

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingleEmojiBrightnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoView = PhotoViewAttacher(binding.imageViewSingle2)
        photoView.setScaleLevels(1.0f, 5.0f, 10.0f)

        binding.buttonOpenSingle2.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .createIntent { intent ->
                    pickImage.launch(intent)
                }
        }

        binding.buttonConvertSingle2.setOnClickListener {
            KeyboardUtil.hideKeyboard(it)
            binding.imageViewSingle2.setImageBitmap(openedBitmap)
            startConvert()
        }

        binding.buttonSaveSingle2.setOnClickListener {
            requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        colorSetting()
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap? = BitmapUtil.getBitmap(
                data?.data, binding.buttonOpenSingle2,
                binding.buttonConvertSingle2,
                binding.buttonSaveSingle2,
                requireContext()
            )

            binding.imageViewSingle2.setImageBitmap(bitmap)
            openedBitmap = bitmap
            photoView.update()
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                FileUtil.saveNow(
                    binding.imageViewSingle2.drawable.toBitmap(),
                    requireContext(),
                    binding.buttonSaveSingle2
                )
            } else {
                Toast.makeText(context, getString(R.string.no_permissions), Toast.LENGTH_SHORT).show()
            }
        }

    private fun startConvert() {
        imageQuality = binding.qualitySeekSingle2.progress
        imageQuality = (imageQuality * 140) / 100

        emojiSize = when {
            binding.typeEmojiSizeSingle2.text.isBlank() -> 28
            else -> binding.typeEmojiSizeSingle2.text.toString().toInt()
        }

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
                binding.buttonConvertSingle2.setBackgroundResource(R.drawable.btn_green)
                binding.buttonSaveSingle2.setBackgroundResource(R.drawable.btn_yellow)
                binding.buttonSaveSingle2.isEnabled = true
                binding.scrollViewSingle2.smoothScrollTo(0, 0)
                binding.scrollViewSingle2.fullScroll(1)
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
        val hsv = FloatArray(3)
        for (x in 0 until bitmapWidth) {
            for (y in 0 until bitmapHeight) {
                val pixel = createScaledBitmap.getPixel(x, y)
                Color.RGBToHSV(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsv)
                hsv[1] = 0.0f
                pixels.add(Color.HSVToColor(Color.alpha(pixel), hsv))
            }
        }
        val createBitmap = Bitmap.createBitmap(bitmapWidth * step, bitmapHeight * step, Bitmap.Config.ARGB_8888)
        createBitmap.eraseColor(imageBackColor)

        val width = createBitmap.width
        val height = createBitmap.height
        val canvas = Canvas(createBitmap)

        progress.setMax(pixels.size)
        val contrast = binding.brightnessSeekSingle2

        val paint = Paint()
        val max = contrast.max.toInt() - contrast.progress

        var lastIndex = 0
        for (left in 0 until width step step) {
            var index = lastIndex
            for (top in 0 until height step step) {
                val pixel = pixels[index]

                var green = Color.green(pixel) - max
                if (green < 0) green = 0

                paint.colorFilter = LightingColorFilter(View.MEASURED_SIZE_MASK, Color.rgb(green, green, green))
                canvas.drawBitmap(makeSameColor(pixel), left.toFloat(), top.toFloat(), paint)
                index += 1
                progress.incrementProgressBy(1)
            }
            lastIndex = index
        }
        return createBitmap
    }

    private fun makeSameColor(pixel: Int, grayscale: Boolean = false): Bitmap {
        val createBitmap = Bitmap.createBitmap(step, step, Bitmap.Config.ARGB_8888)
        if (!grayscale) {
            val canvas = Canvas(createBitmap)
            val paint = Paint()
            paint.color = View.MEASURED_STATE_MASK
            paint.textSize = emojiSize.toFloat()
            paint.textAlign = Paint.Align.CENTER
            var emoji = binding.typeEmoji2.text.toString()
            if (emoji.isBlank()) emoji = "❤"
            canvas.drawText(
                emoji,
                (canvas.width / 2.0f),
                ((canvas.height / 2.0f) - (paint.descent() + paint.ascent()) / 2.0f), paint
            )
        } else {
            val red = Color.red(pixel)
            createBitmap.eraseColor(Color.rgb(red, red, red) and (-((red shl 24).toDouble() / 0.1)).toInt())
        }
        return createBitmap
    }

    private fun onNewUiData(bitmap: Bitmap?) {
        activity?.runOnUiThread {
            binding.imageViewSingle2.setImageBitmap(bitmap)
            photoView.update()
        }
    }

    private fun colorSetting() {

        binding.seekRedSingle2.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onStartTrackingTouch(indicatorSeekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(indicatorSeekBar: IndicatorSeekBar) {}
            override fun onSeeking(seekParams: SeekParams) {
                imageBackColor = Color.rgb(
                    binding.seekRedSingle2.progress,
                    binding.seekGreenSingle2.progress,
                    binding.seekBlueSingle2.progress
                )
                binding.linearlayoutColorSingle2.setBackgroundColor(imageBackColor)
                val rgb = Color.rgb(
                    255 - Color.red(imageBackColor),
                    255 - Color.green(imageBackColor),
                    255 - Color.blue(imageBackColor)
                )
                binding.titleBackground2.setTextColor(rgb)
                binding.commentBackground2.setTextColor(rgb)
            }
        }
        binding.seekGreenSingle2.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onStartTrackingTouch(indicatorSeekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(indicatorSeekBar: IndicatorSeekBar) {}
            override fun onSeeking(seekParams: SeekParams) {
                imageBackColor = Color.rgb(
                    binding.seekRedSingle2.progress,
                    binding.seekGreenSingle2.progress,
                    binding.seekBlueSingle2.progress
                )
                binding.linearlayoutColorSingle2.setBackgroundColor(imageBackColor)
                val rgb = Color.rgb(
                    255 - Color.red(imageBackColor),
                    255 - Color.green(imageBackColor),
                    255 - Color.blue(imageBackColor)
                )
                binding.titleBackground2.setTextColor(rgb)
                binding.commentBackground2.setTextColor(rgb)
            }
        }
        binding.seekBlueSingle2.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onStartTrackingTouch(indicatorSeekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(indicatorSeekBar: IndicatorSeekBar) {}
            override fun onSeeking(seekParams: SeekParams) {
                imageBackColor = Color.rgb(
                    binding.seekRedSingle2.progress,
                    binding.seekGreenSingle2.progress,
                    binding.seekBlueSingle2.progress
                )
                binding.linearlayoutColorSingle2.setBackgroundColor(imageBackColor)
                val rgb = Color.rgb(
                    255 - Color.red(imageBackColor),
                    255 - Color.green(imageBackColor),
                    255 - Color.blue(imageBackColor)
                )
                binding.titleBackground2.setTextColor(rgb)
                binding.commentBackground2.setTextColor(rgb)
            }
        }
    }
}