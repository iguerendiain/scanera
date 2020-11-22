package nacholab.scanera.lib.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import kotlinx.android.synthetic.main.scanera_camera.*
import nacholab.lib.R
import nacholab.scanera.lib.utils.navigate
import nacholab.scanera.lib.utils.setupAndroidUI
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File

class CameraFragment: BaseFragment() {
    override fun getLayoutId() = R.layout.scanera_camera
    override fun getStatusBarType() = STATUS_BAR_LIGHT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFullScreen(true)
        setupAndroidUI(statusbarBackground, navbarBackground)
        initCamera()

        take_photo.onClick {
            cameraView.takePicture()
        }

        toggle_camera.onClick {
            cameraView.toggleFacing()
        }

        toggle_flash.onClick {
            toggleFlash()
        }

        take_photo.onClick {
            cameraView.takePicture()
        }
    }

    private fun toggleFlash(){
        if (isFlashSupported()){
            val supportedFlashModes = cameraView.cameraOptions?.supportedFlash?.toList()
            val currentFlashMode = cameraView.flash
            val currentFlashModeIndex = supportedFlashModes?.indexOf(supportedFlashModes.find { it == currentFlashMode })

            val nextFlashModeIndex = if (currentFlashModeIndex!=null)
                if (currentFlashModeIndex+1 >= supportedFlashModes.size) 0
                else currentFlashModeIndex+1
            else 0

            cameraView.flash = supportedFlashModes?.get(nextFlashModeIndex)?:Flash.OFF

            updateUI()
        }
    }

    private fun buildTargetFile(): File?{
        return context?.filesDir?.let { File(it, "scaneraPhoto.jpg") }
    }

    private fun initCamera(){
        val photoFile = buildTargetFile()
        photoFile?.parentFile?.mkdirs()

        cameraView.addCameraListener(object: CameraListener() {
            override fun onCameraOpened(options: CameraOptions) {
                super.onCameraOpened(options)
                updateUI()
            }

            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                photoFile?.let {
                    result.toFile(it) {
                        navigate(
                            R.id.action_cameraFragment_to_edgeSetupFragment,
                            bundleOf(
                                EdgeSetupFragment.ARGS_INPUT_FILE to photoFile.absolutePath
                            )
                        )
                    }
                }
            }
        })

        cameraView.setLifecycleOwner(viewLifecycleOwner)
        cameraView.audio = Audio.OFF
        cameraView.playSounds = true
        cameraView.facing = Facing.BACK
        cameraView.flash = Flash.OFF
        updateUI()
    }

    private fun isFlashSupported(): Boolean{
        return cameraView.cameraOptions?.supportedFlash?.contains(Flash.TORCH) == true
    }

    private fun updateUI(){
        currently_selected_camera.text =
            if (cameraView.facing == Facing.BACK) "Selected camera: BACK"
            else "Selected camera: FRONT"

        flash_status.text = "Flash status: " +
                if (isFlashSupported())
                    when (cameraView.flash){
                        Flash.OFF -> "Off"
                        Flash.ON -> "On"
                        Flash.AUTO -> "Auto"
                        Flash.TORCH -> "Torch"
                    }
                else "unavailable"
    }
}