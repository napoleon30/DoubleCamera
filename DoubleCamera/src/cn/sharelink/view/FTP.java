package cn.sharelink.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;

import cn.sharelink.DoubleCameras.HomeActivity;

public class FTP {
	/**
	 * 服务器名.
	 */
	private String hostName;

	/**
	 * 端口号
	 */
	private int serverPort;

	/**
	 * 用户名.
	 */
	private String userName;

	/**
	 * 密码.
	 */
	private String password;

	/**
	 * FTP连接.
	 */
	private FTPClient ftpClient;

	public FTP() {
//		this.hostName = "192.168.0.105";
//			this.hostName = "ftp://192.168.100.1";
	this.hostName = "192.168.100.1";
		this.serverPort= 21 ;
		this.userName = "anonymous";
//		this.userName = "";
		this.password = "";
		this.ftpClient = new FTPClient();
	}

	// -------------------------------------------------------文件上传方法------------------------------------------------

	/**
	 * 上传单个文件.
	 * 
	 * @param localFile
	 *            本地文件
	 * @param remotePath
	 *            FTP目录
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	public void uploadSingleFile(
			InputStream inputStream, 
			String fileName,
			String remotePath,
			UploadProgressListener listener) throws IOException {

		// 上传之前初始化
		this.uploadBeforeOperate(remotePath, listener);

		boolean flag;
		flag = uploadingSingle(inputStream,fileName, listener);
		if (flag) {
			listener.onUploadProgress(HomeActivity.FTP_UPLOAD_SUCCESS, 0,
					inputStream);
		} else {
			listener.onUploadProgress(HomeActivity.FTP_UPLOAD_FAIL, 0,
					inputStream);
		}

		// 上传完成之后关闭连接
		this.uploadAfterOperate(listener);
	}

//	/**
//	 * 上传多个文件.
//	 * 
//	 * @param localFile
//	 *            本地文件
//	 * @param remotePath
//	 *            FTP目录
//	 * @param listener
//	 *            监听器
//	 * @throws IOException
//	 */
//	public void uploadMultiFile(LinkedList<File> fileList, String remotePath,
//			UploadProgressListener listener) throws IOException {
//
//		// 上传之前初始化
//		this.uploadBeforeOperate(remotePath, listener);
//
//		boolean flag;
//
//		for (File singleFile : fileList) {
//			flag = uploadingSingle(singleFile, listener);
//			if (flag) {
//				listener.onUploadProgress(MainActivity.FTP_UPLOAD_SUCCESS, 0,
//						singleFile);
//			} else {
//				listener.onUploadProgress(MainActivity.FTP_UPLOAD_FAIL, 0,
//						singleFile);
//			}
//		}
//
//		// 上传完成之后关闭连接
//		this.uploadAfterOperate(listener);
//	}

	/**
	 * 上传单个文件.
	 * 
	 * @param localFile
	 *            本地文件
	 * @return true上传成功, false上传失败
	 * @throws IOException
	 */
	private boolean uploadingSingle(InputStream inputStream,String fileName,
			UploadProgressListener listener) throws IOException {
		boolean flag = true;
		// 不带进度的方式
		// // 创建输入流
		// InputStream inputStream = new FileInputStream(localFile);
		 // 上传单个文件
//		 flag = ftpClient.storeFile(fileName, inputStream);
//		 // 关闭文件流
//		 inputStream.close();

		// 带有进度的方式
		BufferedInputStream buffIn = new BufferedInputStream(
				inputStream);
		ProgressInputStream progressInput = new ProgressInputStream(buffIn,listener, fileName);
		flag = ftpClient.storeFile(fileName, progressInput);
		buffIn.close();

		return flag;
	}
	
	/**
	 * 上传文件之前初始化相关参数
	 * 
	 * @param remotePath
	 *            FTP目录
	 * @param listener
	 *            监听器
	 * @throws IOException
	 */
	private void uploadBeforeOperate(String remotePath,
			UploadProgressListener listener) throws IOException {

		// 打开FTP服务
		try {
			this.openConnect();
			listener.onUploadProgress(HomeActivity.FTP_CONNECT_SUCCESSS, 0,
					null);
		} catch (IOException e1) {
			e1.printStackTrace();
			listener.onUploadProgress(HomeActivity.FTP_CONNECT_FAIL, 0, null);
			return;
		}

		// 设置模式
		ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
		// FTP下创建文件夹
		ftpClient.makeDirectory(remotePath);
		// 改变FTP目录
		ftpClient.changeWorkingDirectory(remotePath);
		// 上传单个文件

	}

	/**
	 * 上传完成之后关闭连接
	 * 
	 * @param listener
	 * @throws IOException
	 */
	private void uploadAfterOperate(UploadProgressListener listener)
			throws IOException {
		this.closeConnect();
		listener.onUploadProgress(HomeActivity.FTP_DISCONNECT_SUCCESS, 0, null);
	}


	// -------------------------------------------------------打开关闭连接------------------------------------------------

	/**
	 * 打开FTP服务.
	 * 
	 * @throws IOException
	 */
	public void openConnect() throws IOException {
		// 中文转码
		ftpClient.setControlEncoding("UTF-8");
		int reply; // 服务器响应值
		// 连接至服务器
		ftpClient.connect(hostName, serverPort);
		// 获取响应值
		reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			// 断开连接
			ftpClient.disconnect();
			throw new IOException("connect fail: " + reply);
		}
		// 登录到服务器
		ftpClient.login(userName, password);
		// 获取响应值
		reply = ftpClient.getReplyCode();
		Log.i("reply", "===="+reply);
		if (!FTPReply.isPositiveCompletion(reply)) {
			// 断开连接
			ftpClient.disconnect();
			throw new IOException("connect fail: " + reply);
		} else {
			// 获取登录信息
			FTPClientConfig config = new FTPClientConfig(ftpClient
					.getSystemType().split(" ")[0]);
			config.setServerLanguageCode("zh");
			ftpClient.configure(config);
			// 使用被动模式设为默认
			ftpClient.enterLocalPassiveMode();
			// 二进制文件支持
			ftpClient
					.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
		}
	}

	/**
	 * 关闭FTP服务.
	 * 
	 * @throws IOException
	 */
	public void closeConnect() throws IOException {
		if (ftpClient != null) {
			// 退出FTP
			ftpClient.logout();
			// 断开连接
			ftpClient.disconnect();
		}
	}

	// ---------------------------------------------------上传、下载、删除监听---------------------------------------------
	
	/*
	 * 上传进度监听
	 */
	public interface UploadProgressListener {
		public void onUploadProgress(String currentStep, long uploadSize, InputStream inputStream);
	}


}
