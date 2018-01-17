package cn.sharelink.view;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class MyToast {
	private Context context;
	private Toast mToast;
	
	public MyToast(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public void showToast(String text) {
		if (mToast == null) {
			mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.CENTER, 0, 0);
		} else {
			mToast.setText(text);
			mToast.setDuration(Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	public void cancelToast() {
		if (mToast != null) {
			mToast.cancel();
		}
	}
}
