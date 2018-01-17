package cn.sharelink.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.sharelink.MyApplication;

import android.os.Environment;

/**
 * APP工具类
 */
public class AppUtil {

	public static final String RTSP_URL_nhd = "rtsp://192.168.100.1/cam1/h264-1";
	public static final String RTSP_URL_720P = "rtsp://192.168.100.1/cam1/h264";
	
	public static final String APP_PATH = getSDPath() + "/Android/data/cn.sharelink.doublecamera";
	
	public static final String IMG_TYPE = ".jpg";
	public static final String VID_TYPE = ".mp4";
	
	public static  String Transport = "udp";
	
	public static String getCurrentTime() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
	}
	
	public static String getFilePath() {
		File appDir = new File(getSDPath() + "/DoubleCamera");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}

		return getSDPath() + "/DoubleCamera";
	}
	public static String getImagePath() {
		File appDir = new File(getFilePath() + "/snapshot");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		return getFilePath() + "/snapshot";

	}
	public static String getVideoPath() {
		File appDir = new File(getFilePath() + "/video");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		return getFilePath() + "/video";

	}
	
	public static String getImageName() {
		return AppUtil.getImagePath() + "/IMG_" + AppUtil.getCurrentTime() + ".jpg";
	}
	public static String getVideoName() {
		return AppUtil.getVideoPath() + "/VID_" + AppUtil.getCurrentTime() + ".mp4";
	}
	
	public static final String[][] text_menu = {
		{"退出",		"Exit" ,"退出"}, //0
		{"拍照",		"Snapshot","拍照"}, //1 
		{"录像",		"Record","錄像"}, //2
		{"SD录像", 	"SD Record","SD錄像"}, //3
		{"文件",		"Playback","文件"}, //4 
		{"速度",		"Speed","速度"}, //5
		{"重力控制",	"Gravity","重力控制"},//6
		{"开/关", 	"On/Off","開/關"},//7
		{"翻滚", 	"Rolling","翻滾"},//8
		{"一键降落",	"Landing","壹鍵降落"},//9
		{"设置", 	"Setting","設置"},//10
		{"一键解锁",	"Unlock","壹鍵解鎖"},//11
		{"微调","Minitrim","微調"},//12
	};
	
	public static final String[] version_number = {"版本参数:","Version Parameter:","版本參數:"};
	
	public static final String[] text_dialog1_tittle = { "提示", "WiFi is disable","提示"};
	public static final String[] text_dialog1_info = {"无法连接到摄像头，请设置网络", "Please enable WiFi and connect to specified SSID","無法連接到攝像頭，請設置網絡"};
	public static final String[] text_dialog1_cancel = {"取消", "Cancel","取消"};
	public static final String[] text_dialog1_confirm = {"确定", "OK","確定"};
	
	public static final String[] text_dialog1_transfer_udp = {"UDP传输模式","udp transfer model","UDP傳輸模式"};
	public static final String[] text_dialog1_transfer_tcp = {"TCP传输模式","tcp transfer model","TCP傳輸模式"};
	
	public static final String[] text_dialog_delfile_tittle = {"提示", "Message","提示"};
	public static final String[] text_dialog_delfile_content = {"是否删除 \"", "Delete \"","是否刪除 \""};
	public static final String[] text_dialog_delfile_cancel = {"取消", "Cancel","取消"};
	public static final String[] text_dialog_delfile_confirm = {"删除", "Delete","刪除"};
	public static final String[] text_dialog_delfile_failed = {"删除失败", "Delete failed","刪除失敗"};
	
	public static final String[] text_dialog_renamefile_tittle = {"重命名", "Rename","重命名"};
	public static final String[] text_dialog_renamefile_cancel = {"取消", "Cancel","取消"};
	public static final String[] text_dialog_renamefile_confirm = {"确定", "OK","確定"};
	public static final String[] text_dialog_renamefile_failed = {"重命名失败", "Rename failed","重命名失敗"};
			
	public static final String[] text_dialog_setting_title = {"设置", "Setting","設置"}; 
	public static final String[][] text_dialog_setting_content0 = {
		{"一键平衡", "Balance" ,"壹鍵平衡"   		},     
		{"紧急停止", "Stop"    ,"緊急停止"			},  
		{"网络设置", "Configure network","網絡設置"},
		{"版本号查询","Version Checked","版本號查詢"},
		{"OTA版本升级","OTA Version Upgrade","OTA版本升級"}
		
	};
	public static final String[][][] text_dialog_setting_content = {
		{{"定高模式", "High Limit" ,"定高模式"   	}, {"关闭", "Close" 	,"關閉"	}, {"开启", "Open","開啟"}},     
		{{"无头模式", "No Direction" ,"無頭模式" 	}, {"关闭", "Close","關閉" 		}, {"开启", "Open","開啟"}},    
		{{"影像翻转", "Image to flip","影像翻轉" 	}, {"关闭", "Close", "關閉" 	}, {"开启", "Open","開啟"}},     
		{{"操作模式", "Control mode","操作模式"  	}, {"左手", "Left","左手"		}, {"右手", "Right","右手" }},     
//		{{"语言切换", "Language"     		}, {"中文", "Chinese"	}, {"英文", "English"}},    
		{{"存储位置", "Stroage Location","存儲位置"	}, {"SD卡", "SD Card","SD卡"	}, {"手机", "Phone","手機" }},
		

	};
	public static final String[] text_dialog_setting_cancel = {"取消", "Cancel","取消"};
	public static final String[] text_dialog_setting_confirm = {"确定", "OK","確定"};
	
	public static final String[] text_toast1 = {"已保存到", "Saved as ","已保存到"};
	public static final String[] text_toast2 = {"拍照失败", "Failed to Snapshot","拍照失敗"};
	public static final String[] text_toast3 = {"录制中，请稍后操作", "Recording, please wait...","錄制中，請稍後操作"};
	public static final String[] text_toast4 = {"获取权限失败", "Failed to get privilege","獲取權限失敗"};
	public static final String[] text_toast5 = {"释放权限失败", "Failed to release privilege","釋放權限失敗"};
	public static final String[] text_toast6 = {"设置时间失败", "Failed to Set time","設置時間失敗"};
	public static final String[] text_toast7 = {"正在录制", "Recording","正在錄制"};
	public static final String[] text_toast8 = {"录制失败", "Failed to record","錄制失敗"};
	public static final String[] text_toast9 = {"录制成功", "Recording success","錄制成功"};
	public static final String[] text_toast10 = {"请检查SD卡", "Please check the SDcard","請檢查SD卡"};
	
	public static final String[] text_dialog = {"连接中，请稍候 ...", "Connecting, please wait...","連接中，請稍候 ..."};
	
	public static final String[] text_upgrade1 = {"升级","Upgrade","升級"};
	public static final String[] text_upgrade2 = {"升级文件上传中，请稍后...","Upgrading, please wait...","升級文件上傳中，請稍後..."};
	
	public static String getProgressDialogText() {
		return text_dialog[s_Language];
	}
	
	public static String getSDPath(){
		boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(hasSDCard){
			return Environment.getExternalStorageDirectory().toString();
		}else
			return Environment.getDownloadCacheDirectory().toString();
	}
	
	public static int s_SpeedChoose;
	public static int s_NoHead;
	public static int s_Language;
	public static int s_ControlMode;
	public static int s_StroageLocation;
	public static int s_FlipImage;

	public static int trim1;
	public static int trim2;
	public static int trim3;
	
	public static void readDataFile() throws NumberFormatException, IOException {

		File appDir = new File(APP_PATH);
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		
		File dataFile = new File(AppUtil.APP_PATH + "/data");
		
		if (dataFile.exists()) {
			FileInputStream fis = new FileInputStream(dataFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			String line;
			
			while((line = reader.readLine()) != null) {
				String dat = line.substring(line.indexOf(":") + 1);
				if(line.startsWith("speed")) {
					
					if(dat.equals("30%")) {
						s_SpeedChoose = 0;
					} else if(dat.equals("60%")) {
						s_SpeedChoose = 1;
					} else if(dat.equals("100%")){
						s_SpeedChoose = 2;
					} else {
						s_SpeedChoose = 0;
					}
				}
				
				if(line.startsWith("nohead")) {
					
					if(dat.equals("CLOSE")) {
						s_NoHead = 0;
					} else if(dat.equals("OPEN")) {
						s_NoHead = 1;
					} else {
						s_NoHead = 0;
					}
				}
				
				Locale locale = MyApplication.getContext().getResources()
						.getConfiguration().locale;
				String language = locale.getLanguage();
				String country = locale.getCountry();
//				Log.i("AAAAAA", "语言是" + language + country);

				if (language.endsWith("zh")) {
					if (country.endsWith("CH")) {
						s_Language = 0;
					}
					if (country.endsWith("TW")) {
						s_Language = 2;
					}
				} else if (language.endsWith("en")) {
					s_Language = 1;
				}else{
					s_Language=0;
				}
				
				if(line.startsWith("hand")) {
					if(dat.equals("LEFT")) {
						s_ControlMode = 0;
					} else if(dat.equals("RIGHT")) {
						s_ControlMode = 1;
					} else {
						s_ControlMode = 0;
					}
				}
				
				if(line.startsWith("stroage")) {
					if(dat.equals("SDcard")) {
						s_StroageLocation = 0;
					} else if(dat.equals("Phone")) {
						s_StroageLocation = 1;
					} else {
						s_StroageLocation = 1;
					}
				}
				
				if(line.startsWith("rotate")) {
					if(dat.equals("0")) {
						s_FlipImage = 0;
					} else if(dat.equals("180")) {
						s_FlipImage = 1;
					} else {
						s_FlipImage = 0;
					}
				}
				
				if(line.startsWith("trim1")) {
					trim1 = Integer.parseInt(dat);
				}
				if(line.startsWith("trim2")) {
					trim2 = Integer.parseInt(dat);
				}
				if(line.startsWith("trim3")) {
					trim3 = Integer.parseInt(dat);
				}
			}
			reader.close();
			fis.close();
		} else {
			s_SpeedChoose = 0;
			s_Language = 0;
			s_ControlMode = 0;
			s_NoHead = 0;
			s_StroageLocation = 0;
			s_FlipImage = 0;
			trim1 = 32;
			trim2 = 32;
			trim3 = 32;
			
			writeSetupParameterToFile();
		}
	}
	
	public static void writeSetupParameterToFile() {

		try {
			File dataFile = new File(AppUtil.APP_PATH + "/data");
			FileOutputStream fos = new FileOutputStream(dataFile, false); // 如果采用追加方式用true
			StringBuffer sb = new StringBuffer();
			
			if(s_SpeedChoose == 0) {
				sb.append("speed:30%\n");
			} else if(s_SpeedChoose == 1){
				sb.append("speed:60%\n");
			} else if(s_SpeedChoose == 2){
				sb.append("speed:100%\n");
			}
			
			if(s_NoHead == 0) {
				sb.append("nohead:CLOSE\n");
			} else {
				sb.append("nohead:OPEN\n");
			}
			
			if(s_Language == 0) {
				sb.append("language:CH\n");
			} else {
				sb.append("language:EN\n");
			}
			
			if(s_ControlMode == 0) {
				sb.append("hand:LEFT\n");
			} else {
				sb.append("hand:RIGHT\n");
			}
			
			if(s_StroageLocation == 0) {
				sb.append("stroage:SDcard\n");
			} else {
				sb.append("stroage:Phone\n");
			}
			
			if(s_FlipImage == 0) {
				sb.append("rotate:0\n");
			} else {
				sb.append("rotate:180\n");
			}
			
			sb.append("trim1:" + trim1 + "\n")
			  .append("trim2:" + trim2 + "\n")
			  .append("trim3:" + trim3 + "\n");

			fos.write(sb.toString().getBytes("UTF8"));
			fos.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public static void setSetupParameter(int speedChoose, int headDirection, int language, int controlMode, int stroageLocation, int flipImage, int trim1, int trim2, int trim3) {
		
		AppUtil.s_SpeedChoose = speedChoose;
		AppUtil.s_NoHead = headDirection;
		AppUtil.s_Language = language;
		AppUtil.s_ControlMode = controlMode;
		AppUtil.s_StroageLocation = stroageLocation;
		AppUtil.s_FlipImage = flipImage;
		AppUtil.trim1 = trim1;
		AppUtil.trim2 = trim2;
		AppUtil.trim3 = trim3;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				writeSetupParameterToFile();
			}
		}).start();
		
	}
}
