package cn.sharelink.DoubleCameras;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cn.sharelink.view.AppUtil;
import cn.sharelink.view.FTP;
import cn.sharelink.view.FTP.UploadProgressListener;
import cn.sharelink.view.HttpThread;
import cn.sharelink.view.SetupListViewAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter.LengthFilter;
import android.text.style.LineHeightSpan.WithDensity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class HomeActivity extends Activity {

	protected static final String TAG = "HomeActivity";
	public static final int REQUSET = 1;
	boolean check = true;

	private static final int HTTP_VERSION = HttpThread.HTTP_VERSION;
	private static final int HTTP_OTA = HttpThread.HTTP_OTA;
	private static final int HTTP_720p_1 = HttpThread.HTTP_720p_1;
//	private static final int HTTP_720p_2 = HttpThread.HTTP_720p_2;

	public static final String FTP_CONNECT_SUCCESSS = "ftp连接成功";
	public static final String FTP_CONNECT_FAIL = "ftp连接失败";
	public static final String FTP_DISCONNECT_SUCCESS = "ftp断开连接";
	public static final String FTP_FILE_NOTEXISTS = "ftp上文件不存在";

	public static final String FTP_UPLOAD_SUCCESS = "ftp文件上传成功";
	public static final String FTP_UPLOAD_FAIL = "ftp文件上传失败";
	public static final String FTP_UPLOAD_LOADING = "ftp文件正在上传";

	public static final int PROGRESS_DIALOG = 100;
	protected static final String RtspVideoView = null;

	public ProgressDialog myDialog;

	String strResult;
	String Vendor_ID;
	String Product_ID;
	String Version;
	String Build_date;
	int result;
	int ret_1;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int value = (Integer) msg.obj;
			switch (msg.what) {
			case HTTP_VERSION:
				Bundle b = msg.getData();
				strResult = b.getString("strResult");
				Vendor_ID = b.getString("Vendor_ID");
				Product_ID = b.getString("Product_ID");
				Version = b.getString("Version");
				Build_date = b.getString("Build_date");
				Log.i("SSS", "=============" + strResult);

				break;

			case HTTP_OTA:
				if (value != 0) {
					Toast.makeText(HomeActivity.this, "OTAOTA",
							Toast.LENGTH_LONG).show();
				}
				break;

			case PROGRESS_DIALOG:
				Bundle bundle = msg.getData();
				result = bundle.getInt("uploadSize");

			case HTTP_720p_1:
				Bundle bb = msg.getData();
				ret_1 = bb.getInt("ret_1");
				Log.e("HomeActivity", "720_ret_1:" + ret_1);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_home);
		Log.e(TAG, "onCreate");
		

		final ButtonOnClick buttonOnClick = new ButtonOnClick(1);
		// model按钮设置tcp或udp传输
		findViewById(R.id.model_socket).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Builder builder = new AlertDialog.Builder(
								HomeActivity.this,
								AlertDialog.THEME_DEVICE_DEFAULT_DARK);
						builder.setSingleChoiceItems(
								new String[] {
										AppUtil.text_dialog1_transfer_udp[AppUtil.s_Language],
										AppUtil.text_dialog1_transfer_tcp[AppUtil.s_Language] },
								0, buttonOnClick);
						builder.setPositiveButton(
								AppUtil.text_dialog1_confirm[AppUtil.s_Language],
								buttonOnClick);
						builder.setNegativeButton(
								AppUtil.text_dialog1_cancel[AppUtil.s_Language],
								buttonOnClick);
						AlertDialog dialog = builder.create();

						dialog.show();
						// dialog.getWindow().setLayout(600, 500);

						Window dialogWindow = dialog.getWindow();
						WindowManager.LayoutParams lp = dialogWindow
								.getAttributes();
						lp.alpha = 0.9f;
						dialogWindow.setAttributes(lp);// 设置Dialog的透明度

						dialogWindow.setGravity(Gravity.CENTER);
						WindowManager m = getWindowManager();
						Display d = m.getDefaultDisplay(); // 获取(www.111cn.net)屏幕宽、高用
						WindowManager.LayoutParams p = dialogWindow
								.getAttributes(); // 获取对话框当前的参数值
						p.height = (int) (d.getHeight() * 0.5); // 高度设置为屏幕的0.3
						p.width = (int) (d.getWidth() * 0.5); // 宽度设置为屏幕的0.85
						dialogWindow.setAttributes(p);
					}
				});

		// Play按钮设置监听，启动新的VideoPlayerActivity
		findViewById(R.id.btn_play).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isWifiConnected()) {
//					
					Intent intent = new Intent();
					intent.setClass(HomeActivity.this, PlayActivity.class);
					startActivityForResult(intent, REQUSET);
				} else {
					dialog_setNetwork();

				}
			}

		});

