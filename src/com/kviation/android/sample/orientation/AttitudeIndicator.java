
package com.kviation.android.sample.orientation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class AttitudeIndicator extends View {

  private static final boolean LOG_FPS = false;

  private static final int SKY_COLOR = Color.parseColor("#36B4DD");
  private static final int EARTH_COLOR = Color.parseColor("#865B4B");
  private static final int MIN_PLANE_COLOR = Color.parseColor("#E8D4BB");
  private static final float TOTAL_VISIBLE_PITCH_DEGREES = 45 * 2; // ± 45°

  private final PorterDuffXfermode mXfermode;
  private final Paint mBitmapPaint;
  private final Paint mEarthPaint;
  private final Paint mPitchLadderPaint;
  private final Paint mMinPlanePaint;
  private final Paint mBottomPitchLadderPaint;

  // These are created once and reused in subsequent onDraw calls.
  private Bitmap mSrcBitmap;
  private Canvas mSrcCanvas;
  private Bitmap mDstBitmap;

  private int mWidth;
  private int mHeight;

  private float mPitch = 0; // Degrees
  private float mRoll = 0; // Degrees, left roll is positive

  public AttitudeIndicator(Context context) {
    this(context, null);
  }

  public AttitudeIndicator(Context context, AttributeSet attrs) {
    super(context, attrs);

    mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    mBitmapPaint = new Paint();
    mBitmapPaint.setFilterBitmap(false);

    mEarthPaint = new Paint();
    mEarthPaint.setAntiAlias(true);
    mEarthPaint.setColor(EARTH_COLOR);

    mPitchLadderPaint = new Paint();
    mPitchLadderPaint.setAntiAlias(true);
    mPitchLadderPaint.setColor(Color.WHITE);
    mPitchLadderPaint.setStrokeWidth(3);

    mBottomPitchLadderPaint = new Paint();
    mBottomPitchLadderPaint.setAntiAlias(true);
    mBottomPitchLadderPaint.setColor(Color.WHITE);
    mBottomPitchLadderPaint.setStrokeWidth(3);
    mBottomPitchLadderPaint.setAlpha(128);

    mMinPlanePaint = new Paint();
    mMinPlanePaint.setAntiAlias(true);
    mMinPlanePaint.setColor(MIN_PLANE_COLOR);
    mMinPlanePaint.setStrokeWidth(5);
    mMinPlanePaint.setStyle(Paint.Style.STROKE);
  }

  public float getPitch() {
    return mPitch;
  }

  public float getRoll() {
    return mRoll;
  }

  public void setAttitude(float pitch, float roll) {
    mPitch = pitch;
    mRoll = roll;
    invalidate();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mWidth = w;
    mHeight = h;
  }

  private Bitmap getSrc() {
    if (mSrcBitmap == null) {
      mSrcBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
      mSrcCanvas = new Canvas(mSrcBitmap);
    }
    Canvas canvas = mSrcCanvas;

    float centerX = mWidth / 2;
    float centerY = mHeight / 2;

    // Background
    canvas.drawColor(SKY_COLOR);

    // Save the state without any rotation/translation so
    // we can revert back to it to draw the fixed components.
    canvas.save();

    // Orient the earth to reflect the pitch and roll angles
    canvas.rotate(mRoll, centerX, centerY);
    canvas.translate(0, (mPitch / TOTAL_VISIBLE_PITCH_DEGREES) * mHeight);

    // Draw the earth as a rectangle, well beyond the view bounds
    // to account for large nose-down pitch.
    canvas.drawRect(-mWidth, centerY, mWidth * 2, mHeight * 2, mEarthPaint);

    // Draw white horizon and top pitch ladder
    float ladderStepY = mHeight / 12;
    canvas.drawLine(-mWidth, centerY, mWidth * 2, centerY, mPitchLadderPaint);
    for (int i = 1; i <= 4; i++) {
      float y = centerY - ladderStepY * i;
      float width = mWidth / 8;
      canvas.drawLine(centerX - width / 2, y, centerX + width / 2, y, mPitchLadderPaint);
    }

    // Draw the bottom pitch ladder
    float bottomLadderStepX = mWidth / 12;
    float bottomLadderStepY = mWidth / 12;
    canvas.drawLine(centerX, centerY, centerX - bottomLadderStepX * 3.5f, centerY
        + bottomLadderStepY * 3.5f, mBottomPitchLadderPaint);
    canvas.drawLine(centerX, centerY, centerX + bottomLadderStepX * 3.5f, centerY
        + bottomLadderStepY * 3.5f, mBottomPitchLadderPaint);
    for (int i = 1; i <= 3; i++) {
      float y = centerY + bottomLadderStepY * i;
      canvas.drawLine(centerX - bottomLadderStepX * i, y, centerX + bottomLadderStepX * i, y,
          mBottomPitchLadderPaint);
    }

    // Return to normal to draw the miniature plane
    canvas.restore();

    // Draw the nose dot
    canvas.drawPoint(centerX, centerY, mMinPlanePaint);

    // Half-circle of miniature plane
    float minPlaneCircleRadiusX = mWidth / 6;
    float minPlaneCircleRadiusY = mHeight / 6;
    RectF wingsCircleBounds = new RectF(centerX - minPlaneCircleRadiusX, centerY
        - minPlaneCircleRadiusY, centerX + minPlaneCircleRadiusX, centerY + minPlaneCircleRadiusY);
    canvas.drawArc(wingsCircleBounds, 0, 180, false, mMinPlanePaint);

    // Wings of miniature plane
    float wingLength = mWidth / 6;
    canvas.drawLine(centerX - minPlaneCircleRadiusX - wingLength, centerY, centerX
        - minPlaneCircleRadiusX, centerY, mMinPlanePaint);
    canvas.drawLine(centerX + minPlaneCircleRadiusX, centerY, centerX + minPlaneCircleRadiusX
        + wingLength, centerY, mMinPlanePaint);

    // Draw vertical post
    canvas.drawLine(centerX, centerY + minPlaneCircleRadiusY, centerX, centerY
        + minPlaneCircleRadiusY + mHeight / 3, mMinPlanePaint);

    return mSrcBitmap;
  }

  private Bitmap getDst() {
    if (mDstBitmap == null) {
      mDstBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
      Canvas c = new Canvas(mDstBitmap);
      c.drawColor(Color.TRANSPARENT);

      Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
      p.setColor(Color.RED);
      c.drawOval(new RectF(0, 0, mWidth, mHeight), p);
    }
    return mDstBitmap;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (LOG_FPS) {
      countFps();
    }

    Bitmap src = getSrc();
    Bitmap dst = getDst();

    int sc = canvas.saveLayer(0, 0, mWidth, mHeight, null, Canvas.MATRIX_SAVE_FLAG
        | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
        | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

    canvas.drawBitmap(dst, 0, 0, mBitmapPaint);
    mBitmapPaint.setXfermode(mXfermode);
    canvas.drawBitmap(src, 0, 0, mBitmapPaint);
    mBitmapPaint.setXfermode(null);

    canvas.restoreToCount(sc);
  }

  private long frameCountStartedAt = 0;
  private long frameCount = 0;

  private void countFps() {
    frameCount++;
    if (frameCountStartedAt == 0) {
      frameCountStartedAt = System.currentTimeMillis();
    }
    long elapsed = System.currentTimeMillis() - frameCountStartedAt;
    if (elapsed >= 1000) {
      Log.i("FPS: " + frameCount);
      frameCount = 0;
      frameCountStartedAt = System.currentTimeMillis();
    }
  }
}
