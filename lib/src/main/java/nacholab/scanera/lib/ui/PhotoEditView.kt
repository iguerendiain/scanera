package nacholab.scanera.lib.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withMatrix
import kotlin.math.atan2
import kotlin.math.hypot

class PhotoEditView: View {

    companion object {
        private const val DEFAULT_CONTRAST = 1f
        private const val DEFAULT_BRIGHTNESS = 0f
        private const val DEFAULT_SATURATION = 1f
        private const val DEFAULT_ROTATION = 0f
        private const val DEFAULT_SCALE = 1f
    }

    enum class TouchInteractionMode{
        IDLE,
        MOVING,
        MULTITOUCHROTSCALING
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private var photoEditBackgroundColor = Color.MAGENTA

    var inputBitmap: Bitmap? = null
    set(value){
        field = value

        value?.let {
            sourceRect.set(
                0,
                0,
                it.width,
                it.height
            )

            baseScale = -1f
            baseX = -1f
            baseY = -1f
        }

    }

    var currentContrast = DEFAULT_CONTRAST
    var currentBrightness = DEFAULT_BRIGHTNESS
    var currentSaturation = DEFAULT_SATURATION
    var currentRotation = DEFAULT_ROTATION
    var centerX = 0f
    var centerY = 0f
    var currentScale = DEFAULT_SCALE

    var baseScale = 1f
    var baseX = 0f
    var baseY = 0f

    private val bitmapPaint by lazy {
        Paint().apply {
            colorFilter = buildColorFilter()
        }
    }

