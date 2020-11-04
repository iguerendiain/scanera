package nacholab.scanera

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation

const val NAVIGATION_TAG = "NAVIGATION"

fun Fragment.getNavController() = activity?.let { Navigation.findNavController(it, R.id.mainFragment) }

fun Fragment.navigate(target: Int){
    try {
        getNavController()?.navigate(target)
    }catch (e: Exception){
        Log.i(NAVIGATION_TAG, e.message.toString())
    }
}

fun Fragment.navigate(target: Int, args: Bundle){
    try{
        getNavController()?.navigate(target, args)
    }catch (e: Exception){
        Log.i(NAVIGATION_TAG, e.message.toString())
    }
}

fun Fragment.navigateUp(){
    try {
        getNavController()?.navigateUp()
    }catch (e: Exception){
        Log.i(NAVIGATION_TAG, e.message.toString())
    }
}

fun Fragment.navigatePopTo(target: Int){
    try {
        Navigation.findNavController(activity as AppCompatActivity, R.id.mainFragment)
            .popBackStack(target, false)
    }catch(e: Exception){
        Log.i(NAVIGATION_TAG, e.message.toString())
    }
}

fun Fragment.sendSMS(phone: String, message: String) = sendSMS(context, phone, message)
fun Fragment.openURLInChrome(url: String?) = openURLInChrome(context, url)

fun AppCompatActivity.getCurrentFragment(): Fragment? {
    supportFragmentManager.findFragmentById(R.id.mainFragment)?.let { navFragment ->
        navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
            return fragment
        }
    }

    return null
}

fun sendSMS(context: Context?, phone: String, message: String){
    val uri = Uri.parse("smsto:%s".format(phone))
    val intent = Intent(Intent.ACTION_SENDTO, uri)
    intent.putExtra("sms_body", message)
    context?.startActivity(intent)
}

fun openURLInChrome(context: Context?, url: String?){
    context?.let { url?.let {
        val intent = Intent(Intent.ACTION_VIEW,Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")
        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            intent.setPackage(null)
            context.startActivity(intent)
        }
    }}
}

fun openGooglePlay(context: Context?){
    context?.let {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        val openGooglePlay = Intent(Intent.ACTION_VIEW, uri)
        openGooglePlay.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )

        try {
            context.startActivity(openGooglePlay)
        } catch (e: Exception) {
            openURLInChrome(context, "http://play.google.com/store/apps/details?id=${context.packageName}")
        }
    }
}