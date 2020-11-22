package nacholab.scanera.lib.utils

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.max

fun perspective2TopToBottomOrthogonal(
    originalPicture: String,
    topLeftX: Double,
    topLeftY: Double,
    topRightX: Double,
    topRightY: Double,
    bottomRightX: Double,
    bottomRightY: Double,
    bottomLeftX: Double,
    bottomLeftY: Double
): Bitmap {
    val originalImage = Imgcodecs.imread(originalPicture)

    Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_BGR2RGB)
//
    val topWidth = calculateDistance(topLeftX, topLeftY, topRightX, topRightY)
    val bottomWidth = calculateDistance(bottomLeftX, bottomLeftY, bottomRightX, bottomRightY)
    val leftHeight = calculateDistance(topLeftX, topLeftY, bottomLeftX, bottomLeftY)
    val rightHeight = calculateDistance(topRightX, topRightY, bottomRightX, bottomRightY)

    val newWidth = max(topWidth, bottomWidth)
    val newHeight = max(leftHeight, rightHeight)

    val outputImage = Mat(newWidth.toInt(), newHeight.toInt(), CvType.CV_8UC4)

    val sourceCorners = MatOfPoint2f(
        Point(topLeftX, topLeftY),
        Point(topRightX, topRightY),
        Point(bottomLeftX, bottomLeftY),
        Point(bottomRightX, bottomRightY)
    )

    val destinationCorners = MatOfPoint2f(
        Point(0.0, 0.0),
        Point(newWidth - 1, 0.0),
        Point(0.0, newHeight -1),
        Point(newWidth - 1, newHeight -1)
    )

    val homografyMatrix = Calib3d.findHomography(
        sourceCorners,
        destinationCorners,
        Calib3d.RANSAC,
        3.0
    )

    Imgproc.warpPerspective(
        originalImage,
        outputImage,
        homografyMatrix,
        Size(newWidth, newHeight)
    )

    val outputBitmap = Bitmap.createBitmap(newWidth.toInt(), newHeight.toInt(), Bitmap.Config.ARGB_8888)

    Utils.matToBitmap(outputImage, outputBitmap)

    return outputBitmap
}