package nacholab.scanera.lib.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import kotlinx.android.synthetic.main.edge_setup_fragment.*
import nacholab.lib.*
import nacholab.scanera.lib.utils.getBitmapWithCorrectOrientation
import nacholab.scanera.lib.utils.navigate
import nacholab.scanera.lib.utils.perspective2TopToBottomOrthogonal
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.io.FileOutputStream

class EdgeSetupFragment: BaseFragment(){
    companion object{
        const val ARGS_INPUT_FILE = "input_file"
    }

    override fun getLayoutId() = R.layout.edge_setup_fragment
    override fun getStatusBarType() = STATUS_BAR_LIGHT

    private val inputFile by lazy {
        arguments?.getString(ARGS_INPUT_FILE)?.let {
            File(it)
        }
    }

    private val inputBitmap by lazy {
        inputFile?.let {
            if (it.exists()) return@lazy getBitmapWithCorrectOrientation(it.absolutePath)
            else return@lazy null
        }
    }

    private var correctedBitmap: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFullScreen(false)
        mainImage.imageBitmap = inputBitmap

        correctPerspective.onClick {
            inputBitmap?.let { bitmap ->
                runPerspectiveCorrection(bitmap)
            }
        }

        ready.onClick {
            val correctedFile = File(
                context?.filesDir,
                "scaneraCorrected.jpg"
            )

            val output = FileOutputStream(correctedFile)
            correctedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, output)
            output.flush()
            output.close()

            inputFile?.delete()

            navigate(
                R.id.action_edgeSetupFragment_to_photoEditFragment,
                bundleOf(
                    PhotoEditFragment.ARGS_INPUT_FILE to correctedFile.absolutePath
                )
            )
        }

        reset.onClick {
            edgeSelection.visibility = View.VISIBLE
            mainImage.imageBitmap = inputBitmap
        }
    }

    private fun runPerspectiveCorrection(bitmap: Bitmap){
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val edgeSelectionWith = edgeSelection.width
        val edgeSelectionHeight = edgeSelection.height
        val widthScale = originalWidth / edgeSelectionWith.toFloat()
        val heightScale = originalHeight / edgeSelectionHeight.toFloat()

        inputFile?.absolutePath?.let {
            correctedBitmap = perspective2TopToBottomOrthogonal(
                it,
                edgeSelection.handles[0][0].toDouble() * widthScale,
                edgeSelection.handles[0][1].toDouble() * heightScale,
                edgeSelection.handles[1][0].toDouble() * widthScale,
                edgeSelection.handles[1][1].toDouble() * heightScale,
                edgeSelection.handles[2][0].toDouble() * widthScale,
                edgeSelection.handles[2][1].toDouble() * heightScale,
                edgeSelection.handles[3][0].toDouble() * widthScale,
                edgeSelection.handles[3][1].toDouble() * heightScale
            )

            mainImage.imageBitmap = correctedBitmap
            edgeSelection.visibility = View.GONE
        }
    }

}