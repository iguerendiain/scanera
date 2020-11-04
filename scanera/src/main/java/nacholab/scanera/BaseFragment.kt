package nacholab.scanera

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    companion object{
        const val STATUS_BAR_LIGHT = 1
        const val STATUS_BAR_DARK = 2
        const val STATUS_BAR_NO_CHANGE = -1
    }

    abstract fun getLayoutId(): Int
    abstract fun getStatusBarType(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            setStatusBarBrightness(getStatusBarType())
    }

    protected fun setStatusBarBrightness(brightness: Int){
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R ->
                when (brightness) {
                    STATUS_BAR_DARK ->
                        activity?.window?.decorView?.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    STATUS_BAR_LIGHT ->
                        activity?.window?.decorView?.systemUiVisibility = 0
                }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                when (brightness){
                    STATUS_BAR_DARK ->
                        activity?.window?.insetsController?.setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS)
                    STATUS_BAR_LIGHT ->
                        activity?.window?.insetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS)
                }
            }
        }
    }

    protected fun setFullScreen(
        fullScreen: Boolean,
        navbarBackground: View? = null,
        statusbarBackground: View? = null,
        statusbarBrightness: Int = STATUS_BAR_NO_CHANGE
    ){
        setFullScreen(fullScreen, arrayOf(navbarBackground), arrayOf(statusbarBackground), statusbarBrightness)
    }

    private fun setFullScreen(
        fullScreen: Boolean,
        navbarBackgrounds: Array<View?>? = null,
        statusbarBackgrounds: Array<View?>? = null,
        statusbarBrightness: Int = STATUS_BAR_NO_CHANGE
    ){
        setStatusBarBrightness(statusbarBrightness)
        (activity as Scanera?)?.setFullScreen(fullScreen)
        if (fullScreen){
            navbarBackgrounds?.forEach { it?.visibility = View.VISIBLE }
            statusbarBackgrounds?.forEach { it?.visibility = View.VISIBLE }
        }else{
            navbarBackgrounds?.forEach { it?.visibility = View.GONE }
            statusbarBackgrounds?.forEach { it?.visibility = View.GONE}
        }
    }
}