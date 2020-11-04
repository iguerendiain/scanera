package nacholab.scanera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.photo_edit_fragment.*
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File

class PhotoEditFragment: BaseFragment() {
    companion object{
        const val ARGS_INPUT_FILE = "input_file"
    }

    override fun getLayoutId() = R.layout.photo_edit_fragment
    override fun getStatusBarType() = STATUS_BAR_NO_CHANGE

    private val inputFile by lazy {
        arguments?.getString(EdgeSetupFragment.ARGS_INPUT_FILE)?.let {
            File(it)
        }
    }

    private val inputBitmap by lazy {
        inputFile?.let {
            if (it.exists()) return@lazy getBitmapWithCorrectOrientation(it.absolutePath)
            else return@lazy null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFullScreen(false)
        correctedImage.imageBitmap = inputBitmap

        discard.onClick {
            activity?.finish()
        }

        save.onClick {
            (activity as Scanera?)?.fileInput?.let {
                it.delete()
                inputFile?.copyTo(it)
            }
            inputFile?.delete()
            val resultIntent = Intent()
            activity?.setResult(Activity.RESULT_OK, resultIntent)
            activity?.finish()
        }
    }
}