package cn.sharelink.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.AES.AESCrypto;

/**
 * @author ChenJun 处理HTTP通信的线程类
 * 
 */
public class HttpThread extends Thread {

	private static final String TAG = "HTTP_Thread";

	public static final int HTTP_START = 0;
	public static final int HTTP_SET_TIME = 1;
	public static final int HTTP_CHECK_STROAGE = 2;
	public static final int HTTP_BRIDGE = 3;
	public static final int HTTP_TAKEPHOTO = 4;
	public static final int HTTP_START_RECORD = 5;
	public static final int HTTP_STOP_RECORD = 6;
	public static final int HTTP_GET_PRIVILEGE = 7;
	public static final int HTTP_RELEASE_PRIVILEGE = 8;
	public static final int HTTP_FLIP_MIRROR_IMAGE = 9;
	public static final int HTTP_VERSION = 10;
	public static final int HTTP_OTA = 11;
	public static final int HTTP_720p_1 = 12;
	public static final int HTTP_RESTART = 13;
	public static final int HTTP_720p_2 = 14;

	// http://192.168.100.1/server.command?command=video_h264_mode&mode=2&authcode=
	private static final String rtspUrl_720p_1 = "http://192.168.100.1:80/server.command?command=video_h264_mode&mode=2&authcode=";// mode=2表示大摄像头的
	private static final String rtspUrl_720p_2 = "http://192.168.100.1:80/server.command?command=video_h264_mode&mode=1&authcode=";// mode=2表示小摄像头的

	private static final String http_url = "http://192.168.100.1:80";
	private static final String authcode_str = "&authcode=";
	private static final String getPrivilege_cmd = "/server.command?command=get_privilege";
	private static final String releasePrivilege_cmd = "/server.command?command=release_privilege"
			+ authcode_str;
	private static final String setDate_cmd = "/server.command?command=set_date&tz=GMT-8:00&date=";
	private static final String checkStroage_cmd = "/server.command?command=check_storage";
	private static final String snapshot_cmd = "/server.command?command=snapshot&pipe=0"
			+ authcode_str;
	private static final String isRecord_cmd = "/server.command?command=is_pipe_record";
	private static final String startRecord_cmd = "/server.command?command=start_record_pipe&type=h264&pipe=0"
			+ authcode_str;
	private static final String stopRecord_cmd = "/server.command?command=stop_record&type=h264&pipe=0"
			+ authcode_str;
	private static final String flipOrMirror_cmd = "/server.command?command=set_flip_mirror&value=";
	private static final String bridge1_cmd = "/server.command?command=bridge&type=0&value=";
	private static final String bridge2_cmd = authcode_str;
	private static final String getVersion_cmd = "http://192.168.100.1/md5tag";
	private static final String getVersionInfo_cmd = "http://192.168.100.1/information";
	private static final String getVersionlog_cmd = "http://192.168.100.1/diagnostic.log";

	private static final String ota_upgrade = "/server.command?command=ota2&authcode=";

	public boolean isRun;
	public int cmd_index;
	private int value;

	private Handler handler;

	public HttpThread() {
		super();
	}

	public HttpThread(int cmd_index, Handler handler) {
		// TODO Auto-generated constructor stub
		this.cmd_index = cmd_index;
		this.handler = handler;
	}

