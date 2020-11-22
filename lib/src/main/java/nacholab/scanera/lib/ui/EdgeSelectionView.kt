package nacholab.scanera.lib.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class EdgeSelectionView: View {

    companion object{
        private const val HANDLE_HITBOX = 50f
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    )

    private val handlePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }

    private val edgeSetupPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.argb(128, 0, 0, 0)
            style = Paint.Style.FILL
        }
    }

    val handles by lazy {
        mutableListOf(
                arrayOf(200f, 200f),                    // TopLeft
                arrayOf(width - 200f, 200f),            // TopRight
                arrayOf(width - 200f, height - 200f),   // BottomRight
                arrayOf(200f, height - 200f)            // BottomLeft
        )
    }

    private val pointerCoords by lazy { MotionEvent.PointerCoords() }

    private val edgeSetupPath by lazy { Path() }

    private var selectedHandle = -1

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            edgeSetupPath.apply{
                reset()
                moveTo(0f, 0f)                        // VIEW - Top Left
                lineTo(width.toFloat(), 0f)              // VIEW - Top Right
                lineTo(handles[1][0], handles[1][1])        // DOC - Top Right
                lineTo(handles[0][0], handles[0][1])        // DOC - Top Left
                lineTo(handles[3][0], handles[3][1])        // DOC - Bottom Left
                lineTo(handles[2][0], handles[2][1])        // DOC - Bottom Right
                lineTo(handles[1][0], handles[1][1])        // DOC - Top Right
                lineTo(width.toFloat(), 0f)              // VIEW - Top Right
                lineTo(width.toFloat(), height.toFloat())   // VIEW - Bottom Right
                lineTo(0f, height.toFloat())             // VIEW - Bottom Left
                lineTo(0f, 0f)                        // VIEW - Top Left
            }

            drawPath(edgeSetupPath, edgeSetupPaint)

            drawCircle(handles[0][0], handles[0][1], 30f, handlePaint)
            drawCircle(handles[1][0], handles[1][1], 30f, handlePaint)
            drawCircle(handles[2][0], handles[2][1], 30f, handlePaint)
            drawCircle(handles[3][0], handles[3][1], 30f, handlePaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val pointerIndex = 0
        event?.getPointerCoords(pointerIndex, pointerCoords)

        val x = pointerCoords.x
        val y = pointerCoords.y

        return when (event?.actionMasked){
            MotionEvent.ACTION_DOWN -> {
                handles.forEachIndexed { idx, it ->
                    if (x > it[0] - HANDLE_HITBOX && x < it[0] + HANDLE_HITBOX && y > it[1] - HANDLE_HITBOX && y < it[1] + HANDLE_HITBOX){
                        selectedHandle = idx
                        return@forEachIndexed
                    }
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                selectedHandle = -1
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectedHandle != -1){
                    handles[selectedHandle][0] = x
                    handles[selectedHandle][1] = y
                    invalidate()
                }
                true
            }
            else -> false
        }
    }

}