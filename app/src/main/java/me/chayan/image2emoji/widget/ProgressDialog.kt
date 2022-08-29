package me.chayan.image2emoji.widget


import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import me.chayan.image2emoji.R
import java.text.NumberFormat

/**
 * A dialog showing a progress indicator and an optional text message or view.
 * Only a text message or a view can be used at the same time.
 *
 * <p>The dialog can be made cancelable on back key press.</p>
 *
 * <p>The progress range is 0 to {@link #getMax() max}.</p>
 */
class ProgressDialog(private var context: Context?) {

    companion object {
        const val STYLE_HORIZONTAL: Int = 1
        const val STYLE_SPINNER: Int = 0
    }

    private lateinit var progressDialog: AlertDialog
    private var mProgressStyle: Int = STYLE_SPINNER

    private var mProgress: ProgressBar? = null
    private var mMessageView: TextView? = null

    private var mProgressNumber: TextView? = null
    private var mProgressNumberFormat: String? = null
    private var mProgressPercent: TextView? = null
    private var mProgressPercentFormat: NumberFormat? = null

    private var mMax: Int = 0
    private var mProgressVal: Int = 0
    private var mSecondaryProgressVal: Int = 0
    private var mIncrementBy: Int = 0
    private var mIncrementSecondaryBy: Int = 0
    private var mProgressDrawable: Drawable? = null
    private var mIndeterminateDrawable: Drawable? = null
    private var mMessage: String = ""
    private var mIndeterminate: Boolean = false
    private var cancelable: Boolean = true

    private var mViewUpdateHandler: Handler? = null

    init {

        initFormats()

        if (mMax > 0) setMax(mMax)
        if (mProgressVal > 0) setProgress(mProgressVal)
        if (mSecondaryProgressVal > 0) setSecondaryProgress(mSecondaryProgressVal)
        if (mIncrementBy > 0) incrementProgressBy(mIncrementBy)
        if (mIncrementSecondaryBy > 0) incrementSecondaryProgressBy(mIncrementSecondaryBy)
        if (mProgressDrawable != null) setProgressDrawable(mProgressDrawable!!)
        if (mIndeterminateDrawable != null) setIndeterminateDrawable(mIndeterminateDrawable!!)
        setMessage(mMessage)

        setIndeterminate(mIndeterminate)

        /* Initialize horizontal layout as the default layout */
        spinnerLayout()

        onProgressChanged()

        /* Hide progress dialog initially */
        dismiss()

    }

    private fun initFormats() {
        mProgressNumberFormat = "%1d/%2d"
        mProgressPercentFormat = NumberFormat.getPercentInstance()
        mProgressPercentFormat?.maximumFractionDigits = 0
    }

    private fun spinnerLayout() {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.progress_dialog_spinner, null as ViewGroup?, false)

        mProgress = view.findViewById<View>(R.id.progress) as ProgressBar
        mMessageView = view.findViewById<View>(R.id.message) as TextView

        setMax(mMax)
        setProgress(mProgressVal)
        setIndeterminate(mIndeterminate)
        setMessage(mMessage)

