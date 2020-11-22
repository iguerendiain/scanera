package nacholab.scanera.lib

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import nacholab.lib.R.layout.scanera_main_activity
import org.opencv.android.OpenCVLoader
import java.io.File

class Scanera : AppCompatActivity() {
    companion object{
        const val EXTRAS_FILE = "file_input"
    }

    val fileInput by lazy {
        intent?.extras?.get(EXTRAS_FILE) as File?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenCVLoader.initDebug()
        setContentView(scanera_main_activity)
    }

    @SuppressLint("NewApi")
    fun setFullScreen(fullscreen: Boolean) {
        when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.LOLLIPOP until Build.VERSION_CODES.R -> if (fullscreen)
                window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            else
                window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            in Build.VERSION_CODES.R..Int.MAX_VALUE -> window.insetsController?.let {
                if (fullscreen) {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    it.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                }
            }
        }
    }
}