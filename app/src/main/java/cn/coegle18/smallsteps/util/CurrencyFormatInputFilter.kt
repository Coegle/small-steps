package cn.coegle18.smallsteps.util

import android.app.Activity
import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.view.inputmethod.InputMethodManager
import java.text.DecimalFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

class CurrencyFormatInputFilter : InputFilter {
    private var mPattern: Pattern = Pattern.compile("(0|[1-9]+[0-9]*)?(\\.[0-9]{0,2})?")
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val result = (dest.subSequence(0, dstart)
            .toString() + source.toString()
                + dest.subSequence(dend, dest.length))
        val matcher: Matcher = mPattern.matcher(result)
        return if (!matcher.matches()) dest.subSequence(dstart, dend) else null
    }
}

class ActivityUtil {
    companion object {
        fun hideSoftKeyBoard(activity: Activity) {
            activity.currentFocus?.let { view ->
                val imm =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}

object Util {
    val balanceFormatter = DecimalFormat("0.00")
    val balanceFormatterSimple = DecimalFormat("0.##")
}