	public HttpThread(int cmd_index, int value, Handler handler) {
		// TODO Auto-generated constructor stub
		this.cmd_index = cmd_index;
		this.handler = handler;
		this.value = value;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			switch (cmd_index) {
			case HTTP_START:

				do_setDate(); // 设置时间
				// do_getPrivilege(); // 获取权限
				do_flipOrMirror(value);
				do_checkStorage(); // 检测SD卡是否存在
				// get_version();
				do_bridge(); // 不断的发送控制数据

				break;
			case HTTP_RESTART:

				break;

			case HTTP_TAKEPHOTO:
				do_takePhoto(); // 发送拍照
				break;

			case HTTP_START_RECORD:
				do_startRecord(); // 发送开始录像
				break;

			case HTTP_STOP_RECORD: // 停止录像
				do_stopRecord();
				break;

			case HTTP_RELEASE_PRIVILEGE: // 释放控制权限
				do_releasePrivilege();
				break;
			case HTTP_FLIP_MIRROR_IMAGE:
				do_flipOrMirror(value);
				break;
			case HTTP_VERSION:// 版本信息
				get_version();
				break;
			case HTTP_OTA:// OTA升级
				do_getPrivilege(); // 获取权限
				ota_upgrade();
				break;
			case HTTP_720p_1:
				do_getPrivilege(); // 获取权限
				do_720p_1();
				break;
			case HTTP_720p_2:
				do_getPrivilege();
				do_720p_2();
				break;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String sAuthcode = null;

	/**
	 * 获取控制权限
	 */
	void do_getPrivilege() throws Exception {
		String strResult;
		int value = -1;
		HttpGet getMethod = new HttpGet(http_url + getPrivilege_cmd);
		Log.e("ota_upgrade", http_url + getPrivilege_cmd);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream inputStream = httpEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line = reader.readLine();
			JSONObject jsonObj = new JSONObject(line);
			value = jsonObj.getInt("value");
			if (value == 0) {
				line = reader.readLine();
				jsonObj = new JSONObject(line);
				String nonce = jsonObj.getString("nonce");
				Log.e("test", "nonce:" + nonce);
				String line2 = reader.readLine();
				JSONObject jsonObj2 = new JSONObject(line2);
				sAuthcode = jsonObj2.getString("authcode");
				// sAuthcode = AESCrypto.getAuthcode(nonce);
				// Log.i("do_getPrivilege", "encrypt:" +
				// AESCrypto.encrypt(nonce));
				Log.e("test", "Authcode:" + sAuthcode);
			}
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Message msg = handler.obtainMessage(HTTP_GET_PRIVILEGE, value);
		Bundle bundle = new Bundle();
		bundle.putString("authcode", sAuthcode);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	// 720p_1
	public int ret_1 = -1;

	void do_720p_1() throws ClientProtocolException, IOException, JSONException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("do_720p", "do_720p_1()");
		String strResult;

		String sb = rtspUrl_720p_1 + sAuthcode;
		Log.e("do_720p", "do_720p_1_url" + sb);

		HttpGet getMethod = new HttpGet(sb.toString());
		Log.e("test", "720p_HttpGet_1");
		HttpClient httpClient = new DefaultHttpClient();
		Log.e("test", "720p_HttpClient_1");
		HttpResponse httpResponse;
		synchronized (httpClient) {

			/* 发送请求并等待响应 */
			httpResponse = httpClient.execute(getMethod);
			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据 */
				strResult = EntityUtils.toString(httpResponse.getEntity());
				ret_1 = JSON_getValue(strResult);
				Log.e("do_720p", "720p_ret_1=" + ret_1);

			} else {
				strResult = httpResponse.getStatusLine().toString();
			}
		}

		handler.obtainMessage(HTTP_720p_1, ret_1).sendToTarget();

		Log.i("do_720p", strResult);
		// destroy();

		Message msg = handler.obtainMessage(HTTP_720p_1, ret_1);

		Bundle bundle = new Bundle();
		bundle.putInt("ret_1", ret_1);
		msg.setData(bundle);
		handler.sendMessage(msg);

	}

	// 720p_2
	public int ret_2 = -1;

	void do_720p_2() throws ClientProtocolException, IOException, JSONException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("do_720p", "do_720p_2()");
		String strResult;

		String sb = rtspUrl_720p_2 + sAuthcode;
		Log.e("do_720p", "do_720p_2_url" + sb);

		HttpGet getMethod = new HttpGet(sb.toString());
		Log.e("test", "720p_HttpGet_2");
		HttpClient httpClient = new DefaultHttpClient();
		Log.e("test", "720p_HttpClient_2");
		HttpResponse httpResponse;
		synchronized (httpClient) {

			/* 发送请求并等待响应 */
			httpResponse = httpClient.execute(getMethod);
			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据 */
				strResult = EntityUtils.toString(httpResponse.getEntity());
				ret_2 = JSON_getValue(strResult);
				Log.e("do_720p", "720p_ret_2=" + ret_2);

			} else {
				strResult = httpResponse.getStatusLine().toString();
			}
		}

