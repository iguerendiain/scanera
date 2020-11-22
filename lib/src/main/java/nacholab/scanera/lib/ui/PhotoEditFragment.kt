package nacholab.scanera.lib.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.photo_edit_fragment.*
import nacholab.lib.R
import nacholab.scanera.lib.Scanera
import nacholab.scanera.lib.utils.getBitmapWithCorrectOrientation
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
        photo_edit.inputBitmap = inputBitmap

        discard.onClick { activity?.finish() }
        save.onClick { save() }
        reset.onClick { reset() }
        rccw.onClick { rotateCCW() }
        rcw.onClick { rotateCW() }

        brup.onClick { brightness(20f) }
        brdn.onClick { brightness(-20f) }
        ctup.onClick { contrast(.2f) }
        ctdn.onClick { contrast(-.2f) }
        stup.onClick { saturation(.1f) }
        stdn.onClick { saturation(-.1f) }
    }

    private fun saturation(change: Float){
        photo_edit.currentSaturation+=change
        updateImageColors()
    }

    private fun brightness(change: Float){
        photo_edit.currentBrightness+=change
        updateImageColors()
    }

    private fun contrast(change: Float){
        photo_edit.currentContrast+=change
        updateImageColors()
    }

    private fun updateImageColors(){
        photo_edit.updateColorFilter()
        photo_edit.invalidate()
    }

    private fun rotateCW(){
        photo_edit.rotate(90f)
        photo_edit.invalidate()
    }

    private fun rotateCCW(){
        photo_edit.rotate(-90f)
        photo_edit.invalidate()
    }

    private fun reset(){
        photo_edit.reset()
    }

    private fun save(){
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