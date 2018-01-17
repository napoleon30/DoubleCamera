package cn.sharelink.DoubleCameras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import cn.sharelink.MyApplication;
import cn.sharelink.view.AppUtil;
import cn.sharelink.view.ControlMsg;
import cn.sharelink.view.HttpThread;
import cn.sharelink.view.MenuButton;
import cn.sharelink.view.MyToast;
import cn.sharelink.view.PlayVoice;
import cn.sharelink.view.RockerView;
import cn.sharelink.view.RtspVideoView;
import cn.sharelink.view.SetupListViewAdapter;
import cn.sharelink.view.TrimView;
import cn.sharelink.view.MenuButton.MenuButtonOnClickListener;

public class PlayActivity extends Activity implements OnClickListener {

	private static final String TAG = "PlayActivity";
	private static String rtspUrl_nHD1 = "rtsp://192.168.100.1/cam1/h264-1";
	private static String rtspUrl_nHD2 = "rtsp://192.168.100.1/cam1/h264";
	String url01 = null;
	String url02 = null;

	// HTTP通信的控制参数
	private static final int HTTP_START = HttpThread.HTTP_START;
	private static final int HTTP_SET_TIME = HttpThread.HTTP_SET_TIME;
	private static final int HTTP_CHECK_STROAGE = HttpThread.HTTP_CHECK_STROAGE;
	private static final int HTTP_BRIDGE = HttpThread.HTTP_BRIDGE;
	private static final int HTTP_TAKEPHOTO = HttpThread.HTTP_TAKEPHOTO;
	private static final int HTTP_START_RECORD = HttpThread.HTTP_START_RECORD;
	private static final int HTTP_STOP_RECORD = HttpThread.HTTP_STOP_RECORD;
	private static final int HTTP_GET_PRIVILEGE = HttpThread.HTTP_GET_PRIVILEGE;
	private static final int HTTP_RELEASE_PRIVILEGE = HttpThread.HTTP_RELEASE_PRIVILEGE;
	private static final int HTTP_VERSION = HttpThread.HTTP_VERSION;
	private static final int HTTP_720p_1 = HttpThread.HTTP_720p_1;
	private static final int HTTP_720p_2 = HttpThread.HTTP_720p_2;
	private static final int HTTP_RESTART = HttpThread.HTTP_RESTART;

	// 各个窗口的layout定义
	private RelativeLayout mLayoutView_menu; // 菜单栏的layout
	private RelativeLayout mLayoutView_rocker; // 摇杆的layout
	private RelativeLayout mLayoutView_trim; // 微调的layout
	private RelativeLayout mLayoutView_screen; // 屏幕, 用于监听触摸屏幕
	private RelativeLayout.LayoutParams p;

	private ImageButton total;
	private ImageButton model1;
	private ImageButton model2;
	private ImageButton model3;
	private ImageButton model4;

	private RtspVideoView mVideoView1;
	private RtspVideoView mVideoView2;
	RelativeLayout layout;
	LayoutParams lp1, lp2;
	private int mWindow_width, mWindow_height;

	// mLayoutView_menu中的控件
	private MenuButton mBtn_exit; // 退出按钮
	private MenuButton mBtn_snapShot; // 截图按钮
	private MenuButton mBtn_record; // 录像按钮
	private MenuButton mBtn_playback; // 查看本地文件按钮
	private MenuButton mBtn_SDRecord; // 远程SD卡录像按钮
	private Button mBtn_lock; // 锁定按钮
	private MenuButton mBtn_minitrim; // 微调按钮
	private MenuButton mBtn_setting; // 设置按钮
	// mLayoutView_rocker中的控件
	private RockerView mRocker_left; // 左侧摇杆
	private RockerView mRocker_right; // 右侧摇杆

	// mLayoutView_trim中的控件
	private TrimView mTrimView1;
	private TrimView mTrimView2;
	private TrimView mTrimView3;

	private boolean isSDRecording = false; // 正在录像
	private boolean isRecording = false; // 正在本地录像
	private boolean isStartRecord = false; // 已启动本地录像
	private long mSDRecord_startTime = 0; // 录像开始时间
	private long mRecord_startTime = 0; // 本地录像开始时间

	private MyToast mToast; // 定义Toast

	public ControlMsg mCtlMsg; // 飞控控制数据

	private boolean haveSDcard = false; // 摄像头模块有无SDcard

	private ListenRecordThread listenRecordThread = new ListenRecordThread(); // 监听录像线程
	private HttpThread bridgeThread = null; // HTTP桥接线程

	private String mAuthcode; // 控制权限的数据

