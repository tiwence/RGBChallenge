package com.tiwence.rgbcolorchallenge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DiskView extends View {

	private int mRed = 0;
	private int mGreen = 0;
	private int mBlue = 0;

	private boolean isRightChoice = false;

	public boolean isRightChoice() {
		return isRightChoice;
	}

	public DiskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
	}

	public DiskView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setClickable(true);
	}


	public void setDiskViewColor(int[] rgbColors) {
		this.mRed = rgbColors[0];
		this.mGreen = rgbColors[1];
		this.mBlue = rgbColors[2];
		this.invalidate();
	}

	

	public int getRed() {
		return mRed;
	}

	public void setRed(int mRed) {
		this.mRed = mRed;
	}

	public int getGreen() {
		return mGreen;
	}

	public void setGreen(int mGreen) {
		this.mGreen = mGreen;
	}

	public int getBlue() {
		return mBlue;
	}

	public void setBlue(int mBlue) {
		this.mBlue = mBlue;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int x = getWidth();
		int y = getHeight();
		int radius;
		radius = 100;
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		canvas.drawPaint(paint);
		// Use Color.parseColor to define HTML colors
		paint.setColor(Color.rgb(mRed, mGreen, mBlue));
		canvas.drawCircle(x / 2, y / 2, radius, paint);
	}

	public void setRightChoice(boolean isRightChoice) {
		this.isRightChoice = isRightChoice;
	}

	public DiskView(Context context) {
		super(context);
		setClickable(true);
	}

}