//		new HttpThread(HTTP_720p_1, handler).start();// 提前开启720p影像线程（先连接wifi再打开app）
		new HttpThread(HTTP_VERSION, handler).start();// 提前开启版本号信息线程（先连接wifi再打开app）

		findViewById(R.id.btn_help).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HomeActivity.this,
						HelpAcitivity.class);
				startActivity(intent);
			}
		});

		// WiFi按钮设置监听，用于设置WiFi
		findViewById(R.id.btn_setting0).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						View layout_setup = LayoutInflater.from(
								HomeActivity.this).inflate(
								R.layout.dialog_setup, null);
						ListView listView_setup = (ListView) layout_setup
								.findViewById(R.id.listview_setup);
						final SetupListViewAdapter setupListViewAdapter = new SetupListViewAdapter(
								HomeActivity.this, AppUtil.s_Language,
								new int[] { AppUtil.s_NoHead,
										AppUtil.s_FlipImage,
										AppUtil.s_ControlMode,
										// AppUtil.s_Language,
										AppUtil.s_StroageLocation });

						listView_setup.setAdapter(setupListViewAdapter);

						final AlertDialog setupDialog = new AlertDialog.Builder(
								HomeActivity.this,
								AlertDialog.THEME_DEVICE_DEFAULT_DARK)
								.setTitle(
										AppUtil.text_dialog_setting_title[AppUtil.s_Language])
								.setView(layout_setup)
								.setPositiveButton(
										AppUtil.text_dialog_setting_confirm[AppUtil.s_Language],
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												int len = setupListViewAdapter.arrayList
														.size();

												int[] nums = new int[len];
												for (int i = 1; i < len - 1; i++) {
													SetupListViewAdapter.SetupItem setupItem = setupListViewAdapter.arrayList
															.get(i);
													nums[i] = setupItem
															.getSetupNum();
													Log.i(TAG, setupItem
															.toString());
												}

												AppUtil.s_StroageLocation = nums[len - 3];
												// AppUtil.s_Language =
												// nums[len- 2];
												AppUtil.s_ControlMode = nums[len - 4];
												AppUtil.s_FlipImage = nums[len - 5];
												AppUtil.s_NoHead = nums[len - 6];
												AppUtil.writeSetupParameterToFile();
											}
										})
								.setNegativeButton(
										AppUtil.text_dialog_setting_cancel[AppUtil.s_Language],
										null).show();

						listView_setup
								.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(
											AdapterView<?> parent, View view,
											int position, long id) {
										// TODO Auto-generated method stub
										if (position == 0) {// 点击网络设置
											setupDialog.dismiss();
											setNetwork();
										}
										if (position == 5) { // 点击版本号查询
											setupDialog.dismiss();
											try {
												Log.i("DDD", "HttpThread start"
														+ position);
												version_number();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										if (position == 6) { // 点击OTA版本升级
											setupDialog.dismiss();

											myDialog = new ProgressDialog(
													HomeActivity.this,
													AlertDialog.THEME_DEVICE_DEFAULT_DARK);
											myDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // 设置为矩形
											myDialog.setTitle(AppUtil.text_upgrade1[AppUtil.s_Language]);
											myDialog.setMessage(AppUtil.text_upgrade2[AppUtil.s_Language]);
											myDialog.setIcon(R.drawable.upgrade);
											myDialog.setIndeterminate(false); // 设置进度条是否为不明确
											// myDialog.setCancelable(true);
											myDialog.setCanceledOnTouchOutside(false);
											myDialog.setMax(100); // 设置进度条的最大值
											myDialog.setProgress(0); // 设置当前默认进度为
																		// 0

											// 为进度条添加取消按钮
											myDialog.setButton(
													AppUtil.text_dialog_setting_confirm[AppUtil.s_Language],
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int which) {
															myDialog.cancel();

														}
													});
											myDialog.show(); // 显示进度条

											new Thread() {
												public void run() {
													while (result <= 100) {
														myDialog.setProgress(result);
													}

													myDialog.dismiss();
												}
											}.start();

											// 上传md5tag文件
											new Thread(new Runnable() {
												public void run() {
													// 上传
													InputStream inputStream1 = HomeActivity.this
															.getResources()
															.openRawResource(
																	R.raw.md5tag);
													String fileName1 = "md5tag";
													try {
														new FTP()
																.uploadSingleFile(
																		inputStream1,
																		fileName1,
																		"",
																		new UploadProgressListener() {

																			@Override
																			public void onUploadProgress(
																					String currentStep,
																					long uploadSize,
																					InputStream inputStream) {
																				if (currentStep
																						.equals(HomeActivity.FTP_UPLOAD_SUCCESS)) {
																					Log.d(TAG,
																							"-----shanchuan11111--successful");
																				}
																			}
																		});
													} catch (IOException e) {
														e.printStackTrace();
													}

													// 上传_firmware_.bin文件
													InputStream inputStream2 = HomeActivity.this
															.getResources()
															.openRawResource(
																	R.raw._firmware_);
													String fileName2 = "_firmware_.bin";

													try {
														new FTP()
																.uploadSingleFile(
																		inputStream2,
																		fileName2,
																		"",
																		new UploadProgressListener() {

																			@Override
																			public void onUploadProgress(
																					String currentStep,
																					final long uploadSize,
																					InputStream inputStream2) {
																				if (currentStep
																						.equals(HomeActivity.FTP_UPLOAD_SUCCESS)) {
																					new HttpThread(
																							HTTP_OTA,
																							handler)
																							.start();
																					Log.d(TAG,
																							"-----shanchuan--successful");

																				} else if (currentStep
																						.equals(HomeActivity.FTP_UPLOAD_LOADING)) {
																					Log.d(TAG,
																							"uploadSize"
																									+ uploadSize);
																					float num = (float) uploadSize / 5080064;
																					Log.d(TAG,
																							"-----shangchuan--num-"
																									+ num);
																					result = (int) (num * 100);
																					Log.d(TAG,
																							"-----shangchuan---"
																									+ result
																									+ "%");

																					Message msg = handler
																							.obtainMessage(
																									PROGRESS_DIALOG,
																									0);
																					Bundle bundle = new Bundle();
																					bundle.putInt(
																							"uploadSize",
																							result);
																					msg.setData(bundle);
																					handler.sendMessage(msg);

																				}

																			}
																		});
													} catch (IOException e1) {
														e1.printStackTrace();
													}
												}
											}).start();

										}
									}

								});
					}
				});

		// file按钮设置监听，用于查看截图和录像的文件
		findViewById(R.id.btn_file).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HomeActivity.this,
						FileActivity.class);
				// Intent intent = new Intent(HomeActivity.this,
				// SetupActivity.class);
				startActivity(intent);
			}
		});

		pathIsExist();

		try {
			AppUtil.readDataFile();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * VideoPlayerActivity中无法连接到摄像头时，调用该函数 弹出是否需要设置WiFi的对话框
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUSET && resultCode == -1) {
			dialog_setNetwork();
		}
	}

	/**
	 * 检测网络状态
	 * 
	 * @return true 连接的是WiFi false 连接的是移动数据或者没有连接上网络
	 */
	private boolean isWifiConnected() {

		boolean flag = false;
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String ssid = wifiInfo.getSSID();
		Log.d("JNI", "this is wificheck" + ssid);
		if (ssid.startsWith("Skycam", 1) || ssid.startsWith("InnoCam", 1)) {
			flag = true;

		} else {
			flag = false;
		}

		return flag;
	}

	/**
	 * 连接网络失败弹出的dialog
	 */
	private void dialog_setNetwork() {
		int i = AppUtil.s_Language;

		AlertDialog.Builder builder = new AlertDialog.Builder(this,
				AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setTitle(AppUtil.text_dialog1_tittle[i]);
		builder.setMessage(AppUtil.text_dialog1_info[i]);
		builder.setPositiveButton(AppUtil.text_dialog1_confirm[i],
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setNetwork();
					}
				});
		builder.setNegativeButton(AppUtil.text_dialog1_cancel[i], null);
		builder.create();
		builder.show();
	}

	/**
	 * 网络未连接时，调用设置方法
	 */
	private void setNetwork() {

		Intent intent = null;
		/**
		 * 判断手机系统的版本！如果API大于10 就是3.0+ 因为3.0以上的版本的设置和3.0以下的设置不一样，调用的方法不同
		 */
		if (android.os.Build.VERSION.SDK_INT > 10) {
			intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
		} else {
			intent = new Intent();
			ComponentName component = new ComponentName("com.android.settings",
					"com.android.settings.WirelessSettings");
			intent.setComponent(component);
			intent.setAction("android.intent.action.VIEW");
		}
		startActivity(intent);
	}

	private void version_number() throws Exception {
		String version = Vendor_ID + "\n" + Product_ID + "\n" + Version + "\n"
				+ Build_date;
		Log.i("DDD", version);
		AlertDialog.Builder builder = new Builder(HomeActivity.this,
				AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setTitle(AppUtil.version_number[AppUtil.s_Language]);
		builder.setMessage(version);
		builder.setPositiveButton(
				AppUtil.text_dialog1_confirm[AppUtil.s_Language],
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();

	}
	
	@Override
	protected void onResume() {
		if(isWifiConnected()){
			new HttpThread(HTTP_VERSION, handler).start();// 提前开启版本号信息线程（先打开app再连接wifi）
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		System.exit(0);
		super.onDestroy();
	}

	/**
	 * 路径是否存在 不存在则创建
	 */
	private void pathIsExist() {
		File file = new File(AppUtil.getImagePath());
		if (!file.exists()) {
			file.mkdirs();
		}

		File file1 = new File(AppUtil.getVideoPath());
		if (!file1.exists()) {
			file1.mkdirs();
		}
	}

	private class ButtonOnClick implements DialogInterface.OnClickListener {

		private int index;// 选项的索引
		String socket = "udp";

		public ButtonOnClick(int index) {
			this.index = index;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// which表示单击的索引，所有选项的索引都是大于0，按钮索引都是小于0的
			if (which == 0) {
				// 如果单击的是列表项，将当前列表项的索引保存在index中。
				// 如果想单击列表项后关闭对话框，可在此处调用dialog.cancel()
				// 或是用dialog.dismiss()方法。
				index = which;
				socket = "udp";
				Log.e("transport", socket);
			} else if (which == 1) {
				index = which;
				socket = "tcp";
				Log.e("transport", socket);
			} else if (which == DialogInterface.BUTTON_POSITIVE) {
				AppUtil.Transport = socket;
				dialog.dismiss();
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
				dialog.dismiss();
			}
		}

	}
}