    private val debugBGPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = Color.argb(128,0,0,0)
        }
    }

    private val debugTXTPaint by lazy {
        Paint().apply{
            style = Paint.Style.FILL
            color = Color.WHITE
            textSize = 20f
        }
    }


    private val bitmapMatrix = Matrix()
    private val sourceRect = Rect()

    fun updateColorFilter() {
        bitmapPaint.colorFilter = buildColorFilter()
    }

    private fun buildColorFilter() =
        ColorMatrixColorFilter(
            ColorMatrix().apply {
                setSaturation(currentSaturation)
                postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            currentContrast, 0f, 0f, 0f, currentBrightness,
                            0f, currentContrast, 0f, 0f, currentBrightness,
                            0f, 0f, currentContrast, 0f, currentBrightness,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
        )

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawColor(photoEditBackgroundColor)
            inputBitmap?.let { bitmap ->
                if (baseScale == -1f) {
                    val imgW = bitmap.width.toFloat()
                    val imgH = bitmap.height.toFloat()

                    val xScale = width / imgW
                    val yScale = height / imgH

                    baseScale = xScale.coerceAtMost(yScale)

                    val newWidth = imgW * baseScale
                    val newHeight = imgH * baseScale

                    baseX = width / 2f - newWidth / 2f
                    baseY = height / 2f - newHeight / 2f

                    bitmapMatrix.postScale(baseScale, baseScale)
                    bitmapMatrix.postTranslate(baseX, baseY)
                }

                withMatrix(bitmapMatrix) {
                    drawBitmap(bitmap, 0f, 0f, bitmapPaint)
                }
            }

            drawDebug(canvas)
        }
    }

    private fun drawDebug(canvas: Canvas){
        canvas.drawRect(
            0f,
            canvas.height - 30f,
            canvas.width.toFloat(),
            canvas.height.toFloat(),
            debugBGPaint
        )

        canvas.drawText(
            "($centerX;$centerY) @ $currentRotation X $currentScale",
            0f,
            canvas.height - 5f,
            debugTXTPaint
        )
    }

    fun reset() {
        currentSaturation = DEFAULT_SATURATION
        currentContrast = DEFAULT_CONTRAST
        currentBrightness = DEFAULT_BRIGHTNESS
        currentRotation = DEFAULT_ROTATION
        centerX = 0f
        centerY = 0f
        currentScale = DEFAULT_SCALE
        bitmapMatrix.reset()
        bitmapMatrix.setScale(baseScale, baseScale)
        bitmapMatrix.postTranslate(baseX, baseY)
        updateColorFilter()
        invalidate()
    }

    fun rotate(angle: Float){
        bitmapMatrix.postRotate(angle, width / 2f, height / 2f)
        currentRotation+=angle
    }

    private val lastPrimaryPosition = PointF()
    private val lastSecondaryPosition = PointF()
    private var primaryPointer: PointerData? = null
    private var secondaryPointer: PointerData? = null
    private val pointers = arrayListOf<PointerData>()
    private var mode = TouchInteractionMode.IDLE

    @Suppress("NAME_SHADOWING")
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return event?.let { event ->
            val pointerIndex = event.actionIndex
            val pointerId = event.getPointerId(pointerIndex)

            val x = event.getX(pointerIndex)
            val y = event.getY(pointerIndex)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {

                    lastPrimaryPosition.x = x
                    lastPrimaryPosition.y = y
                    PointerData(
                        pointerId,
                        x,
                        y
                    ).let {
                        primaryPointer = it
                        pointers.add(it)
                    }

                    mode = TouchInteractionMode.MOVING
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    PointerData(
                        pointerId,
                        x,
                        y
                    ).let {
                        pointers.add(it)
                        if (secondaryPointer == null) secondaryPointer = it
                    }

                    mode = TouchInteractionMode.MULTITOUCHROTSCALING

                    primaryPointer?.let {
                        lastPrimaryPosition.x = it.x
                        lastPrimaryPosition.y = it.y
                    }

                    secondaryPointer?.let {
                        lastSecondaryPosition.x = it.x
                        lastSecondaryPosition.y = it.y
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    for (p in 0 until event.pointerCount) {
                        pointers.find { it.pointerId == event.getPointerId(p) }?.also {
                            it.x = event.getX(p)
                            it.y = event.getY(p)
                        }
                    }

                    when (mode) {
                        TouchInteractionMode.MOVING -> handleMoveEvent()
                        TouchInteractionMode.MULTITOUCHROTSCALING -> handleMultitouchRotScaling()
                        else -> {
                        }
                    }

                    primaryPointer?.let {
                        lastPrimaryPosition.x = it.x
                        lastPrimaryPosition.y = it.y
                    }

                    secondaryPointer?.let {
                        lastSecondaryPosition.x = it.x
                        lastSecondaryPosition.y = it.y
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    pointers.find { it.pointerId == pointerId }?.let { pointers.remove(it) }

                    if (primaryPointer?.pointerId == pointerId) {
                        primaryPointer = secondaryPointer
                        secondaryPointer = null
                    }
                    if (secondaryPointer?.pointerId == pointerId) secondaryPointer = null

                    if (pointers.isNotEmpty()) {
                        if (pointers.size == 1) mode =
                            TouchInteractionMode.MOVING

                        if (primaryPointer == null) primaryPointer = pointers.first()
                        if (secondaryPointer == null) secondaryPointer =
                            pointers.find { it != primaryPointer }

                        primaryPointer?.let {
                            lastPrimaryPosition.x = it.x
                            lastPrimaryPosition.y = it.y
                        }

                        secondaryPointer?.let {
                            lastSecondaryPosition.x = it.x
                            lastSecondaryPosition.y = it.y
                        }
                    } else {
                        primaryPointer = null
                        secondaryPointer = null
                    }
                }
                MotionEvent.ACTION_UP -> {
                    pointers.clear()
                    mode = TouchInteractionMode.IDLE
                }
            }
            true
        }?:false
    }

    private fun handleMoveEvent(){
        primaryPointer?.let {
            val deltaX = it.x - lastPrimaryPosition.x
            val deltaY = it.y - lastPrimaryPosition.y
            centerX += deltaX
            centerY += deltaY
            bitmapMatrix.postTranslate(deltaX, deltaY)
            invalidate()
        }
    }

    private fun handleMultitouchRotScaling(){
        primaryPointer?.let { pointer1 -> secondaryPointer?.let { pointer2 ->
            val primaryCurrentPos = PointF(pointer1.x, pointer1.y)
            val secondaryCurrentPos = PointF(pointer2.x, pointer2.y)

            val currentMiddlePointX = (secondaryCurrentPos.x + primaryCurrentPos.x) / 2f
            val currentMiddlePointY = (secondaryCurrentPos.y + primaryCurrentPos.y) / 2f

            val lastMiddlePointX = (lastSecondaryPosition.x + lastPrimaryPosition.x) / 2f
            val lastMiddlePointY = (lastSecondaryPosition.y + lastPrimaryPosition.y) / 2f

            val currentPointsDistance = hypot(
                primaryCurrentPos.x - secondaryCurrentPos.x,
                secondaryCurrentPos.y - primaryCurrentPos.y
            )

            val lastPointsDistance = hypot(
                lastPrimaryPosition.x - lastSecondaryPosition.x,
                lastSecondaryPosition.y - lastPrimaryPosition.y
            )

            val scale = if (lastPointsDistance!=0f) currentPointsDistance / lastPointsDistance else 1f

            val currentAngle = atan2(
                primaryCurrentPos.x - secondaryCurrentPos.x,
                secondaryCurrentPos.y - primaryCurrentPos.y
            ) * 180 / Math.PI

            val previousAngle = atan2(
                lastPrimaryPosition.x - lastSecondaryPosition.x,
                lastSecondaryPosition.y - lastPrimaryPosition.y
            ) * 180 / Math.PI

            val pivotX = width / 2f
            val pivotY = height / 2f
            val deltaAngle = (currentAngle - previousAngle).toFloat()
            val deltaX = currentMiddlePointX - lastMiddlePointX
            val deltaY = currentMiddlePointY - lastMiddlePointY

            centerX += deltaX
            centerY += deltaY
            currentScale *= scale
            currentRotation += deltaAngle

            bitmapMatrix.postTranslate(deltaX, deltaY)
            bitmapMatrix.postScale(scale, scale, pivotX, pivotY)
            bitmapMatrix.postRotate(deltaAngle, pivotX, pivotY)

            invalidate()
        }}
    }

    data class PointerData(
        val pointerId: Int,
        var x: Float,
        var y: Float
    ){
        fun toString(hierarchy: Int): String{
            return "[$pointerId]: $x , $y ($hierarchy)"
        }
    }

}