		handler.obtainMessage(HTTP_720p_2, ret_2).sendToTarget();

		Log.i("do_720p_2", strResult);
		// destroy();

		Message msg = handler.obtainMessage(HTTP_720p_2, ret_2);

		Bundle bundle = new Bundle();
		bundle.putInt("ret_2", ret_2);
		msg.setData(bundle);
		handler.sendMessage(msg);

	}

	void ota_upgrade() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		if (sAuthcode == null) {
			return;
		}
		int value = -1;
		String url = http_url + ota_upgrade + sAuthcode;
		Log.i("ota_upgrade", url);
		HttpGet getMethod = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}
		Log.i("ota_upgrade", "value=" + value);

		Log.i("ota_upgrade", "strResult:" + strResult);

		handler.obtainMessage(HTTP_OTA, value).sendToTarget();
	}

	void do_releasePrivilege() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		if (sAuthcode == null) {
			return;
		}
		int value = -1;
		String url = http_url + releasePrivilege_cmd + sAuthcode;
		Log.i("do_releasePrivilege", url);
		HttpGet getMethod = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_releasePrivilege", strResult);
		handler.obtainMessage(HTTP_RELEASE_PRIVILEGE, value).sendToTarget();
	}

	void do_setDate() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		int ret1 = -1;
		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
		String time = df.format(new Date());
		HttpGet getMethod = new HttpGet(http_url + setDate_cmd + time);
		HttpClient httpClient = new DefaultHttpClient();
		Log.e("test", "do_setDate()");

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret1 = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_setDate", strResult);
		handler.obtainMessage(HTTP_SET_TIME, ret1).sendToTarget();
		Log.e("====================", "do_setDate");

	}

	void do_checkStorage() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		int value = -1;
		HttpGet getMethod = new HttpGet(http_url + checkStroage_cmd);
		HttpClient httpClient = new DefaultHttpClient();

		Log.e("test", "do_checkStorage()");
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("test", "do_checkStorage" + strResult);

		handler.obtainMessage(HTTP_CHECK_STROAGE, value).sendToTarget();
		Log.e("====================", "do_checkStorage");

	}

	void do_bridge() throws ClientProtocolException, IOException,
			JSONException, InterruptedException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("test", "do_bridge()");

		String strResult;
		isRun = true;
		ControlMsg ctlMsg = ControlMsg.getInstance();
		// while (isRun) {
		// int value = -1;
		// long time0 = System.currentTimeMillis();
		// String sendData = ctlMsg.getDataHexString("_");
		// // Log.e("test", "do_bridge():  " + sendData);
		// getMethod = new HttpGet(http_url + bridge1_cmd + sendData
		// + bridge2_cmd + sAuthcode);
		// long time1 = System.currentTimeMillis();
		// /* 发送请求并等待响应 */
		// httpResponse = httpClient.execute(getMethod);
		// long time2 = System.currentTimeMillis();
		// /* 若状态码为200 ok */
		// if (httpResponse.getStatusLine().getStatusCode() == 200) {
		// /* 读返回数据 */
		// strResult = EntityUtils.toString(httpResponse.getEntity());
		// value = JSON_getValue(strResult);
		// } else {
		// strResult = httpResponse.getStatusLine().toString();
		// }
		// long curtime = System.currentTimeMillis();
		// Message msg = handler.obtainMessage(HTTP_BRIDGE, value);
		// Bundle bundle = new Bundle();
		// bundle.putString("send", sendData.replace('_', ' '));
		// msg.setData(bundle);
		// handler.sendMessage(msg);
		// }

		String message_tcp;
		Socket socket = new Socket("192.168.100.1", 4096);
		// socket.connect(new InetSocketAddress("192.168.100.1", 4096));
		while (isRun) {
			byte[] bytes = new byte[12];
			int value = -1;
			long time0 = System.currentTimeMillis();
			String sendData = ctlMsg.getDataHexString("_");
			Log.e("test", "do_bridge():  " + sendData);
			Log.e("test", "sAuthcode:" + sAuthcode);
			String sendData1 = sendData.replace("_", "");

			String str = sAuthcode + sendData1;
			byte[] nn = str.getBytes();
			for (int i = 0; i < nn.length / 2; i++) {
				bytes[i] = uniteBytes(nn[i * 2], nn[i * 2 + 1]);
				Log.e("tttt", "**************" + bytes[i]);
			}

			try {

				// 发送数据给服务端
				OutputStream outputStream = socket.getOutputStream();
				outputStream.write(bytes);
				Log.e("test", "=======================");
				outputStream.flush();
//				 socket.shutdownOutput();
				// 读取数据
				// BufferedReader br = new BufferedReader(new InputStreamReader(
				// socket.getInputStream()));
				// String line = br.readLine();

				 InputStream reader = socket.getInputStream();
				 byte[] bbuf = new byte[8];
				 int count = reader.read(bbuf);
				 //打印读取到的数据
				 
				 Log.e("AAA", ">>>>>>>>>>>>>>>>>>>>>>>>>" + Arrays.toString(bbuf));
				 int bb = bbuf[0];
				 String string = Integer.toHexString(bb);
				Log.e("AAA", ">>>>>>>>>>>>>>>>>>>>>>>>>"+string);
				 
				// br.close();
				// reader.close();
//				 socket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e("tttt", "来自服务器的数据1");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("tttt", "来自服务器的数据2" + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		socket.close();
	}
	
	/**  
	 * 转化字符串为十六进制编码  
	 * @param s  
	 * @return  
	 */  
	public static String toHexString(String s) {  
	    String str = "";  
	    for (int i = 0; i < s.length(); i++) {  
	        int ch = (int) s.charAt(i);  
	        String s4 = Integer.toHexString(ch);  
	        str = str + s4;  
	    }  
	    return str;  
	} 

	/**
	 * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
	 * 
	 * @param src0
	 *            byte
	 * @param src1
	 *            byte
	 * @return byte
	 */
	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	/**
	 * 
	 * @param hexString
	 * @return 将十六进制转换为字节数组
	 */
	public static byte[] HexStringToBinary(String hexString) {
		// hexString的长度对2取整，作为bytes的长度
		int len = hexString.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;// 字节高四位
		byte low = 0;// 字节低四位

		for (int i = 0; i < len; i++) {
			// 右移四位得到高位
			high = (byte) ((hexString.indexOf(hexString.charAt(2 * i))) << 4);
			low = (byte) hexString.indexOf(hexString.charAt(2 * i + 1));
			bytes[i] = (byte) (high | low);// 高地位做或运算
		}
		return bytes;
	}

	public byte stringToByte(String string) {

		int in = Integer.parseInt(string.replaceAll("^0[x|X]", ""), 16);// 十六进制字符串转十进制int
		Integer integer = new Integer(in);
		byte byt = integer.byteValue();
		return byt;
	}

	void do_takePhoto() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("test", "do_takePhoto()");

		String strResult;
		int value = -1;
		String url = http_url + snapshot_cmd + sAuthcode;
		Log.i("do_takePhoto", url);
		HttpGet getMethod = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_takePhoto", strResult);
		handler.obtainMessage(HTTP_TAKEPHOTO, value).sendToTarget();
	}

	void do_startRecord() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("test", "do_startRecord()");
		String strResult;
		int value = -1;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(http_url + isRecord_cmd);
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}
		Log.i("do_startRecord1", strResult);

		if (value == 1) { // 表示正在录制
			handler.obtainMessage(HTTP_START_RECORD, value).sendToTarget();
			return;
		}

		getMethod = new HttpGet(http_url + startRecord_cmd + sAuthcode);
		httpResponse = httpClient.execute(getMethod);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_START_RECORD, value).sendToTarget();

		Log.i("do_startRecord2", strResult);
	}

	void do_stopRecord() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("test", "do_stopRecord()");
		String strResult;
		int value = -1;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(http_url + isRecord_cmd);
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}
		Log.i("do_stopRecord1", strResult);

		if (value == 0) { // 表示没有录制
			handler.obtainMessage(HTTP_STOP_RECORD, value).sendToTarget();
			return;
		}

		getMethod = new HttpGet(http_url + stopRecord_cmd + sAuthcode);
		httpResponse = httpClient.execute(getMethod);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_STOP_RECORD, value).sendToTarget();

		Log.i("do_stopRecord2", strResult);

	}

	public void get_version() throws Exception {
		String strResult;
		String Vendor_ID = null;
		String Product_ID = null;
		String Version = null;
		String Build_date = null;

		int ret2 = 0;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(getVersionInfo_cmd);

		Log.e("test", "执行get_version:");
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			Log.e("test", "get_version:成功===");

			strResult = EntityUtils.toString(httpResponse.getEntity());
			// 获取json数据对象
			/*
			 * Gson gson = new Gson(); Bean bean = gson.fromJson(strResult,
			 * Bean.class); Message msg = Message.obtain(); msg.obj = bean;
			 * handler.sendMessage(msg);
			 */
			// ret2 = JSON_getValue(strResult);
			Log.e("test", "get_version:成功===" + strResult);
			// ret2=JSON_getValue(strResult);
			if (ret2 == 0) {

				String[] sourceArray = strResult.split("\n");
				for (int i = 0; i < sourceArray.length; i++) {
					String array = sourceArray[i];
					Log.i("SSS", array);
				}
				Vendor_ID = sourceArray[2];
				Log.e("test", "get_version:成功===" + Vendor_ID);
				Product_ID = sourceArray[3];
				Log.e("test", "Product_ID:成功===" + Vendor_ID);
				Version = sourceArray[4];
				Log.e("test", "Version:成功===" + Vendor_ID);
				Build_date = sourceArray[5];
				Log.e("test", "Build_date:成功===" + Vendor_ID);
			}

		} else {
			strResult = httpResponse.getStatusLine().toString();
			Log.e("test", "get_version:失败===" + strResult);
		}
		Message msg = handler.obtainMessage(HTTP_VERSION, ret2);

		Bundle bundle = new Bundle();
		bundle.putString("strResult", strResult);
		bundle.putString("Vendor_ID", Vendor_ID);
		bundle.putString("Product_ID", Product_ID);
		bundle.putString("Version", Version);
		bundle.putString("Build_date", Build_date);
		msg.setData(bundle);
		handler.sendMessage(msg);

		// handler.obtainMessage(HTTP_VERSION, ret2).sendToTarget();

		// Message msg = handler.obtainMessage(HTTP_VERSION, ret2);
		//
		// Bundle bundle = new Bundle();
		// bundle.putString("strResult", strResult);
		// msg.setData(bundle);
		// handler.sendMessage(msg);

	}

	void do_flipOrMirror(int value) throws ClientProtocolException,
			IOException, JSONException {
		if (sAuthcode == null) {
			return;
		}

		Log.e("test", "do_flipOrMirror()");
		String strResult;
		int ret3 = -1;

		StringBuffer sb = new StringBuffer();
		sb.append(http_url).append(flipOrMirror_cmd).append(value)
				.append(authcode_str).append(sAuthcode);
		Log.e("====================", "do_flipOrMirror" + sb);
		Log.e("====================", "do_flipOrMirror value" + value);

		HttpGet getMethod = new HttpGet(sb.toString());
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */

			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret3 = JSON_getValue(strResult);
			Log.e("====================", "do_flipOrMirror" + ret3);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_FLIP_MIRROR_IMAGE, ret3).sendToTarget();

		Log.i("do_flipOrMirror", strResult);
		Log.e("====================", "do_flipOrMirror");

	}

	int JSON_getValue(String strResult) throws JSONException {
		JSONObject jsonObj = new JSONObject(strResult);
		int value = jsonObj.getInt("value");
		return value;
	}

}