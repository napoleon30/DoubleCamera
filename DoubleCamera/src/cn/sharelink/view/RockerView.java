package cn.sharelink.view;

import cn.sharelink.DoubleCameras.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

public class RockerView extends View implements SensorEventListener,
		OnGestureListener {

	// public RockerView(Context context, AttributeSet attrs, int defStyleAttr)
	// {
	// super(context, attrs, defStyleAttr);
	// // TODO Auto-generated constructor stub
	// }

	private String TAG = "RockerView";
	

	// 回弹
	private boolean isRecover_x; // x轴方向是否回弹
	private boolean isRecover_y; // y轴方向是否回弹
	// 锁定
	private boolean isLocked_x; // x轴锁定
	private boolean isLocked_y; // y轴锁定

	// 固定摇杆背景圆形的X,Y坐标以及半径
	private float mRockerBg_X;
	private float mRockerBg_Y;
	private float mRockerBg_R;
	// 摇杆的X,Y坐标以及摇杆的半径
	private float mRockerBtn_X = 0;
	private float mRockerBtn_Y = 0;
	private float mRockerBtn_R;
	private Bitmap mBmpRockerBg;
	private Bitmap mBmpRockerBtn;

	private PointF mCenterPoint;
	private PointF mTopPoint;
	private PointF mBottomPoint;
	private PointF mLeftPoint;
	private PointF mRightPoint;

	private boolean isPreDraw;
	public float maxData;

	private SensorManager mSensorManager = null;
	private Context context;

	public static int rockWidth;
	public static int rockHeight;

	public GestureDetector gestureDetector;

	public RockerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
		mBmpRockerBg = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.rocker_bg1);
		mBmpRockerBtn = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.rocker_btn2);

		TypedArray a = context
				.obtainStyledAttributes(attrs, R.styleable.myview);
		isRecover_x = a.getBoolean(R.styleable.myview_recover_x, true);
		isRecover_y = a.getBoolean(R.styleable.myview_recover_y, true);
		isLocked_x = a.getBoolean(R.styleable.myview_locked_x, false);
		isLocked_y = a.getBoolean(R.styleable.myview_locked_y, false);
		isPreDraw = false;

		getViewTreeObserver().addOnPreDrawListener(
				new ViewTreeObserver.OnPreDrawListener() {

					// 调用该方法时可以获取view实际的宽getWidth()和高getHeight()
					@Override
					public boolean onPreDraw() {
						// TODO Auto-generated method stub
						getViewTreeObserver().removeOnPreDrawListener(this);

						rockWidth = getWidth();
						rockHeight = getHeight();

						mRockerBg_R = 0.8f * getWidth() / 2;
						mRockerBtn_R = 0.2f * getWidth() / 2;

						maxData = mRockerBg_R - mRockerBtn_R;

						mCenterPoint = new PointF(getWidth() / 2,
								getHeight() / 2);
						mTopPoint = new PointF(mCenterPoint.x, mCenterPoint.y
								- maxData);
						mBottomPoint = new PointF(mCenterPoint.x,
								mCenterPoint.y + maxData);
						mLeftPoint = new PointF(mCenterPoint.x - maxData,
								mCenterPoint.y);
						mRightPoint = new PointF(mCenterPoint.x + maxData,
								mCenterPoint.y);

						mRockerBg_X = mCenterPoint.x;
						mRockerBg_Y = mCenterPoint.y;

						mRockerBtn_X = isRecover_x ? mCenterPoint.x
								: mLeftPoint.x;
						// mRockerBtn_Y = isRecover_y ? mCenterPoint.y :
						// mBottomPoint.y;
						mRockerBtn_Y = mCenterPoint.y;

						isPreDraw = true;

						return true;
					}
				});
		gestureDetector = new GestureDetector(context, this);
	}

	boolean isDrawing = true;

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		isDrawing = true;
		canvas.drawBitmap(mBmpRockerBg, null, new Rect(
				(int) (mRockerBg_X - mRockerBg_R),
				(int) (mRockerBg_Y - mRockerBg_R),
				(int) (mRockerBg_X + mRockerBg_R),
				(int) (mRockerBg_Y + mRockerBg_R)), null);
		canvas.drawBitmap(mBmpRockerBtn, null, new Rect(
				(int) (mRockerBtn_X - mRockerBtn_R),
				(int) (mRockerBtn_Y - mRockerBtn_R),
				(int) (mRockerBtn_X + mRockerBtn_R),
				(int) (mRockerBtn_Y + mRockerBtn_R)), null);
		isDrawing = false;
	}

	public boolean isUseSensor = false; // 是否采用重力传感器控制

	public void setUseSensor(boolean isUseSensor) {
		if (mSensorManager == null && isUseSensor) {
			mSensorManager = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
					SensorManager.SENSOR_DELAY_GAME);
		}

		this.isUseSensor = isUseSensor;
		if (!isUseSensor) {
			if (isRecover_x) {
				mRockerBtn_X = mCenterPoint.x;
			}

			if (isRecover_y) {
				mRockerBtn_Y = mCenterPoint.y;
			}
			reportRockerChange(0.0f, 0.0f);
			if (!isDrawing) {
				postInvalidate();
			}
		}
	}

	private long last_TouchTime = 0; // 上次触摸的时间
	private boolean isTouchValid = false; // 触摸是否有效
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.i("MotionEvent", "RockerView  dispatch");
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// TODO Auto-generated method stub
		if (isUseSensor) {
			return false;
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			Log.i("MotionEvent", "RockerView UP");
			onSingleTapUp(event);
			setVisibility(View.INVISIBLE);
		}
		gestureDetector.onTouchEvent(event);

		reportRockerChange((mRockerBtn_X - mCenterPoint.x) / maxData,
				(mCenterPoint.y - mRockerBtn_Y) / maxData);

		if (!isDrawing) {
			postInvalidate();
		}

		return true;
	}

	/***
	 * 得到两点之间的弧度
	 */
	public double getRad(float px1, float py1, float px2, float py2) {
		// 得到两点X的距离
		float x = px2 - px1;
		// 得到两点Y的距离
		float y = py1 - py2;
		// 算出斜边长
		float xie = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		// 得到这个角度的余弦值（通过三角函数中的定理 ：邻边/斜边=角度余弦值）
		float cosAngle = x / xie;
		// 通过反余弦定理获取到其角度的弧度
		float rad = (float) Math.acos(cosAngle);
		// 注意：当触屏的位置Y坐标<摇杆的Y坐标我们要取反值-0~-180
		if (py2 < py1) {
			rad = -rad;
		}
		return rad;
	}

	private static final float SENSOR_MAXVALUE = 9.81f;

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

		if (!isUseSensor) {
			return;
		}
		float[] values = event.values;

		if (values[2] > 0) { // 手机正面朝上
			if (isRecover_x) {
				setRockerBtn_x(values[1] / SENSOR_MAXVALUE * maxData
						+ mCenterPoint.x);
			}

			if (isRecover_y) {
				setRockerBtn_y(values[0] / SENSOR_MAXVALUE * maxData
						+ mCenterPoint.x);
			}

			reportRockerChange(values[1] / SENSOR_MAXVALUE, -values[0]
					/ SENSOR_MAXVALUE);

			if (!isDrawing) {
				postInvalidate();
			}
		}
		// Log.i("gravity", String.format("%.2f", values[0]) + "/" +
		// String.format("%.2f", values[1]) + "/" +
		// String.format("%.2f", values[2]));
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	protected void reportRockerChange(float x, float y) {
		if (mRockerChangeListener != null) {

			if (x > 1.0f) {
				x = 1.0f;
			} else if (x < -1.0f) {
				x = -1.0f;
			}

			if (y > 1.0f) {
				y = 1.0f;
			} else if (y < -1.0f) {
				y = -1.0f;
			}
			if (isLocked_y) {
				mRockerChangeListener.report(RockerView.this, x, -1.0f);
			} else {
				mRockerChangeListener.report(RockerView.this, x, y);
			}
		}
	}

	/**
	 * 
	 * @param R
	 *            圆周运动的旋转点
	 * @param centerX
	 *            旋转点X
	 * @param centerY
	 *            旋转点Y
	 * @param rad
	 *            旋转的弧度
	 */
	public void getXY(float centerX, float centerY, float R, double rad) {
		// 获取圆周运动的X坐标
		if (!isLocked_x) { // 没有锁定x轴
			setRockerBtn_x((float) (R * Math.cos(rad)) + centerX);
		}
		// 获取圆周运动的Y坐标
		if (!isLocked_y) { // 没有锁定y轴
			setRockerBtn_y((float) (R * Math.sin(rad)) + centerY);
		}
	}

	private void setRockerBtn_x(float x) {
		mRockerBtn_X = x;
		if (mRockerBtn_X < mLeftPoint.x) {
			mRockerBtn_X = mLeftPoint.x;
		} else if (mRockerBtn_X > mRightPoint.x) {
			mRockerBtn_X = mRightPoint.x;
		}
	}

	private void setRockerBtn_y(float y) {
		mRockerBtn_Y = y;
		if (mRockerBtn_Y < mTopPoint.y) {
			mRockerBtn_Y = mTopPoint.y;
		} else if (mRockerBtn_Y > mBottomPoint.y) {
			mRockerBtn_Y = mBottomPoint.y;
		}
	}

	public void setRecover_x(boolean isRecover_x) {
		this.isRecover_x = isRecover_x;
		if (isPreDraw) {
			mRockerBtn_X = isRecover_x ? mCenterPoint.x
					: (mCenterPoint.x + maxData);
			postInvalidate();
		}
	}

	public void setRecover_y(boolean isRecover_y) {
		this.isRecover_y = isRecover_y;
		if (isPreDraw) {
			mRockerBtn_Y = isRecover_y ? mCenterPoint.y
					: (mCenterPoint.y + maxData);
			postInvalidate();
		}
	}

	public void setLocked_y(boolean isLocked_y, int posion) {
		this.isLocked_y = isLocked_y;
		if (isPreDraw) {
			if (posion == 1) { // 底部
				mRockerBtn_Y = mCenterPoint.y + maxData;
			} else if (posion == 2) { // 中间
				mRockerBtn_Y = mCenterPoint.y;
			} else if (posion == 3) { // 顶部
				mRockerBtn_Y = mCenterPoint.y - maxData;
			}
			reportRockerChange((mRockerBtn_X - mCenterPoint.x) / maxData,
					(mCenterPoint.y - mRockerBtn_Y) / maxData);
			postInvalidate();
		}
	}

	public void destory() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
	}

	public interface RockerChangeListener {
		public void report(View v, float x, float y);
	}

	private RockerChangeListener mRockerChangeListener = null;

	public void setRockerChangeListener(
			RockerChangeListener rockerChangeListener) {
		mRockerChangeListener = rockerChangeListener;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// 当释放按键时摇杆要恢复摇杆的位置为初始位置
		if (isRecover_x) {
			mRockerBtn_X = mCenterPoint.x;
		}

		if (isRecover_y) {
			mRockerBtn_Y = mCenterPoint.y;
		}
		// isTouchValid = false;

		reportRockerChange((mRockerBtn_X - mCenterPoint.x) / maxData,
				(mCenterPoint.y - mRockerBtn_Y) / maxData);

		if (!isDrawing) {
			postInvalidate();
		}
		
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {

		// 当触屏区域不在活动范围内
		if (Math.sqrt(Math.pow((mRockerBg_X - (int) e2.getX()), 2)
				+ Math.pow((mRockerBg_Y - (int) e2.getY()), 2)) <= mRockerBg_R) {

			if (!isLocked_x) {
				mRockerBtn_X = e2.getX();
			}
			if (!isLocked_y) {
				mRockerBtn_Y = e2.getY();
			}

		}

		reportRockerChange((mRockerBtn_X - mCenterPoint.x) / maxData,
				(mCenterPoint.y - mRockerBtn_Y) / maxData);

		if (!isDrawing) {
			postInvalidate();
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
}
