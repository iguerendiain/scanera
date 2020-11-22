package nacholab.scanera.lib.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropImageView: View {

    companion object{
        private const val HANDLE_HITBOX = 50f
        private const val HANDLE_TOPLEFT = 0
        private const val HANDLE_TOP = 1
        private const val HANDLE_TOPRIGHT = 2
        private const val HANDLE_RIGHT = 3
        private const val HANDLE_BOTTOMRIGHT = 4
        private const val HANDLE_BOTTOM = 5
        private const val HANDLE_BOTTOMLEFT = 6
        private const val HANDLE_LEFT = 7
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    )

    private val croppedPaint by lazy {
        Paint().apply{
            color = Color.argb(128, 0, 0, 0)
            style = Paint.Style.FILL
        }
    }

    private val pointerCoords by lazy { MotionEvent.PointerCoords() }

    private var selectedHandle: Int? = null

    private var topCrop = 0
    private var bottomCrop = 0
    private var leftCrop = 0
    private var rightCrop = 0
    private var boundsInitialized = false

    private val topRect = Rect()
    private val bottomRect = Rect()
    private val leftRect = Rect()
    private val rightRect = Rect()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            if (!boundsInitialized){
                topCrop = 50
                bottomCrop = height - 50
                leftCrop = 50
                rightCrop = width - 50
                boundsInitialized = true
            }

            topRect.set(0, 0, width, topCrop)
            bottomRect.set(0, bottomCrop, width, height)
            leftRect.set(0, topCrop, leftCrop, bottomCrop)
            rightRect.set(rightCrop, topCrop, width, bottomCrop)

            drawRect(topRect, croppedPaint)
            drawRect(leftRect, croppedPaint)
            drawRect(bottomRect, croppedPaint)
            drawRect(rightRect, croppedPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val previousX = pointerCoords.x
        val previousY = pointerCoords.y

        val pointerIndex = 0
        event?.getPointerCoords(pointerIndex, pointerCoords)

        val x = pointerCoords.x
        val y = pointerCoords.y

        return when (event?.actionMasked){
            MotionEvent.ACTION_DOWN -> {
                selectedHandle = solveHandle(x, y)
                selectedHandle != null
            }
            MotionEvent.ACTION_UP -> {
                selectedHandle = null
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectedHandle != null){
                    val deltaX = (x - previousX).toInt()
                    val deltaY = (y - previousY).toInt()
                    when (selectedHandle){
                        HANDLE_TOPLEFT -> {
                            topCrop+=deltaY
                            leftCrop+=deltaX
                        }
                        HANDLE_TOP -> {
                            topCrop+=deltaY
                        }
                        HANDLE_TOPRIGHT -> {
                            topCrop+=deltaY
                            rightCrop+=deltaX
                        }
                        HANDLE_RIGHT -> {
                            rightCrop+=deltaX
                        }
                        HANDLE_BOTTOMRIGHT -> {
                            rightCrop+=deltaX
                            bottomCrop+=deltaY
                        }
                        HANDLE_BOTTOM -> {
                            bottomCrop+=deltaY
                        }
                        HANDLE_BOTTOMLEFT -> {
                            bottomCrop+=deltaY
                            leftCrop+=deltaX
                        }
                        HANDLE_LEFT -> {
                            leftCrop+=deltaX
                        }
                    }

                    invalidate()
                }
                true
            }
            else -> false
        }
    }

    private fun solveHandle(x: Float, y: Float): Int?{
        return if (y > topCrop - HANDLE_HITBOX && y < topCrop + HANDLE_HITBOX)
            if (x > leftCrop - HANDLE_HITBOX && x < leftCrop + HANDLE_HITBOX)
                HANDLE_TOPLEFT
            else if (x > rightCrop - HANDLE_HITBOX && x < rightCrop + HANDLE_HITBOX)
                HANDLE_TOPRIGHT
            else if (x > leftCrop && x < rightCrop)
                HANDLE_TOP
            else null
        else if (y > bottomCrop - HANDLE_HITBOX && y < bottomCrop + HANDLE_HITBOX)
            if (x > leftCrop - HANDLE_HITBOX && x < leftCrop + HANDLE_HITBOX)
                HANDLE_BOTTOMLEFT
            else if (x > rightCrop - HANDLE_HITBOX && x < rightCrop + HANDLE_HITBOX)
                HANDLE_BOTTOMRIGHT
            else if (x > leftCrop && x < rightCrop)
                HANDLE_BOTTOM
            else null
        else if (y > topCrop && y < bottomCrop)
            if (x > leftCrop - HANDLE_HITBOX && x < leftCrop + HANDLE_HITBOX)
                HANDLE_LEFT
            else if (x > rightCrop - HANDLE_HITBOX && x < rightCrop + HANDLE_HITBOX)
                HANDLE_RIGHT
            else null
        else null
    }

}