	private boolean isHideAllView = false;
	private boolean isCountDown_HideAllView = true; // 可以倒计时
	private static final int s_TotalTime_HideAllView = 60;
	private int mTime_HideAllView = s_TotalTime_HideAllView;
	private int mLanguage;
	private boolean isOpenControl = true;
	private int mControlMode; // 控制模式，0表示左手，1表示右手
	private int mFlipImage;
	private int mStroageLocaltion;
	InnerBoradcastReceiver receiver;
	private PlayVoice mPlayVoice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate=======================");
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_play);
		new HttpThread(HTTP_720p_1, HTTP_handler).start();
		try {
			Thread.currentThread().sleep(3500); // 主线程休眠，以给720p线程拿到返回值的时间
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mWindow_width = dm.widthPixels;
		mWindow_height = dm.heightPixels;
		Log.i("mWindow_width_height", "mWindow_width=" + mWindow_width + "\n"
				+ "mWindow_height=" + mWindow_height);

		mVideoView1 = (RtspVideoView) findViewById(R.id.rtspVideoView1);
		mVideoView2 = (RtspVideoView) findViewById(R.id.rtspVideoView2);
		mVideoView2.setVisibility(View.INVISIBLE);

		if (isWifiConnected()) {
			bridgeThread = new HttpThread(HTTP_START, HTTP_handler);
			bridgeThread.start();

		}
		mVideoView1.setVideo(rtspUrl_nHD1, null);
		Log.e("test", "setVideo.............. ");
		mVideoView2.setVideo(rtspUrl_nHD2, null);

		layout = (RelativeLayout) findViewById(R.id.layout);

		lp1 = mVideoView1.getLayoutParams();
		lp1.width = mWindow_width;
		lp1.height = mWindow_height;

		lp2 = mVideoView2.getLayoutParams();
		lp2.width = mWindow_width / 4;
		lp2.height = mWindow_height / 4;

		mVideoView1.setLayoutParams(lp1);
		mVideoView2.setLayoutParams(lp2);
		// mVideoView2.setOnTouchListener(new MyOnTouchListener());

		mVideoView1.setZOrderOnTop(false);
		mVideoView2.setZOrderOnTop(true);

		mToast = new MyToast(this);
		mPlayVoice = new PlayVoice(this);

		/********* layout_menu 中的控件 ***********/
		mLayoutView_menu = (RelativeLayout) findViewById(R.id.layoutView_menu);
		mBtn_exit = (MenuButton) findViewById(R.id.btn_exit);
		mBtn_snapShot = (MenuButton) findViewById(R.id.btn_snapshot);
		mBtn_record = (MenuButton) findViewById(R.id.btn_record);
		mBtn_playback = (MenuButton) findViewById(R.id.btn_playback);
		mBtn_SDRecord = (MenuButton) findViewById(R.id.btn_sdRecord);
		mBtn_lock = (Button) findViewById(R.id.btn_lock);
		mBtn_minitrim = (MenuButton) findViewById(R.id.btn_minitrim);
		mBtn_setting = (MenuButton) findViewById(R.id.btn_setting);
		total = (ImageButton) findViewById(R.id.totalMenu);
		model1 = (ImageButton) findViewById(R.id.model1);
		model2 = (ImageButton) findViewById(R.id.model2);
		model3 = (ImageButton) findViewById(R.id.model3);
		model4 = (ImageButton) findViewById(R.id.model4);
		total.setOnClickListener(this);
		model1.setOnClickListener(this);
		model2.setOnClickListener(this);
		model3.setOnClickListener(this);
		model4.setOnClickListener(this);

		// 设置监听
		mBtn_exit.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_snapShot.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_record.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_playback.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_SDRecord.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_minitrim.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_setting.setMenuOnClickListener(new MyMenuOnClickListener());
		
		mBtn_lock.setOnClickListener(this);

		changeRecordLocaltion();// 选择存储模式（SD卡存储、手机存储）

		mBtn_exit.setVisibility(View.INVISIBLE);
		mBtn_snapShot.setVisibility(View.INVISIBLE);
		mBtn_record.setVisibility(View.INVISIBLE);
		mBtn_SDRecord.setVisibility(View.INVISIBLE);
		mBtn_playback.setVisibility(View.INVISIBLE);
		mBtn_minitrim.setVisibility(View.INVISIBLE);
		mBtn_setting.setVisibility(View.INVISIBLE);
		model1.setVisibility(View.INVISIBLE);
		model2.setVisibility(View.INVISIBLE);
		model3.setVisibility(View.INVISIBLE);
		model4.setVisibility(View.INVISIBLE);

		/********* layout_rocker 中的控件 ***********/
		mLayoutView_rocker = (RelativeLayout) findViewById(R.id.layoutView_rocker);
		mRocker_left = (RockerView) findViewById(R.id.rocker_left);
		mRocker_right = (RockerView) findViewById(R.id.rocker_right);
		mRocker_left.setVisibility(View.INVISIBLE);
		mRocker_right.setVisibility(View.INVISIBLE);
		// mRocker_left.setRockerChangeListener(new MyRockerChangeListener());
		// mRocker_right.setRockerChangeListener(new MyRockerChangeListener());

		/********* layout_trim 中的控件 ***********/
		mLayoutView_trim = (RelativeLayout) findViewById(R.id.layoutView_trim);
		mTrimView1 = (TrimView) findViewById(R.id.trimview1);
		mTrimView2 = (TrimView) findViewById(R.id.trimview2);
		mTrimView3 = (TrimView) findViewById(R.id.trimview3);

		mTrimView1.setVisibility(View.INVISIBLE);
		mTrimView2.setVisibility(View.INVISIBLE);
		mTrimView3.setVisibility(View.INVISIBLE);

		mTrimView1.setTrimChangeListener(new MyTrimChangeListener());
		mTrimView2.setTrimChangeListener(new MyTrimChangeListener());
		mTrimView3.setTrimChangeListener(new MyTrimChangeListener());
		mTrimView1.setPlayVoice(mPlayVoice);
		mTrimView2.setPlayVoice(mPlayVoice);
		mTrimView3.setPlayVoice(mPlayVoice);

		mLanguage = AppUtil.s_Language;
		mControlMode = AppUtil.s_ControlMode;
		mFlipImage = AppUtil.s_FlipImage;
		mStroageLocaltion = AppUtil.s_StroageLocation;

		if (mFlipImage == 1) {
			mFlipImage = 3;
		} else {
			mFlipImage = 0;
		}

		if (mFlipImage == 3) {
			mFlipImage = 1;
		}

		listenRecordThread.start(); // 开启录像监听线程

		mCtlMsg = ControlMsg.getInstance();

		mVideoView1.rotate(mFlipImage == 0); // 0表示影像是正的
												// //////////////////////////
		mTrimView1.setProgress(AppUtil.trim1);
		mTrimView2.setProgress(AppUtil.trim2);
		mTrimView3.setProgress(AppUtil.trim3);
		mCtlMsg.setSpeedLimit(AppUtil.s_SpeedChoose);
		mCtlMsg.setNoHead(AppUtil.s_NoHead);

		control_lockAndHighLimit(0); // 开启定高
		changeTextLanguage(mLanguage);

		mLayoutView_screen = (RelativeLayout) findViewById(R.id.layoutView_screen);
		mLayoutView_screen.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mTime_HideAllView = s_TotalTime_HideAllView;

				int w = RockerView.rockWidth;
				int h = RockerView.rockHeight;

				RockerView rocker1 = null;
				RockerView rocker2 = null;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (isHideAllView) {
						isHideAllView = false;
						setViewVisibility(mLayoutView_menu, View.VISIBLE);
						setViewVisibility(mLayoutView_trim, View.VISIBLE);
					}

					Log.i("MotionEvent", "PlayActivity--DOWN");
					if (mControlMode == 0) {

						if (event.getX() > 0.1f * mWindow_width
								&& event.getX() < 0.45f * mWindow_width
								&& event.getY() > 0.15f * mWindow_height
								&& event.getY() < 0.85f * mWindow_height) {
							mRocker_left.setVisibility(View.VISIBLE);
							Log.i("MotionEvent", "PlayActivity--mRocker_left");
							p = (RelativeLayout.LayoutParams) mRocker_left
									.getLayoutParams();
							p.leftMargin = (int) event.getX() - w / 2;
							p.topMargin = (int) event.getY() - h / 2;
							mRocker_left.setLayoutParams(p);
							mRocker_left
									.setRockerChangeListener(new MyRockerChangeListener());
							// mRocker_right.setRockerChangeListener(null);
							return false;
						} else if (event.getX() > 0.55f * mWindow_width
								&& event.getX() < 0.95f * mWindow_width
								&& event.getY() > 0.15f * mWindow_height
								&& event.getY() < 0.85f * mWindow_height) {
							mRocker_right.setVisibility(View.VISIBLE);
							Log.i("MotionEvent", "PlayActivity--mRocker_right");
							p = (RelativeLayout.LayoutParams) mRocker_right
									.getLayoutParams();
							// p.height = mWindow_height/2;
							// p.width = mWindow_width/2;
							p.leftMargin = (int) event.getX() - w / 2;
							p.topMargin = (int) event.getY() - h / 2;
							mRocker_right.setLayoutParams(p);
							mRocker_right
									.setRockerChangeListener(new MyRockerChangeListener());
							// mRocker_left.setRockerChangeListener(null);

							return false;
						}
					} else if (mControlMode == 1) {
						if (event.getX() > 0.1f * mWindow_width
								&& event.getX() < 0.45f * mWindow_width
								&& event.getY() > 0.15f * mWindow_height
								&& event.getY() < 0.85f * mWindow_height) {
							mRocker_right.setVisibility(View.VISIBLE);
							Log.i("MotionEvent", "PlayActivity--mRocker_left");
							p = (RelativeLayout.LayoutParams) mRocker_right
									.getLayoutParams();
							p.leftMargin = (int) event.getX() - w / 2;
							p.topMargin = (int) event.getY() - h / 2;
							mRocker_right.setLayoutParams(p);
							mRocker_right
									.setRockerChangeListener(new MyRockerChangeListener());
							// mRocker_right.setRockerChangeListener(null);
							return false;
						} else if (event.getX() > 0.55f * mWindow_width
								&& event.getX() < 0.95f * mWindow_width
								&& event.getY() > 0.15f * mWindow_height
								&& event.getY() < 0.85f * mWindow_height) {
							mRocker_left.setVisibility(View.VISIBLE);
							Log.i("MotionEvent", "PlayActivity--mRocker_right");
							p = (RelativeLayout.LayoutParams) mRocker_left
									.getLayoutParams();
							// p.height = mWindow_height/2;
							// p.width = mWindow_width/2;
							p.leftMargin = (int) event.getX() - w / 2;
							p.topMargin = (int) event.getY() - h / 2;
							mRocker_left.setLayoutParams(p);
							mRocker_left
									.setRockerChangeListener(new MyRockerChangeListener());
							// mRocker_left.setRockerChangeListener(null);

							return false;
						}
					}
					break;

				}

				if (isHideAllView) {
					if (!mBtn_minitrim.isChecked()) {
					}
					return true; // 避免显现其他View时的触摸操作
				} else {
					return false;
				}

			}
		});
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		Log.i("MotionEvent", "PlayActivity dispatch");
		return super.dispatchTouchEvent(ev);
	}

	// @Override
	// public void onWindowFocusChanged(boolean hasFocus) {
	// // TODO Auto-generated method stub
	// super.onWindowFocusChanged(hasFocus);
	// changeControlMode();
	// }

	// /**
	// * 左右手切换及关闭控制
	// */
	// private void changeControlMode() {
	//
	// if (!isOpenControl) {
	// setViewVisibility(mLayoutView_trim, View.INVISIBLE);
	// setViewVisibility(mLayoutView_rocker, View.INVISIBLE);
	// return;
	// } else {
	// setViewVisibility(mLayoutView_trim, View.VISIBLE);
	// setViewVisibility(mLayoutView_rocker, View.VISIBLE);
	// }
	//
	// int ll = mRocker_left.getLeft();
	// int lt = mRocker_left.getTop();
	// int lr = mRocker_left.getRight();
	// int lb = mRocker_left.getBottom();
	//
	// int rl = mRocker_right.getLeft();
	// int rt = mRocker_right.getTop();
	// int rr = mRocker_right.getRight();
	// int rb = mRocker_right.getBottom();
	//
	// boolean isLeftHand = mControlMode == 0;
	// if ((isLeftHand && ll > rl) || (!isLeftHand && ll < rl)) {
	// // 左手模式且左摇杆起始坐标比右边大
	// // 右手模式且左摇杆起始坐标比右边小
	// mRocker_left.layout(rl, rt, rr, rb);
	// mRocker_right.layout(ll, lt, lr, lb);
	// }
	//
	// ll = mTrimView1.getLeft();
	// lt = mTrimView1.getTop();
	// lr = mTrimView1.getRight();
	// lb = mTrimView1.getBottom();
	//
	// rl = mTrimView2.getLeft();
	// rt = mTrimView2.getTop();
	// rr = mTrimView2.getRight();
	// rb = mTrimView2.getBottom();
	//
	// if ((isLeftHand && ll > rl) || (!isLeftHand && ll < rl)) {
	// // 左手模式且左摇杆起始坐标比右边大
	// // 右手模式且左摇杆起始坐标比右边小
	// mTrimView1.layout(rl, rt, rr, rb);
	// mTrimView2.layout(ll, lt, lr, lb);
	// }
	//
	// ll = mTrimView0.getLeft();
	// lt = mTrimView0.getTop();
	// lr = mTrimView0.getRight();
	// lb = mTrimView0.getBottom();
	//
	// rl = mTrimView3.getLeft();
	// rt = mTrimView3.getTop();
	// rr = mTrimView3.getRight();
	// rb = mTrimView3.getBottom();
	//
	// if ((isLeftHand && ll > rl) || (!isLeftHand && ll < rl)) {
	// // 左手模式且左摇杆起始坐标比右边大
	// // 右手模式且左摇杆起始坐标比右边小
	// mTrimView0.layout(rl, rt, rr, rb);
	// mTrimView3.layout(ll, lt, lr, lb);
	// }
	// }

	private void changeRecordLocaltion() {
		if (mStroageLocaltion == 0) {
			setViewVisibility(mBtn_SDRecord, View.VISIBLE);
			setViewVisibility(mBtn_record, View.GONE);
		} else {
			setViewVisibility(mBtn_SDRecord, View.GONE);
			setViewVisibility(mBtn_record, View.VISIBLE);
		}

	}

	/**
	 * 切换语言
	 * 
	 * @param language
	 */
	protected void changeTextLanguage(int mLanguage) {
		int i = mLanguage;

		mBtn_exit.setText(AppUtil.text_menu[0][i]);
		mBtn_snapShot.setText(AppUtil.text_menu[1][i]);
		mBtn_record.setText(AppUtil.text_menu[2][i]);
		mBtn_SDRecord.setText(AppUtil.text_menu[3][i]);
		mBtn_playback.setText(AppUtil.text_menu[4][i]);
		mBtn_minitrim.setText(AppUtil.text_menu[12][i]);
		mBtn_setting.setText(AppUtil.text_menu[10][i]);

//		mBtn_lock.setText(AppUtil.text_menu[11][i]);

	}

	private void control_lockAndHighLimit(int mode) {
		if (mode == 0) {
			setViewVisibility(mBtn_lock, View.VISIBLE); // 显示锁定按钮
			mRocker_left.setLocked_y(true, 2); // 锁定y轴, 摇杆至于中间
			mRocker_left.setRecover_y(true);
			mCtlMsg.setStartFly(0);
		} else if (mode == 1) {
			setViewVisibility(mBtn_lock, View.INVISIBLE); // 隐藏定高按钮
			mRocker_left.setLocked_y(false, 1); // 解除y轴, 摇杆至于底部
			mRocker_left.setRecover_y(false);
			mCtlMsg.setStartFly(1);
			mHandler.sendEmptyMessageDelayed(5, 1000);
		} else if (mode == 2) {
			setViewVisibility(mBtn_lock, View.INVISIBLE); // 隐藏定高按钮
			mRocker_left.setLocked_y(false, 0); // 解除y轴， 摇杆位置不变
			// mCtlMsg.setStartFly(1);
			// mHandler.sendEmptyMessageDelayed(5, 1000);
			// mCtlMsg.setHighLimit(0); // 关闭定高
		}

	}

	private void setViewVisibility(View view, int visibility) {
		if (view.getVisibility() != visibility) {
			view.setVisibility(visibility);
		}
	}

	private boolean isWifiConnected() {

		boolean flag = false;
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String ssid = wifiInfo.getSSID();
		Log.d("JNI", "this is wificheck" + ssid);
		if (ssid.startsWith("Skycam", 1) || ssid.startsWith("TANK", 1)) {
			flag = true;
		} else {
			flag = false;
		}

		return flag;
	}

	class MyTrimChangeListener implements TrimView.TrimChangeListener {

		@Override
		public void report(View v, int progress) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.trimview1:
				Log.i(TAG, "trimview1: " + progress);
				mCtlMsg.setYaw_trim(progress * 2);
				break;

			case R.id.trimview2:
				Log.i(TAG, "trimview2: " + progress);
				mCtlMsg.setRoll_trim(progress * 2);
				break;

			case R.id.trimview3:
				Log.i(TAG, "trimview3: " + progress);
				mCtlMsg.setPitch_trim(progress * 2);
				break;

			default:
				break;
			}
		}
	}

	/**
	 * 监听摇杆动作的类
	 */
	class MyRockerChangeListener implements RockerView.RockerChangeListener {

		@Override
		public void report(View v, float x, float y) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.rocker_left) {
				if (x > 0.15 || x < -0.15) {
					mCtlMsg.setYaw(x); // 设置方向舵
				} else {
					mCtlMsg.setYaw(0);
				}

				if (y > 0.15 || y < -0.15) {
					mCtlMsg.setThrottle(y); // 设置油门
				} else {
					mCtlMsg.setThrottle(0);
				}

			} else if (v.getId() == R.id.rocker_right) {
				if (x > 0.15 || x < -0.15) {
					mCtlMsg.setRoll(x); // 设置副翼
				} else {
					mCtlMsg.setRoll(0);
				}

				if (y > 0.15 || y < -0.15) {
					mCtlMsg.setPitch(y); // 设置升降舵
				} else {
					mCtlMsg.setPitch(0);
				}

			}
			mTime_HideAllView = s_TotalTime_HideAllView;
			isHideAllView = false;
		}
	}

	/**
	 * 　录像监听线程
	 */
	class ListenRecordThread extends Thread {

		public boolean isRun;
		int sleepTime = 1000;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isRun = true;
			while (isRun) {
				boolean mVideoView = false;
				if (model_choose == 1 || model_choose == 3) {
					mVideoView = mVideoView1.videoIsRecording();
				} else if (model_choose == 2 || model_choose == 4) {
					mVideoView = mVideoView2.videoIsRecording();
				}
				if (mVideoView == true) {
					mHandler.sendEmptyMessage(0);
					sleepTime = 1000;
				} else {
					mHandler.sendEmptyMessage(1);
					mRecord_startTime = System.currentTimeMillis(); // 记录本地录像的启动时间
					sleepTime = 1000;
				}
				try {
					Thread.sleep(sleepTime);
					if (isCountDown_HideAllView) {
						mTime_HideAllView--;
					}
					// Log.i(TAG, "Time:" + mTime_HideAllView);
					if (mTime_HideAllView <= 0) {
						mTime_HideAllView = 0;
						isHideAllView = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 　用于处理录像信息的Handler 和 清除mCtlMsg byte[5]
	 */
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // 正在本地录像
				isRecording = true;
				setRecordTime(mBtn_record, mRecord_startTime);
				break;

			case 1: // 停止本地录像
				isRecording = false;
				mBtn_record.setText(AppUtil.text_menu[2][mLanguage]);
				break;
			case 2:
				mCtlMsg.setBalance(0);
				Log.i("mHandler", "clear Balance");
				break;
			case 3:
				mCtlMsg.setStop(0);
				Log.i("mHandler", "clear stop");
				break;
			case 5:
				mCtlMsg.setStartFly(0);
				Log.i("mHandler", "clear StartFly");
				break;

			default:
				break;
			}

			if (isSDRecording) { // 正在录像
				setRecordTime(mBtn_SDRecord, mSDRecord_startTime); // 修改mTv_record
			} else {
				mSDRecord_startTime = 0;
			}

			if (isHideAllView) {
				setViewVisibility(mLayoutView_menu, View.INVISIBLE);
				setViewVisibility(mLayoutView_trim, View.INVISIBLE);
			}
		}

		/**
		 * 设置录像时间TextView
		 * 
		 * @param tv
		 *            需要设置的TextView
		 * @param startTime
		 *            录制开始时间
		 */
		private void setRecordTime(MenuButton btn, long startTime) {
			int time = (int) ((System.currentTimeMillis() - startTime) / 1000); // 总时间，单位s
			String sTime = String.format("%02d", time / 60) + ":"
					+ String.format("%02d", time % 60);
			btn.setText(1, sTime);
		}
	};
	Handler videoEventHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			RtspVideoView.HandlerMsg handlerMsg = (RtspVideoView.HandlerMsg) msg.obj;
			boolean isSuccess = handlerMsg.isSuccess;
			switch (msg.what) {
			case RtspVideoView.START_RTSP:
				if (!isSuccess) {
					mToast.showToast("START_RTSP failed");
				}
				break;
			case RtspVideoView.SNAPSHOT:
				if (!isSuccess) {
					mToast.showToast(AppUtil.text_toast2[mLanguage]);
				} else {
					mToast.showToast(AppUtil.text_toast1[mLanguage] + "\""
							+ handlerMsg.msg + "\"");
				}
				break;

			case RtspVideoView.RECORD:
				if (!isSuccess) {
					mToast.showToast(AppUtil.text_toast8[mLanguage]);
				} else {
					mToast.showToast(AppUtil.text_toast1[mLanguage] + "\""
							+ handlerMsg.msg + "\"");
				}
				break;
			case RtspVideoView.STOP_RTSP:
				if (!isSuccess) {
					Log.d(TAG, "stop_stsp success");
				}
			default:
				break;
			}
		}
	};

	// class MyOnTouchListener implements OnTouchListener {
	//
	// GestureDetector gestureDetector;
	//
	// public MyOnTouchListener() {
	// // TODO Auto-generated constructor stub
	// gestureDetector = new GestureDetector(PlayActivity.this,
	// new OnGestureListener() {
	//
	// float distanceX_cnt = 0, distanceY_cnt = 0;
	//
	// @Override
	// public boolean onSingleTapUp(MotionEvent e) {
	// // TODO Auto-generated method stub
	//
	// mVideoView1.videoPause();
	// mVideoView2.videoPause();
	// layout.removeAllViews();
	//
	// mVideoView1.setLayoutParams(lp2);
	// mVideoView2.setLayoutParams(lp1);
	//
	// mVideoView1.setZOrderOnTop(true);
	// mVideoView2.setZOrderOnTop(false);
	// mVideoView1.setVisibility(View.VISIBLE);
	// mVideoView2.setVisibility(View.VISIBLE);
	//
	// layout.addView(mVideoView1);
	// layout.addView(mVideoView2);
	// // mVideoView2
	// // .setOnTouchListener(new MyOnTouchListener1());
	// mVideoView1.videoResume();
	// mVideoView2.videoResume();
	// return true;
	// }
	//
	// @Override
	// public void onShowPress(MotionEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public boolean onScroll(MotionEvent e1, MotionEvent e2,
	// float distanceX, float distanceY) {
	// // TODO Auto-generated method stub
	// distanceX_cnt += distanceX;
	// distanceY_cnt += distanceY;
	// if (Math.abs(distanceX_cnt) > 24.0f
	// || Math.abs(distanceY_cnt) > 24.0f) {
	// int l = mVideoView2.getLeft();
	// int t = mVideoView2.getTop();
	// int r = mVideoView2.getRight();
	// int b = mVideoView2.getBottom();
	// l = (int) (l - distanceX_cnt);
	// t = (int) (t - distanceY_cnt);
	// r = (int) (r - distanceX_cnt);
	// b = (int) (b - distanceY_cnt);
	// mVideoView2.layout(l, t, r, b);
	// distanceX_cnt = 0;
	// distanceY_cnt = 0;
	// }
	// return true;
	// }
	//
	// @Override
	// public void onLongPress(MotionEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public boolean onFling(MotionEvent e1, MotionEvent e2,
	// float velocityX, float velocityY) {
	// // TODO Auto-generated method stub
	// // mVideoView2.videoResume();
	//
	// return false;
	// }
	//
	// @Override
	// public boolean onDown(MotionEvent e) {
	// // TODO Auto-generated method stub
	// // mVideoView2.videoPause();
	//
	// return false;
	// }
	// });
	// }

	// @Override
	// public boolean onTouch(View v, MotionEvent event) {
	// // TODO Auto-generated method stub
	// gestureDetector.onTouchEvent(event);
	//
	// return true;
	// }
	// }

	class InnerBoradcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			/*
			 * Intent intent1=new Intent(); intent.setAction("STARTHOME");
			 * sendBroadcast(intent1);
			 */
			finish();
			Log.d("JNI", "this is PlayActivity");
		}
	}

	class MyMenuOnClickListener implements MenuButtonOnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			switch (v.getId()) {
			// 退出
			case R.id.btn_exit:
				finish();
				onDestroy();
				break;
			// 截图
			case R.id.btn_snapshot:
				mBtn_snapShot.setEnabled(false);
				RtspVideoView mVideoView = mVideoView1;
				if (model_choose == 1 || model_choose == 3) {
					mVideoView = mVideoView1;
				} else if (model_choose == 2 || model_choose == 4) {
					mVideoView = mVideoView2;
				}

				mVideoView.takeSnapShot(AppUtil.getImageName());
				// if (mVideoView.takeSnapShot(name)) {
				// mToast.showToast(AppUtil.text_toast1[mLanguage] + name);
				// } else {
				// mToast.showToast(AppUtil.text_toast2[mLanguage]);
				// }
				mBtn_snapShot.setEnabled(true);
				break;
			// 录像
			case R.id.btn_record:
				if (model_choose == 1 || model_choose == 3) {
					try {
						if (mVideoView1.isReady()) { // ///////////
							if (mVideoView1.videoIsRecording()) { // ///////////
								isStartRecord = false;
								mVideoView1.videoRecordStop(); // 停止录像
																// /////////////
								Log.e("aa", "停止录像 /1");
							} else {
								Log.e("aa", "启动录像 /1");
								mVideoView1.videoRecordStart(AppUtil
										.getVideoName()); // 启动录像 /////////////
							}
						} else {
							if (mBtn_record.isChecked()) {
								mBtn_record.setChecked(false);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (model_choose == 2 || model_choose == 4) {
					try {
						if (mVideoView2.isReady()) { // ///////////
							if (mVideoView2.videoIsRecording()) { // ///////////
								isStartRecord = false;
								mVideoView2.videoRecordStop(); // 停止录像
																// /////////////
								Log.e("aa", "停止录像 /22");
							} else {
								Log.e("aa", "启动录像 /22");
								mVideoView2.videoRecordStart(AppUtil
										.getVideoName()); // 启动录像 /////////////
							}
						} else {
							if (mBtn_record.isChecked()) {
								mBtn_record.setChecked(false);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			// SD录像
			case R.id.btn_sdRecord:
				if (haveSDcard) {
					mBtn_SDRecord.setEnabled(false);

					if (mBtn_SDRecord.isChecked()) {
						new HttpThread(HTTP_START_RECORD, HTTP_handler).start();
					} else {
						new HttpThread(HTTP_STOP_RECORD, HTTP_handler).start();
					}
				} else {
					mToast.showToast(AppUtil.text_toast10[mLanguage]);
					if (mBtn_SDRecord.isChecked()) {
						mBtn_SDRecord.setChecked(false);
					}
				}
				break;
			// 查看截图录像文件
			case R.id.btn_playback:
				if (!isSDRecording && !isRecording) {
					Intent intent = new Intent(PlayActivity.this,
							FileActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				} else {
					mToast.showToast(AppUtil.text_toast3[mLanguage]);
				}
				break;

			// 微调按键
			case R.id.btn_minitrim:
				if (mBtn_minitrim.isChecked()) {
					setViewVisibility(mTrimView3, View.VISIBLE);
					setViewVisibility(mTrimView1, View.VISIBLE);
					setViewVisibility(mTrimView2, View.VISIBLE);
				} else {
					setViewVisibility(mTrimView1, View.INVISIBLE);
					setViewVisibility(mTrimView2, View.INVISIBLE);
					setViewVisibility(mTrimView3, View.INVISIBLE);
				}
				break;

			// 设置按键
			case R.id.btn_setting:
				isCountDown_HideAllView = false; // 关闭隐藏所有控制View倒计时

				View layout_setup = LayoutInflater.from(PlayActivity.this)
						.inflate(R.layout.dialog_setup, null);
				ListView listView_setup = (ListView) layout_setup
						.findViewById(R.id.listview_setup);
				final boolean isAddBalance = mCtlMsg.getThrottle() == 0;
				final SetupListViewAdapter setupListViewAdapter = new SetupListViewAdapter(
						PlayActivity.this, mLanguage, isAddBalance, new int[] {
								mCtlMsg.getHighLimit(), mCtlMsg.getNoHead(),
								mFlipImage, mControlMode,
								// mLanguage,
								mStroageLocaltion });

				listView_setup.setAdapter(setupListViewAdapter);

				final AlertDialog setupDialog = new AlertDialog.Builder(
						PlayActivity.this,
						AlertDialog.THEME_DEVICE_DEFAULT_DARK)
						.setTitle(AppUtil.text_dialog_setting_title[mLanguage])
						.setView(layout_setup)
						.setPositiveButton(
								AppUtil.text_dialog_setting_confirm[mLanguage],
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										int len = setupListViewAdapter.arrayList
												.size();
										int[] nums = new int[len];
										boolean[] isChangeds = new boolean[len];
										for (int i = 0; i < len; i++) {
											SetupListViewAdapter.SetupItem setupItem = setupListViewAdapter.arrayList
													.get(i);
											Log.i(TAG, setupItem.toString());
											nums[i] = setupItem.getSetupNum();
											isChangeds[i] = setupItem
													.isChanged();
										}

										if (isChangeds[len - 1]) {
											mStroageLocaltion = nums[len - 1];
											changeRecordLocaltion();
										}
										// if (isChangeds[len - 2]) {
										// mLanguage = nums[len - 2];
										// changeTextLanguage(mLanguage);
										// }
										if (isChangeds[len - 2]) {
											mControlMode = nums[len - 2];
											changeControlMode();
										}
										if (isChangeds[len - 3]) {
											mFlipImage = nums[len - 3];
											if (mFlipImage == 1) {
												Log.e("mFlipImage", "1--3");
												mFlipImage = 3;

											}
											new HttpThread(
													HttpThread.HTTP_FLIP_MIRROR_IMAGE,
													mFlipImage, HTTP_handler)
													.start();
											// mVideoView1.rotate(mFlipImage ==
											// 1);

											if (mFlipImage == 3) {
												Log.e("mFlipImage", "3--1");
												mFlipImage = 1;
											}
											// new HttpThread(
											// HttpThread.HTTP_FLIP_MIRROR_IMAGE,
											// mFlipImage, HTTP_handler)
											// .start();
											// mVideoView1.rotate(mFlipImage ==
											// 0);

										}
										if (isChangeds[len - 4]) {
											mCtlMsg.setNoHead(nums[len - 4]);
										}

										if (isChangeds[len - 5]) {
											mCtlMsg.setHighLimit(nums[len - 5]);
											if (mCtlMsg.getHighLimit() == 0) {
												control_lockAndHighLimit(1);
											} else {
												control_lockAndHighLimit(0);
											}
										}
										writeSetupParameter();
									}
								})
						.setNegativeButton(
								AppUtil.text_dialog_setting_cancel[mLanguage],
								null).show();
				// 对setupDialog设置dismiss监听，当setupDialog消失时进入
				setupDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
						mTime_HideAllView = s_TotalTime_HideAllView;
					}
				});
				listView_setup
						.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								// TODO Auto-generated method stub
								if (isAddBalance && position == 0) {
									mCtlMsg.setBalance(1);
									mHandler.sendEmptyMessageDelayed(2, 1000);
									setupDialog.dismiss();
								}
								if (isAddBalance && position == 1
										|| !isAddBalance && position == 0) {
									mCtlMsg.setStop(1);
									mHandler.sendEmptyMessageDelayed(3, 1000);
									setupDialog.dismiss();
								}
							}
						});
				break;
//			// 定高
//			case R.id.btn_lock:
//				control_lockAndHighLimit(2);
//				break;
			default:
				break;
			}
		}

		protected void control_lockAndHighLimit(int mode) {
			if (mode == 0) {
				setViewVisibility(mBtn_lock, View.VISIBLE); // 显示锁定按钮
				mRocker_left.setLocked_y(true, 2); // 锁定y轴, 摇杆至于中间
				mRocker_left.setRecover_y(true);
				mCtlMsg.setStartFly(0);
			} else if (mode == 1) {
				setViewVisibility(mBtn_lock, View.INVISIBLE); // 隐藏定高按钮
				mRocker_left.setLocked_y(false, 1); // 解除y轴, 摇杆至于底部
				mRocker_left.setRecover_y(false);
				mCtlMsg.setStartFly(1);
				mHandler.sendEmptyMessageDelayed(5, 1000);
			} else if (mode == 2) {
				setViewVisibility(mBtn_lock, View.INVISIBLE); // 隐藏定高按钮
				mRocker_left.setLocked_y(false, 0); // 解除y轴， 摇杆位置不变
				// mCtlMsg.setStartFly(1);
				// mHandler.sendEmptyMessageDelayed(5, 1000);
				// mCtlMsg.setHighLimit(0); // 关闭定高
			}

		}

		/**
		 * 左右手切换及关闭控制
		 */
		protected void changeControlMode() {
			if (!isOpenControl) {
				setViewVisibility(mLayoutView_trim, View.INVISIBLE);
				setViewVisibility(mLayoutView_rocker, View.INVISIBLE);
				return;
			} else {
				setViewVisibility(mLayoutView_trim, View.VISIBLE);
				setViewVisibility(mLayoutView_rocker, View.VISIBLE);
			}

			int ll = mRocker_left.getLeft();
			int lt = mRocker_left.getTop();
			int lr = mRocker_left.getRight();
			int lb = mRocker_left.getBottom();

			int rl = mRocker_right.getLeft();
			int rt = mRocker_right.getTop();
			int rr = mRocker_right.getRight();
			int rb = mRocker_right.getBottom();

			boolean isLeftHand = mControlMode == 0;
			if ((isLeftHand && ll > rl) || (!isLeftHand && ll < rl)) {
				// 左手模式且左摇杆起始坐标比右边大
				// 右手模式且左摇杆起始坐标比右边小
				mRocker_left.layout(rl, rt, rr, rb);
				mRocker_right.layout(ll, lt, lr, lb);
			}

			ll = mTrimView1.getLeft();
			lt = mTrimView1.getTop();
			lr = mTrimView1.getRight();
			lb = mTrimView1.getBottom();

			rl = mTrimView2.getLeft();
			rt = mTrimView2.getTop();
			rr = mTrimView2.getRight();
			rb = mTrimView2.getBottom();

			if ((isLeftHand && ll > rl) || (!isLeftHand && ll < rl)) {
				// 左手模式且左摇杆起始坐标比右边大
				// 右手模式且左摇杆起始坐标比右边小
				mTrimView1.layout(rl, rt, rr, rb);
				mTrimView2.layout(ll, lt, lr, lb);
			}

			rl = mTrimView3.getLeft();
			rt = mTrimView3.getTop();
			rr = mTrimView3.getRight();
			rb = mTrimView3.getBottom();

			if ((isLeftHand && ll > rl) || (!isLeftHand && ll < rl)) {
				// 左手模式且左摇杆起始坐标比右边大
				// 右手模式且左摇杆起始坐标比右边小
				mTrimView3.layout(rl, rt, rr, rb);
			}

		}

		protected void changeRecordLocaltion() {
			if (mStroageLocaltion == 0) {
				setViewVisibility(mBtn_SDRecord, View.VISIBLE);
				setViewVisibility(mBtn_record, View.GONE);
			} else {
				setViewVisibility(mBtn_SDRecord, View.GONE);
				setViewVisibility(mBtn_record, View.VISIBLE);
			}

		}
	}

	/**
	 * @author Administrator 处理HTTP反馈信息Handler
	 */
	public Handler HTTP_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int value = (Integer) msg.obj;

			if (msg.what < 0) {
				return;
			}

			switch (msg.what) {

			case HTTP_GET_PRIVILEGE: // 获取权限返回的消息
				if (value == 0) {
					Bundle bundle = msg.getData();
					mAuthcode = bundle.getString("authcode");
					Log.w(TAG, "mAuthcode" + mAuthcode);
				} else {
					mToast.showToast(AppUtil.text_toast4[mLanguage]);
				}
				break;
			case HTTP_RELEASE_PRIVILEGE:// 释放权限返回的消息
				if (value != 0) {
					mToast.showToast(AppUtil.text_toast5[mLanguage]);
				}
				break;

			case HTTP_SET_TIME: // 设置时间返回的消息
				if (value != 0) {
					mToast.showToast(AppUtil.text_toast6[mLanguage]);
				}
				break;

			case HTTP_CHECK_STROAGE: // 检测SDcard返回的消息
				if (value == 0) {
					haveSDcard = false;
				} else if (value == 1) {
					haveSDcard = true;
				}
				break;

			case HTTP_BRIDGE: // 发送控制数据返回的消息
				Bundle bundle = msg.getData();
				String send = bundle.getString("send");
				break;
			// case HTTP_TAKEPHOTO: // 截图返回的消息
			// if (value == 0) {
			// mToast.showToast("拍照成功");
			// } else {
			// mToast.showToast("拍照失败");
			// }
			// mBtn_takePhoto.setEnabled(true);
			// break;

			case HTTP_START_RECORD: // 启动录像返回的消息
				if (value == 0) {
					mSDRecord_startTime = System.currentTimeMillis();
					mToast.showToast(AppUtil.text_toast7[mLanguage]);
					isSDRecording = true;
				} else {
					mToast.showToast(AppUtil.text_toast8[mLanguage]);
					mBtn_SDRecord.setChecked(false);
					mBtn_SDRecord.setText(AppUtil.text_menu[3][mLanguage]);
					isSDRecording = false;
				}
				mBtn_SDRecord.setEnabled(true);
				break;

			case HTTP_STOP_RECORD: // 停止录像返回的消息
				if (value == 0) {
					mToast.showToast(AppUtil.text_toast9[mLanguage]);
				} else {
					mToast.showToast(AppUtil.text_toast8[mLanguage]);
				}
				isSDRecording = false;
				mBtn_SDRecord.setText(AppUtil.text_menu[3][mLanguage]);
				mBtn_SDRecord.setEnabled(true);
				break;

			case HTTP_VERSION:// 返回版本号数据

				break;

			case HTTP_720p_1:
				Bundle bb = msg.getData();
				int ret_1 = bb.getInt("ret_1");
				Log.e("do_720p", "720_ret_1:" + ret_1);
				break;
			case HTTP_720p_2:
				Bundle bbb = msg.getData();
				int ret_2 = bbb.getInt("ret_2");
				break;
			}

		}
	};

	// class MyOnTouchListener1 implements OnTouchListener {
	//
	// GestureDetector gestureDetector;
	//
	// public MyOnTouchListener1() {
	// // TODO Auto-generated constructor stub
	// gestureDetector = new GestureDetector(PlayActivity.this,
	// new OnGestureListener() {
	//
	// float distanceX_cnt = 0, distanceY_cnt = 0;
	//
	// @Override
	// public boolean onSingleTapUp(MotionEvent e) {
	// // TODO Auto-generated method stub
	//
	// mVideoView2.videoPause();
	// mVideoView1.videoPause();
	// layout.removeAllViews();
	//
	// mVideoView1.setLayoutParams(lp1);
	// mVideoView2.setLayoutParams(lp2);
	//
	// mVideoView1.setZOrderOnTop(false);
	// mVideoView2.setZOrderOnTop(true);
	// mVideoView1.setVisibility(View.VISIBLE);
	// mVideoView2.setVisibility(View.VISIBLE);
	//
	// layout.addView(mVideoView1);
	// layout.addView(mVideoView2);
	// mVideoView2
	// .setOnTouchListener(new MyOnTouchListener());
	// mVideoView2.videoResume();
	// mVideoView1.videoResume();
	// return true;
	// }
	//
	// @Override
	// public void onShowPress(MotionEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public boolean onScroll(MotionEvent e1, MotionEvent e2,
	// float distanceX, float distanceY) {
	// // TODO Auto-generated method stub
	// distanceX_cnt += distanceX;
	// distanceY_cnt += distanceY;
	// if (Math.abs(distanceX_cnt) > 24.0f
	// || Math.abs(distanceY_cnt) > 24.0f) {
	// int l = mVideoView1.getLeft();
	// int t = mVideoView1.getTop();
	// int r = mVideoView1.getRight();
	// int b = mVideoView1.getBottom();
	// l = (int) (l - distanceX_cnt);
	// t = (int) (t - distanceY_cnt);
	// r = (int) (r - distanceX_cnt);
	// b = (int) (b - distanceY_cnt);
	// mVideoView1.layout(l, t, r, b);
	// distanceX_cnt = 0;
	// distanceY_cnt = 0;
	// }
	// return true;
	// }
	//
	// @Override
	// public void onLongPress(MotionEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public boolean onFling(MotionEvent e1, MotionEvent e2,
	// float velocityX, float velocityY) {
	// // TODO Auto-generated method stub
	// // mVideoView1.videoResume();
	// return false;
	// }
	//
	// @Override
	// public boolean onDown(MotionEvent e) {
	// // TODO Auto-generated method stub
	// // mVideoView1.videoPause();
	//
	// return false;
	// }
	// });
	// }
	//
	// @Override
	// public boolean onTouch(View v, MotionEvent event) {
	// // TODO Auto-generated method stub
	// gestureDetector.onTouchEvent(event);
	//
	// return true;
	// }
	// }

	@Override
	protected void onResume() {
		mVideoView1.videoResume();
		mVideoView2.videoResume();
		isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
		mTime_HideAllView = s_TotalTime_HideAllView;
		Log.e(TAG, "onResume================");
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mVideoView1.videoPause();
		mVideoView2.videoPause();
		Log.e(TAG, "onPause================");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		writeSetupParameter();
		mVideoView1.destory();
		mVideoView2.destory();
		mVideoView1.yuv_pixel = null;
		mVideoView2.yuv_pixel = null;
		isCountDown_HideAllView = false;
		mRocker_left.destory();
		mRocker_right.destory();
		listenRecordThread.isRun = false; // 关闭录像监听线程
		bridgeThread.isRun = false; // 关闭桥接控制线程
		new HttpThread(HTTP_RELEASE_PRIVILEGE, HTTP_handler).start(); // 释放控制权限

		// mVideoView1.mDrawThread = null;
		// mVideoView2.mDrawThread = null;

		Log.e(TAG, "onDestory===========");
		super.onDestroy();
	}

	protected void writeSetupParameter() {
		AppUtil.setSetupParameter(mCtlMsg.getSpeedLimit(), mCtlMsg.getNoHead(),
				mLanguage, mControlMode, mStroageLocaltion, mFlipImage,
				mTrimView1.getProgress(), mTrimView2.getProgress(),
				mTrimView3.getProgress());

	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// // TODO Auto-generated method stub
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// finish();
	// System.exit(0);
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	int model_choose = 1;
	int x = 0;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		// 定高
		case R.id.btn_lock:
			control_lockAndHighLimit(2);
			break;
		case R.id.totalMenu:
			if (x == 0) {
				cn.sharelink.view.RoatAnimUtil.startAnimationIn(total);
				mBtn_exit.startAnimation(SCALEbIGaNIM(500));
				mBtn_snapShot.startAnimation(SCALEbIGaNIM(500));
				mBtn_playback.startAnimation(SCALEbIGaNIM(500));
				mBtn_minitrim.startAnimation(SCALEbIGaNIM(500));
				mBtn_setting.startAnimation(SCALEbIGaNIM(500));
				model1.startAnimation(SCALEbIGaNIM(500));
				model2.startAnimation(SCALEbIGaNIM(500));
				model3.startAnimation(SCALEbIGaNIM(500));
				model4.startAnimation(SCALEbIGaNIM(500));

				mBtn_exit.setVisibility(View.VISIBLE);
				mBtn_snapShot.setVisibility(View.VISIBLE);
				if (mStroageLocaltion == 0) {
					mBtn_SDRecord.startAnimation(SCALEbIGaNIM(500));
					setViewVisibility(mBtn_SDRecord, View.VISIBLE);
					setViewVisibility(mBtn_record, View.GONE);
				} else {
					mBtn_record.startAnimation(SCALEbIGaNIM(500));
					setViewVisibility(mBtn_SDRecord, View.GONE);
					setViewVisibility(mBtn_record, View.VISIBLE);
				}
				mBtn_playback.setVisibility(View.VISIBLE);
				mBtn_minitrim.setVisibility(View.VISIBLE);
				mBtn_setting.setVisibility(View.VISIBLE);
				model1.setVisibility(View.VISIBLE);
				model2.setVisibility(View.VISIBLE);
				model3.setVisibility(View.VISIBLE);
				model4.setVisibility(View.VISIBLE);
				x = 1;
			} else if (x == 1) {
				x = 0;
				cn.sharelink.view.RoatAnimUtil.startAnimationOut(total);
				mBtn_exit.startAnimation(scaleSmallAnim(500));
				mBtn_snapShot.startAnimation(scaleSmallAnim(500));
				mBtn_playback.startAnimation(scaleSmallAnim(500));
				mBtn_minitrim.startAnimation(scaleSmallAnim(500));
				mBtn_setting.startAnimation(scaleSmallAnim(500));
				model1.startAnimation(scaleSmallAnim(500));
				model2.startAnimation(scaleSmallAnim(500));
				model3.startAnimation(scaleSmallAnim(500));
				model4.startAnimation(scaleSmallAnim(500));

				mBtn_exit.setVisibility(View.INVISIBLE);
				mBtn_snapShot.setVisibility(View.INVISIBLE);
				if (mStroageLocaltion == 0) {
					mBtn_SDRecord.startAnimation(scaleSmallAnim(500));
					setViewVisibility(mBtn_SDRecord, View.INVISIBLE);
					setViewVisibility(mBtn_record, View.GONE);
				} else {
					mBtn_record.startAnimation(scaleSmallAnim(500));
					setViewVisibility(mBtn_SDRecord, View.GONE);
					setViewVisibility(mBtn_record, View.INVISIBLE);
				}
				mBtn_playback.setVisibility(View.INVISIBLE);
				mBtn_minitrim.setVisibility(View.INVISIBLE);
				mBtn_setting.setVisibility(View.INVISIBLE);
				model1.setVisibility(View.INVISIBLE);
				model2.setVisibility(View.INVISIBLE);
				model3.setVisibility(View.INVISIBLE);
				model4.setVisibility(View.INVISIBLE);
			}
			break;

		case R.id.model1:

			model_choose = 1;
			Log.e("model_choose", "model:" + model_choose);
			remove();
			open_720p_1();
			mVideoView1.setVisibility(View.VISIBLE);
			mVideoView2.setVisibility(View.INVISIBLE);
			layout.addView(mVideoView1);
			break;
		case R.id.model2:

			model_choose = 2;
			Log.e("model_choose", "model:" + model_choose);
			remove();
			open_720p_2();
			mVideoView1.setVisibility(View.INVISIBLE);
			mVideoView2.setVisibility(View.VISIBLE);
			layout.addView(mVideoView2);
			break;

		case R.id.model3:

			model_choose = 3;
			Log.e("model_choose", "model:" + model_choose);
			remove();
			open_720p_1();
			mVideoView1.setVisibility(View.VISIBLE);
			mVideoView2.setVisibility(View.VISIBLE);
			layout.addView(mVideoView1);
			layout.addView(mVideoView2);
			break;
		case R.id.model4:

			model_choose = 4;
			Log.e("model_choose", "model:" + model_choose);
			remove();
			open_720p_2();
			mVideoView1.setVisibility(View.VISIBLE);
			mVideoView2.setVisibility(View.VISIBLE);
			layout.addView(mVideoView1);
			layout.addView(mVideoView2);
			break;
		}

	}

	private void remove() {
		mVideoView1.videoPause();
		mVideoView2.videoPause();
		onDestroy();
		Log.e("mVideoView", "destory");
		mVideoView1.setOnTouchListener(null);
		mVideoView2.setOnTouchListener(null);
		layout.removeAllViews();
	}

	private void open_720p_2() {
		new HttpThread(HTTP_720p_2, HTTP_handler).start();
		Log.e("AAAAAAAA", "开启720p_2线程");
		try {
			Thread.currentThread().sleep(3500); // 主线程休眠，以给720p线程拿到返回值的时间
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bridgeThread = new HttpThread(HTTP_START, HTTP_handler);
		bridgeThread.start();

		mVideoView1.setVideo(rtspUrl_nHD1, null);
		Log.e("test", "setVideo2.............. ");
		mVideoView2.setVideo(rtspUrl_nHD2, null);

		mVideoView1.setLayoutParams(lp2);
		mVideoView2.setLayoutParams(lp1);

		mVideoView1.setZOrderOnTop(true);
		mVideoView1.setZOrderMediaOverlay(true);
		mVideoView2.setZOrderOnTop(false);
	}

	private void open_720p_1() {
		new HttpThread(HTTP_720p_1, HTTP_handler).start();
		Log.e("AAAAAAAA", "开启720p_1线程");
		try {
			Thread.currentThread().sleep(3500); // 主线程休眠，以给720p线程拿到返回值的时间
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bridgeThread = new HttpThread(HTTP_START, HTTP_handler);
		bridgeThread.start();

		mVideoView1.setVideo(rtspUrl_nHD1, null);
		Log.e("test", "setVideo.............. ");
		mVideoView2.setVideo(rtspUrl_nHD2, null);

		mVideoView1.setLayoutParams(lp1);
		mVideoView2.setLayoutParams(lp2);

		mVideoView1.setZOrderOnTop(false);
		mVideoView2.setZOrderOnTop(true);
		mVideoView2.setZOrderMediaOverlay(true);
	}

	// 缩小动画
	private Animation scaleSmallAnim(int duration) {
		AnimationSet animationSet = new AnimationSet(true);
		ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		animationSet.addAnimation(scaleAnim);
		animationSet.addAnimation(alphaAnim);
		animationSet.setDuration(duration);
		animationSet.setFillAfter(true);
		return animationSet;
	}

	// 放大动画
	private Animation SCALEbIGaNIM(int duration) {
		AnimationSet animationSet = new AnimationSet(true);
		ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
		animationSet.addAnimation(scaleAnim);
		animationSet.addAnimation(alphaAnim);
		animationSet.setDuration(duration);
		animationSet.setFillAfter(false);
		return animationSet;
	}
}
