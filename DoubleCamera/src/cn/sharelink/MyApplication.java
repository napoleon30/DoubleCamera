package cn.sharelink;

import cn.sharelink.view.RtspVideoView;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application{
private static Context context;
	

	@Override
	public void onCreate() {
		context = getApplicationContext();
	}

	public static Context getContext() {
		return context;
	}
	
	public static void destory_(RtspVideoView r) {
		r.destory();

	}
}
