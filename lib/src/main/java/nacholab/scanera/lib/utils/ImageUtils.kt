package nacholab.scanera.lib.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

fun getBitmapWithCorrectOrientation(imagePath: String): Bitmap {
    val exif = ExifInterface(imagePath)
    val rotate = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)){
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        else -> 0f
    }

    val rotationMatrix = Matrix().apply { postRotate(rotate) }

    val originalBitmap = BitmapFactory.decodeFile(imagePath)
    return Bitmap.createBitmap(
        originalBitmap,
        0,
        0,
        originalBitmap.width,
        originalBitmap.height,
        rotationMatrix,
        true
    )

}