        setView(view)
    }

    private fun horizontalLayout() {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.progress_dialog_horizontal, null as ViewGroup?, false)

        /* Use a separate handler to update the text views as they
         * must be updated on the same thread that created them.
         */
        mViewUpdateHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                /* Update the number and percent */
                val progress = mProgress?.progress
                val max = mProgress?.max
                if (mProgressNumberFormat != null) {
                    val format = mProgressNumberFormat
                    mProgressNumber?.text = format?.let { String.format(it, progress, max) }
                } else {
                    mProgressNumber?.text = ""
                }
                if (mProgressPercentFormat != null) {
                    val percent = (progress?.toDouble() ?: 0.0) / (max?.toDouble() ?: 100.0)
                    val tmp = SpannableString(mProgressPercentFormat?.format(percent))
                    tmp.setSpan(
                        StyleSpan(android.graphics.Typeface.BOLD),
                        0, tmp.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    mProgressPercent?.text = tmp
                } else {
                    mProgressPercent?.text = ""
                }
            }
        }

        mProgress = view.findViewById<View>(R.id.progress) as ProgressBar
        mProgressNumber = view.findViewById<View>(R.id.progress_number) as TextView
        mProgressPercent = view.findViewById<View>(R.id.progress_percent) as TextView
        mMessageView = view.findViewById<View>(R.id.message) as TextView

        setMax(mMax)
        setProgress(mProgressVal)
        setIndeterminate(mIndeterminate)
        setMessage(mMessage)

        setView(view)
    }

    private fun setView(view: View) {
        val builder = AlertDialog.Builder(context!!)
        builder.setView(view)
        progressDialog = builder.create()
        progressDialog.setCancelable(cancelable)
        if (progressDialog.window != null) {
            progressDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
    }

    private fun onProgressChanged() {
        if (mProgressStyle == STYLE_HORIZONTAL) {
            if (mViewUpdateHandler != null && !mViewUpdateHandler!!.hasMessages(0)) {
                mViewUpdateHandler!!.sendEmptyMessage(0)
            }
        }
    }

    /**
     * Sets the current progress.
     *
     * @param value the current progress, a value between 0 and [.getMax]
     *
     * @see ProgressBar.setProgress
     */
    fun setProgress(value: Int): ProgressDialog {
        mProgress?.progress = value
        onProgressChanged()
        mProgressVal = value
        return this
    }

    /**
     * Sets the secondary progress.
     *
     * @param secondaryProgress the current secondary progress, a value between 0 and
     * [.getMax]
     *
     * @see ProgressBar.setSecondaryProgress
     */
    fun setSecondaryProgress(secondaryProgress: Int): ProgressDialog {
        if (mProgress != null) {
            mProgress?.secondaryProgress = secondaryProgress
            onProgressChanged()
        } else {
            mSecondaryProgressVal = secondaryProgress
        }
        return this
    }

    /**
     * Gets the current progress.
     *
     * @return the current progress, a value between 0 and [.getMax]
     */
    fun getProgress(): Int {
        return if (mProgress != null) {
            mProgress!!.progress
        } else mProgressVal
    }

    /**
     * Gets the current secondary progress.
     *
     * @return the current secondary progress, a value between 0 and [.getMax]
     */
    fun getSecondaryProgress(): Int {
        return if (mProgress != null) {
            mProgress!!.secondaryProgress
        } else mSecondaryProgressVal
    }

    /**
     * Gets the maximum allowed progress value. The default value is 100.
     *
     * @return the maximum value
     */
    fun getMax(): Int {
        return if (mProgress != null) {
            mProgress!!.max
        } else mMax
    }

    /**
     * Sets the maximum allowed progress value.
     */
    fun setMax(max: Int): ProgressDialog {
        if (mProgress != null) {
            mProgress!!.max = max
            onProgressChanged()
        }
        mMax = max
        return this
    }

    /**
     * Increments the current progress value.
     *
     * @param diff the amount by which the current progress will be incremented,
     * up to [.getMax]
     */
    fun incrementProgressBy(diff: Int) {
        if (mProgress != null) {
            mProgress!!.incrementProgressBy(diff)
            onProgressChanged()
        } else {
            mIncrementBy += diff
        }
    }

    /**
     * Increments the current secondary progress value.
     *
     * @param diff the amount by which the current secondary progress will be incremented,
     * up to [.getMax]
     */
    fun incrementSecondaryProgressBy(diff: Int) {
        if (mProgress != null) {
            mProgress!!.incrementSecondaryProgressBy(diff)
            onProgressChanged()
        } else {
            mIncrementSecondaryBy += diff
        }
    }

    /**
     * Sets the drawable to be used to display the progress value.
     *
     * @param d the drawable to be used
     *
     * @see ProgressBar.setProgressDrawable
     */
    fun setProgressDrawable(d: Drawable): ProgressDialog {
        if (mProgress != null) {
            mProgress!!.progressDrawable = d
        } else {
            mProgressDrawable = d
        }
        return this
    }

    /**
     * Sets the drawable to be used to display the indeterminate progress value.
     *
     * @param d the drawable to be used
     *
     * @see ProgressBar.setProgressDrawable
     * @see .setIndeterminate
     */
    fun setIndeterminateDrawable(d: Drawable): ProgressDialog {
        if (mProgress != null) {
            mProgress!!.indeterminateDrawable = d
        } else {
            mIndeterminateDrawable = d
        }
        return this
    }

    /**
     * Change the indeterminate mode for this ProgressDialog. In indeterminate
     * mode, the progress is ignored and the dialog shows an infinite
     * animation instead.
     *
     *
     * **Note:** A ProgressDialog with style [.STYLE_SPINNER]
     * is always indeterminate and will ignore this setting.
     *
     * @param indeterminate true to enable indeterminate mode, false otherwise
     *
     * @see .setProgressStyle
     */
    fun setIndeterminate(indeterminate: Boolean): ProgressDialog {
        if (mProgress != null) {
            mProgress!!.isIndeterminate = indeterminate
        }
        mIndeterminate = indeterminate

        /* Hide progress display TextViews */
        if (indeterminate) {
            mProgressNumberFormat = null
            mProgressPercentFormat = null
        } else
            initFormats()

        return this
    }

    /**
     * Whether this ProgressDialog is in indeterminate mode.
     *
     * @return true if the dialog is in indeterminate mode, false otherwise
     */
    fun isIndeterminate(): Boolean {
        return if (mProgress != null) {
            mProgress!!.isIndeterminate
        } else mIndeterminate
    }

    /**
     * Sets the style of this ProgressDialog, either [ProgressStyle.HorizontalStyle] or
     * [ProgressStyle.SpinnerStyle] or [ProgressStyle.CustomStyle].
     * The default is [ProgressStyle.SpinnerStyle].
     *
     *
     * **Note:** A ProgressDialog with style [ProgressStyle.SpinnerStyle]
     * is always indeterminate and will ignore the [ indeterminate][.setIndeterminate] setting.
     *
     * @param style the style of this ProgressDialog
     */
    fun setProgressStyle(style: Int): ProgressDialog {
        mProgressStyle = style
        when (style) {
            STYLE_HORIZONTAL -> horizontalLayout()
            STYLE_SPINNER -> spinnerLayout()
        }
        return this
    }

    /**
     * Change the format of the small text showing current and maximum units
     * of progress.  The default is "%1d/%2d".
     * Should not be called during the number is progressing.
     * @param format A string passed to [String.format()][String.format];
     * use "%1d" for the current number and "%2d" for the maximum.  If null,
     * nothing will be shown.
     */
    fun setProgressNumberFormat(format: String): ProgressDialog {
        mProgressNumberFormat = format
        onProgressChanged()
        return this
    }

    /**
     * Change the format of the small text showing the percentage of progress.
     * The default is
     * [NumberFormat.getPercentageInstance().][NumberFormat.getPercentInstance]
     * Should not be called during the number is progressing.
     * @param format An instance of a [NumberFormat] to generate the
     * percentage text.  If null, nothing will be shown.
     */
    fun setProgressPercentFormat(format: NumberFormat): ProgressDialog {
        mProgressPercentFormat = format
        onProgressChanged()
        return this
    }

    /**
     * Displays an optional message
     * Functions same as the setMessage() in deprecated ProgressDialog class
     * @param message A string object to display on the progress dialog
     **/
    /* Set message on the progress bar. */
    fun setMessage(message: String): ProgressDialog {
        mMessageView?.text = message
        mMessage = message
        return this
    }

    /**
     * Create and show progress dialog
     */
    /* Display progress dialog */
    fun show(): ProgressDialog {
        progressDialog.show()
        return this
    }

    /**
     * Dismiss progress dialog
     **/
    /* Hide progress dialog */
    fun dismiss() {
        progressDialog.dismiss()
        setProgress(0)
    }

    /**
     * Sets whether the dialog is cancelable or not.  Default is true.
     * @param cancelable A boolean which determines if the dialog can be dismissed by the user
     **/
    /* Toggles value of cancelable */
    fun setCancelable(cancelable: Boolean): ProgressDialog {
        progressDialog.setCancelable(cancelable)
        this.cancelable = cancelable
        return this
    }

    /**
     * Called when back button is pressed.
     * Should be called in the overridden onBackPressed() of the activity
     * @param superOnBackPressed = {super.onBackPressed()} A block of code to be executed.
     **/
    fun onBackPressed(superOnBackPressed: () -> Unit) {
        if (progressDialog.isShowing) {
            if (cancelable)
                dismiss()
        } else
            superOnBackPressed.invoke()
    }

    /**
     * Says whether the dialog is cancelable or not.  Default is true.
     * @return value of cancelable
     */
    fun isCancelable(): Boolean = cancelable

    /**
     * Set message TextView's text color manually.
     * User can also customize [messageTextView][getMessageTextView] directly.
     * @param color ResourceId of text color
     */
    /* Sets text color */
    fun setTextColor(color: Int): ProgressDialog {
        mMessageView?.setTextColor(ContextCompat.getColor(context!!, color))
        return this
    }

    /**
     * Set message TextView's size manually.
     * User can also customize [messageTextView][getMessageTextView] directly.
     * @param sizeInSp text size in sp
     */
    /* Sets text size */
    fun setTextSize(sizeInSp: Float): ProgressDialog {
        mMessageView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp)
        return this
    }

    /**
     * Return message display TextView, so that the user can customize it as per his wish
     * @return Return message display TextView
     */
    fun getMessageTextView(): TextView? = mMessageView

}