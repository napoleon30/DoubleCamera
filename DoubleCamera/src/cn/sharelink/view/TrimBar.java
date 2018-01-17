package cn.sharelink.view;

import cn.sharelink.DoubleCameras.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;


public class TrimBar extends View {

	private Bitmap mBmpTrimBar_bg;
	private Bitmap mBmpTrim_point;
	
	private int mViewWidth, mViewHeight;
	private boolean isPreDraw = false;
	
	private int mMaxProgress;
	private int mCurProgress;
	private boolean isHorz;
	
	public TrimBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.myview);
		mMaxProgress = a.getInteger(R.styleable.myview_trim_max, 100);
		mCurProgress = a.getInteger(R.styleable.myview_trim_progress, 50);
		isHorz = a.getBoolean(R.styleable.myview_orientation, true);
		
		if(isHorz) {
			mBmpTrimBar_bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.trimbar_bg0);
			mBmpTrim_point = BitmapFactory.decodeResource(context.getResources(), R.drawable.trim_point0);
		} else {
			mBmpTrimBar_bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.trimbar_bg1);
			mBmpTrim_point = BitmapFactory.decodeResource(context.getResources(), R.drawable.trim_point1);
		}
		
		
		getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			
			// 调用该方法时可以获取view实际的宽getWidth()和高getHeight()
			@Override
			public boolean onPreDraw() {
				// TODO Auto-generated method stub
				getViewTreeObserver().removeOnPreDrawListener(this); 
				
				mViewWidth = getWidth();
				mViewHeight = getHeight();
				
				isPreDraw = true;

				return true;
			}
		});
	}
	
	@SuppressLint("DrawAllocation") 
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawBitmap(mBmpTrimBar_bg, null, new Rect(0, 0, mViewWidth, mViewHeight), null);
		if(isHorz) {
			int position = mViewWidth * mCurProgress / mMaxProgress;
			canvas.drawBitmap(mBmpTrim_point, null, new Rect(position - 10, 0, position + 10, mViewHeight), null);
		} else {
			int position = mViewHeight * (mMaxProgress - mCurProgress) / mMaxProgress;
			canvas.drawBitmap(mBmpTrim_point, null, new Rect(0, position - 10, mViewWidth, position + 10), null);
		}
	}
	
	public void setMax(int max) {
		mMaxProgress = max;
	}
	public int getMax() {
		return mMaxProgress;
	}

	public void setProgress(int progress) {
		mCurProgress = progress;
		if(mCurProgress > mMaxProgress) {
			mCurProgress = mMaxProgress;
		} else if(mCurProgress < 0) {
			mCurProgress = 0;
		}
		postInvalidate();
	}
	public int getProgress() {
		return mCurProgress;
	}
	
}
