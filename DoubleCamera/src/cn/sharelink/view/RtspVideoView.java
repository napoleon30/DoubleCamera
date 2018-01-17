package cn.sharelink.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.sharelink.DoubleCameras.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RtspVideoView extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final String TAG = "RtspVideoView";

	public static final int START_RTSP = 1;
	public static final int STOP_RTSP = 2;
	public static final int SNAPSHOT = 3;
	public static final int RECORD = 4;

	private int mWidth, mHeight;
	private SurfaceHolder mHolder;
	public DrawThread mDrawThread;
	public DecoderThread mDecoderThread;
	public DecoderThread sDecoderThread;

	private int bmp_Width = 640;
	private int bmp_height = 368;
	public byte[] yuv_pixel;
	private Bitmap mBitmap;
	private boolean isGetBitmap = false;
	private Rect displayRect = new Rect();
	private String mRtspUrl = null;
	private Handler mHandler = null;
	private Bitmap startBitmap;
	private boolean isGetSurface;
	private ByteBuffer buffer;
	private int mIndex;

	public RtspVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mHolder = this.getHolder();
		mHolder.addCallback(this);
		mHolder.setFormat(PixelFormat.RGB_565);
		mDecoderThread = new DecoderThread();
		mDrawThread = new DrawThread();

		startBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.second_pic);
		isGetSurface = false;
	}

	class DecoderThread implements Runnable {
		int time_out = 1;
		boolean isRun;
		boolean isPause;

		public void doPause() {
			isPause = true;
		}

		public synchronized void doResume() {
			isPause = false;
			notify();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isRun = true;
			isPause = false;
			mIndex = StartRtsp(mRtspUrl, AppUtil.Transport);
			Log.e("JNIINDEX", "" + mIndex);
			if (mIndex == 3) {
				sendHandler(START_RTSP, false, null);
				// Log.e("RVV", "THIS UNISIS DECODER");
			} else {
				int w = GetWidth(mIndex);
				int h = GetHeight(mIndex);
				Log.e("test", "width = " + w + ";" + "height=" + h);
				getBitmap(w, h);
				Log.e("RVV", "THIS ISIS DECODER");
				while (isRun) {
					if (isPause) {
						try {
							synchronized (this) {
								wait();
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						Log.e("RVV", "THIS IS THREAD OF DECODER====" + mIndex);
						boolean isAllRight = Decoder(mIndex);

						if (!isAllRight) {
							// Log.e("RVV", "this is allRight");
						}
					}
				}
				sendHandler(STOP_RTSP, true, null);
			}
			Release(mIndex);
		}
	}

	class DrawThread implements Runnable {
		boolean isRun = false;
		boolean isPause;
		String imageName;
		boolean isSnapshot;

		public void snapshot(String imageName) {
			this.imageName = imageName;
			isSnapshot = true;
		}

		private void doSnapshot(Bitmap bitmap) {
			try {
				boolean isSuccess;
				File file = new File(imageName);
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream fos = new FileOutputStream(file);
				isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
						fos);
				fos.close();
				Log.i("TAG", "Save as" + imageName);
				sendHandler(SNAPSHOT, isSuccess, imageName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void doPause() {
			isPause = true;
		}

		public synchronized void doResume() {
			isPause = false;
			notify();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isRun = true;
			isSnapshot = false;
			isPause = false;
			Log.e("test", "开启画图线程");
			while (isRun) {
				if (isPause) {
					try {
						synchronized (this) {
							wait();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (isGetSurface) {
					if (GetYUV(yuv_pixel, mIndex)) {
						buffer = ByteBuffer.wrap(yuv_pixel);
						if (buffer != null) {
							mBitmap.copyPixelsFromBuffer(buffer);
							buffer = null;
							// System.gc();
							if (isSnapshot) {
								doSnapshot(mBitmap);
								isSnapshot = false;
							}
							Canvas canvas = mHolder.lockCanvas(null);
							if (isRotate) {
								canvas.rotate(180.0f);
								canvas.translate(-displayRect.width(),
										-displayRect.height());
							}

							// synchronized (mBitmap) { //
							// ////////////////////

							canvas.drawBitmap(mBitmap, null, displayRect, null);
							// }

							//Log.e("test", "画出一副图像");
							mHolder.unlockCanvasAndPost(canvas);
						}
					} else {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void setVideo(String url, Handler handler) {
		mRtspUrl = url;
		mHandler = handler;

		if (mDecoderThread != null) {
			Thread t2 = new Thread(mDecoderThread);
			t2.start();
		}

		if (mDrawThread != null) {
			Thread t1 = new Thread(mDrawThread);
			t1.start();
		}

	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	private void sendHandler(int what, boolean isSuccess, String msg) {
		if (mHandler != null) {
			mHandler.obtainMessage(what, new HandlerMsg(isSuccess, msg))
					.sendToTarget();
		}
	}

	public class HandlerMsg {
		public boolean isSuccess;
		public String msg;

		public HandlerMsg(boolean isSuccess, String msg) {
			// TODO Auto-generated constructor stub
			this.isSuccess = isSuccess;
			this.msg = msg;

		}
	}

	public void takeSnapShot(String imageName) {
		mDrawThread.snapshot(imageName);

	}

	public void videoRecordStart(final String videoName) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!StartRecord(videoName, mIndex)) {
					Log.e("aa", "===+StartRecord(videoName,a)   false ");
					sendHandler(RECORD, false, videoName);
				} else {
					sendHandler(RECORD, true, videoName);
					Log.e("aa", "===+StartRecord(videoName,a)    true");

				}
			}
		}).start();
	}

	public boolean isReady() {

		return IsConnected(mIndex);
	}

	public boolean videoIsRecording() {
		return IsRecording(mIndex);
	}

	public void videoRecordStop() {
		StopRecord(mIndex);
	}

	public void videoPause() {
		mDrawThread.doPause();
		mDecoderThread.doPause();

	}

	public void videoResume() {
		mDrawThread.doResume();
		mDecoderThread.doResume();

	}

	public void destory() {
		mDecoderThread.isRun = false;
		Log.e("AAAAAAAA", "===============");
		mDrawThread.isRun = false;

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		mWidth = width;
		mHeight = height;
		displayRect.set(0, 0, mWidth, mHeight);

		if (!isGetSurface) {
			Canvas canvas = mHolder.lockCanvas(null);
			canvas.drawBitmap(startBitmap, null, displayRect, null);
			mHolder.unlockCanvasAndPost(canvas);
		}
		isGetSurface = true;
		Log.i(TAG, "surface:" + width + "/" + height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		isGetSurface = false;
	}

	boolean isRotate = false;

	public void rotate() {
		isRotate = !isRotate;
	}

	public void rotate(boolean isRotate) {
		this.isRotate = isRotate;
	}

	public void getBitmap(int w, int h) {
		bmp_Width = w;
		bmp_height = h;

		yuv_pixel = new byte[w * h * 4];
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		isGetBitmap = true;
		// Log.i(TAG, "bmp:" + bmp_Width + "/" + bmp_height);
	}

	static
	{
		System.loadLibrary("avcodec");
		System.loadLibrary("avdevice");
		System.loadLibrary("avfilter");

		System.loadLibrary("avutil");
		System.loadLibrary("avformat");
		System.loadLibrary("swresample");
		System.loadLibrary("swscale");
		
		
		System.loadLibrary("client1");
	}

	private native int StartRtsp(String rtspUrl, String trans);

	private native boolean Decoder(int index);

	private native int GetWidth(int index);

	private native int GetHeight(int index);

	private native boolean IsConnected(int index);

	private native boolean GetYUV(byte[] yuv, int index);

	private native void Release(int index);

	private native boolean StartRecord(String path, int index);

	private native boolean IsRecording(int index);

	private native void StopRecord(int index);
}
