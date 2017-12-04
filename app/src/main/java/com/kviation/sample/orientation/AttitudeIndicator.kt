package com.kviation.sample.orientation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View

class AttitudeIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val mXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    private val mBitmapPaint = Paint().apply {
        isFilterBitmap = false
    }

    private val mSkyColor = Color.parseColor("#36B4DD")

    private val mEarthPaint = Paint().apply {
        color = Color.parseColor("#865B4B")
        isAntiAlias = true
    }

    private val mPitchLadderPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val mBottomPitchLadderPaint = Paint().apply {
        color = Color.WHITE
        alpha = 128
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val mMinPlanePaint = Paint().apply {
        color = Color.parseColor("#E8D4BB")
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val mSrcBitmap: Bitmap by lazy {
        Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
    }

    private val mSrcCanvas: Canvas by lazy {
        Canvas(mSrcBitmap)
    }

    private val mDstBitmap: Bitmap by lazy {
        val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED
        canvas.drawOval(RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat()), paint)
        bitmap
    }

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var frameCountStartedAt: Long = 0
    private var frameCount: Long = 0

    private var pitch = 0f // Degrees
    private var roll = 0f // Degrees, left roll is positive

    fun setAttitude(pitch: Float, roll: Float) {
        this.pitch = pitch
        this.roll = roll
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (LOG_FPS) {
            countFps()
        }
        val sc = saveLayer(canvas)
        canvas.drawBitmap(mDstBitmap, 0f, 0f, mBitmapPaint)
        mBitmapPaint.xfermode = mXfermode
        canvas.drawBitmap(getSrc(), 0f, 0f, mBitmapPaint)
        mBitmapPaint.xfermode = null
        canvas.restoreToCount(sc)
    }

    private fun saveLayer(canvas: Canvas): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return canvas.saveLayer(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), null)
        } else {
            @Suppress("DEPRECATION")
            return canvas.saveLayer(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        }
    }

    private fun getSrc(): Bitmap {
        val canvas = mSrcCanvas
        val width = mWidth.toFloat()
        val height = mHeight.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        // Background
        canvas.drawColor(mSkyColor)

        // Save the state without any rotation/translation so
        // we can revert back to it to draw the fixed components.
        canvas.save()

        // Orient the earth to reflect the pitch and roll angles
        canvas.rotate(roll, centerX, centerY)
        val totalVisiblePitchDegrees = 45f * 2 // +/- 45 degrees
        canvas.translate(0f, pitch / totalVisiblePitchDegrees * height)

        // Draw the earth as a rectangle, well beyond the view bounds
        // to account for large nose-down pitch.
        canvas.drawRect(-width, centerY, width * 2, height * 2, mEarthPaint)

        // Draw white horizon and top pitch ladder
        val ladderStepY = height / 12
        canvas.drawLine(-width, centerY, width * 2, centerY, mPitchLadderPaint)
        for (i in 1..4) {
            val y = centerY - ladderStepY * i
            val stepWidth = width / 8
            canvas.drawLine(centerX - stepWidth / 2, y, centerX + stepWidth / 2, y, mPitchLadderPaint)
        }

        // Draw the bottom pitch ladder
        val bottomLadderStepX = width / 12
        val bottomLadderStepY = width / 12
        canvas.drawLine(centerX, centerY, centerX - bottomLadderStepX * 3.5f, centerY + bottomLadderStepY * 3.5f, mBottomPitchLadderPaint)
        canvas.drawLine(centerX, centerY, centerX + bottomLadderStepX * 3.5f, centerY + bottomLadderStepY * 3.5f, mBottomPitchLadderPaint)
        for (i in 1..3) {
            val y = centerY + bottomLadderStepY * i
            canvas.drawLine(centerX - bottomLadderStepX * i, y, centerX + bottomLadderStepX * i, y, mBottomPitchLadderPaint)
        }

        // Return to normal to draw the miniature plane
        canvas.restore()

        // Draw the nose dot
        canvas.drawPoint(centerX, centerY, mMinPlanePaint)

        // Half-circle of miniature plane
        val minPlaneCircleRadiusX = width / 6
        val minPlaneCircleRadiusY = height / 6
        val wingsCircleBounds = RectF(centerX - minPlaneCircleRadiusX, centerY - minPlaneCircleRadiusY, centerX + minPlaneCircleRadiusX, centerY + minPlaneCircleRadiusY)
        canvas.drawArc(wingsCircleBounds, 0f, 180f, false, mMinPlanePaint)

        // Wings of miniature plane
        val wingLength = width / 6
        canvas.drawLine(centerX - minPlaneCircleRadiusX - wingLength, centerY, centerX - minPlaneCircleRadiusX, centerY, mMinPlanePaint)
        canvas.drawLine(centerX + minPlaneCircleRadiusX, centerY, centerX + minPlaneCircleRadiusX
                + wingLength, centerY, mMinPlanePaint)

        // Draw vertical post
        canvas.drawLine(centerX, centerY + minPlaneCircleRadiusY, centerX, centerY
                + minPlaneCircleRadiusY + height / 3, mMinPlanePaint)

        return mSrcBitmap
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

    private fun countFps() {
        frameCount++
        if (frameCountStartedAt == 0L) {
            frameCountStartedAt = System.currentTimeMillis()
        }
        val elapsed = System.currentTimeMillis() - frameCountStartedAt
        if (elapsed >= 1000) {
            Log.d(TAG, "FPS: " + frameCount)
            frameCount = 0
            frameCountStartedAt = System.currentTimeMillis()
        }
    }

    companion object {
        private val TAG = "AttitudeIndicator"
        private val LOG_FPS = false
    }
}
