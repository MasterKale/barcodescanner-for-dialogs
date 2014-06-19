package com.barcodescannerfordialogs.qrscanner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.barcodescannerfordialogs.R;

import me.dm7.barcodescanner.core.DisplayUtils;

public class ViewFinderView extends View {
	private static final String TAG = ViewFinderView.class.getSimpleName();

	private Rect mFramingRect;

	private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
	private int scannerAlpha;
	private static final int POINT_SIZE = 10;
	private static final long ANIMATION_DELAY = 80l;

	int mScannerWidth;
	int mScannerHeight;
	int mPadding;

	public ViewFinderView(Context context, int width, int height, int padding)
	{
		super(context);
		mScannerWidth = width;
		mScannerHeight = height;
		mPadding = padding;

		// DEBUG
		Log.i(TAG, "width: " + width + ", height: " + height + ", padding: " + padding);
	}

	public ViewFinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setupViewFinder() {
		initFramingRect();
		invalidate();
	}

	public Rect getFramingRect() {
		return mFramingRect;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if(mFramingRect == null) {
			return;
		}

		drawViewFinderMask(canvas);
		drawViewFinderBorder(canvas);
		drawLaser(canvas);
	}

	// This rectangle is used to define the size of the "viewfinder" displayed in the camera overlay
	public synchronized void initFramingRect() {
		Point screenResolution = DisplayUtils.getScreenResolution(getContext());
		if (screenResolution == null) {
			return;
		}

		mFramingRect = new Rect(mPadding, mPadding, mScannerWidth - mPadding, mScannerHeight - mPadding);
	}

	public void drawViewFinderMask(Canvas canvas) {
		Paint paint = new Paint();
		Resources resources = getResources();
		paint.setColor(resources.getColor(R.color.viewfinder_mask));

		// Coordinates of the corners of the viewfinder
		int right = canvas.getWidth();
		int bottom = canvas.getHeight();

		// Top
		canvas.drawRect(0, 0, right, mPadding, paint);
		// Left
		canvas.drawRect(0, mPadding, mPadding, (bottom - mPadding), paint);
		// Right
		canvas.drawRect((right - mPadding), mPadding, right, (bottom - mPadding), paint);
		// Bottom
		canvas.drawRect(0, (bottom - mPadding), right, bottom, paint);
	}

	public void drawViewFinderBorder(Canvas canvas) {
		Paint paint = new Paint();
		Resources resources = getResources();
		paint.setColor(resources.getColor(R.color.border_color));
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(resources.getInteger(R.integer.viewfinder_border_width));
		int lineLength = resources.getInteger(R.integer.viewfinder_border_length);

		// Coordinates of the corners of the viewfinder
		int right = canvas.getWidth() - mPadding;
		int bottom = canvas.getHeight() - mPadding;

		// Upper left
		canvas.drawLine(mPadding, mPadding, mPadding, mPadding + lineLength, paint); // Down
		canvas.drawLine(mPadding, mPadding, mPadding + lineLength, mPadding, paint); // Left

		// Lower Left
		canvas.drawLine(mPadding, bottom, mPadding, bottom - lineLength, paint); // Up
		canvas.drawLine(mPadding, bottom, mPadding + lineLength, bottom, paint); // Right

		// Upper Right
		canvas.drawLine(right, mPadding, right, mPadding + lineLength, paint); // Down
		canvas.drawLine(right, mPadding, right - lineLength, mPadding, paint); // Left

		// Bottom Right
		canvas.drawLine(right, bottom, right, bottom - lineLength, paint); // Up
		canvas.drawLine(right, bottom, right - lineLength, bottom, paint); // Right
	}

	public void drawLaser(Canvas canvas)
	{
		Paint paint = new Paint();
		Resources resources = getResources();
		// Draw a red "laser scanner" line through the middle to show decoding is active
		paint.setColor(resources.getColor(R.color.viewfinder_laser));
		paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
		paint.setStyle(Paint.Style.FILL);
		scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

		int middle = canvas.getHeight()/2;

		int left = mPadding;
		int top = middle - 1;
		int right = canvas.getWidth() - mPadding;
		int bottom = middle + 2;

		canvas.drawRect(left, top, right, bottom, paint);

		postInvalidateDelayed(ANIMATION_DELAY, left, top, right, bottom);
	}
}
