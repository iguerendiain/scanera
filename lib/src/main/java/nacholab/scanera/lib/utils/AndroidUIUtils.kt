package nacholab.scanera.lib.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import org.jetbrains.anko.windowManager

fun Fragment.getNavbarHeight() = getNavbarHeight(activity?.windowManager, activity?.resources)
fun Fragment.getStatusbarHeight() = getStatusbarHeight(activity?.resources)

fun View.openKeyboard() = openKeyboard(context, this)
fun View.closeKeyboard() = closeKeyboard(context, windowToken)

fun openKeyboard(context: Context, view: View){
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun closeKeyboard(context: Context, windowToken: IBinder?){
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
}

fun View.hasNavBar() = hasNavBar(context.windowManager)

fun hasNavBar(windowManager: WindowManager?): Boolean {
    val size = Point()
    val realSize = Point()

    windowManager?.defaultDisplay?.getRealSize(realSize)
    windowManager?.defaultDisplay?.getSize(size)

    return size.y != realSize.y
}

fun View.getStatusbarHeight(): Int = getStatusbarHeight(context.resources)

fun getStatusbarHeight(resources: Resources?): Int {
    var result = 0

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val resourceId = resources?.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId ?: 0 > 0) {
            result = resources!!.getDimensionPixelSize(resourceId!!)
        }
    }

    return result
}

fun View.getNavbarHeight() = getNavbarHeight(
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?,
        context.resources
    )

fun getNavbarHeight(windowManager: WindowManager?, resources: Resources?): Int {
    var result = 0

    if (hasNavBar(windowManager) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val resourceId = resources?.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId?:0 > 0) {
            result = resources!!.getDimensionPixelSize(resourceId!!)
        }
    }

    return result
}

fun Fragment.setupAndroidUI(navbarBackground: View?, statusbarBackground: View?){
    setupAndroidUI(
        activity?.windowManager,
        activity?.resources,
        navbarBackground,
        statusbarBackground
    )
}

fun View.setupAndroidUI(navbarBackground: View?, statusbarBackground: View?){
    setupAndroidUI(
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?,
        context.resources,
        navbarBackground,
        statusbarBackground
    )
}

fun setupAndroidUI(windowManager: WindowManager?, resources: Resources?, navbarBackground: View?, statusbarBackground: View?){
    if (hasNavBar(windowManager)) {
        navbarBackground?.visibility = View.VISIBLE
        val nblp = navbarBackground?.layoutParams
        nblp?.height = getNavbarHeight(windowManager, resources)
        navbarBackground?.layoutParams = nblp
    }else{
        navbarBackground?.visibility = View.GONE
    }

    val sblp = statusbarBackground?.layoutParams
    sblp?.height = getStatusbarHeight(resources)
    statusbarBackground?.layoutParams = sblp
}

fun View.getScreenSize() = getScreenSize(context)

fun getScreenSize(context: Context): Point{
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    val displayMetrics = DisplayMetrics()
    wm?.defaultDisplay?.getMetrics(displayMetrics)
    val height = displayMetrics.heightPixels
    val width = displayMetrics.widthPixels
    return Point(width, height)
}