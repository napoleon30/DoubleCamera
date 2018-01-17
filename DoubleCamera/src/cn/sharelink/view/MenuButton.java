package cn.sharelink.view;

import cn.sharelink.DoubleCameras.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MenuButton extends RelativeLayout implements OnClickListener{

	private Button button = null;
	private CheckBox checkBox = null;
	private TextView textView = null;
	
	private boolean isButton = true;
	private boolean isThreeBg = true;
	private int Speed_index = 0;
	
	private int[] tv_color = new int[2];
	
	private Drawable mBackground = null;
	private Drawable mBackground1 = null;
	private Drawable mBackground2 = null;
	private String mText;
	
	public MenuButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView(context, attrs);
	}

	public MenuButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context, attrs);
	}

	private void initView(Context context, AttributeSet attrs) {
		
		tv_color[0] = context.getResources().getColor(R.color.tv_color1);
		tv_color[1] = context.getResources().getColor(R.color.tv_color2);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.myview); 
		mBackground = a.getDrawable(R.styleable.myview_background);
		mText = a.getString(R.styleable.myview_text);
		isButton = a.getBoolean(R.styleable.myview_isbutton, true);
		isThreeBg = a.getBoolean(R.styleable.myview_isthreebg, false);
		
		if(isThreeBg) {
			mBackground1 = a.getDrawable(R.styleable.myview_background1);
			mBackground2 = a.getDrawable(R.styleable.myview_background2);
		}
		
		
		if(isButton) {
			View.inflate(context, R.layout.layout_menu_button0, this);
		
			button = (Button) findViewById(R.id.btn_menu0);
			textView = (TextView) findViewById(R.id.tv_menu0);
			
			button.setBackground(mBackground);
			textView.setText(mText);
			
			button.setOnClickListener(this);
		} else {
			View.inflate(context, R.layout.layout_menu_button1, this);
			
			checkBox = (CheckBox) findViewById(R.id.btn_menu1);
			textView = (TextView) findViewById(R.id.tv_menu1);
			
			checkBox.setBackground(mBackground);
			textView.setText(mText);
			
			checkBox.setOnClickListener(this);
		}
		
		textView.setTextColor(tv_color[0]);
	}
	
	
	public void setText(int color_index, String text) {
		if(color_index > 1) {
			color_index = 0;
		}
		textView.setTextColor(tv_color[color_index]);
		textView.setText(text);
	}
	
	public void setText(String text) {
		textView.setTextColor(tv_color[0]);
		textView.setText(text);
	}

	public boolean isChecked() {
		if(checkBox != null) {
			return checkBox.isChecked();
		}
		return false;
	}
	
	// 仅在speed的按钮下
	public int getSpeed() {
		return Speed_index % 3;
	}
	
	public void setSpeed(int speed) {
		Speed_index = speed;
		if(Speed_index == 0) {
			button.setBackground(mBackground);
		} else if(Speed_index == 1) {
			button.setBackground(mBackground1);
		} else if(Speed_index == 2) {
			button.setBackground(mBackground2);
		}

	}
	
	public void setChecked(boolean isChecked) {
		if(checkBox != null) {
			checkBox.setChecked(isChecked);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(isThreeBg) {
			Speed_index++;
			int index = Speed_index % 3;
			if(index == 0) {
				button.setBackground(mBackground);
			} else if(index == 1) {
				button.setBackground(mBackground1);
			} else if(index == 2) {
				button.setBackground(mBackground2);
			}
		}
		if(mOnClickListener != null) {
			mOnClickListener.onClick(MenuButton.this);
		}
	}
	
	private MenuButtonOnClickListener mOnClickListener = null;
	public void setMenuOnClickListener(MenuButtonOnClickListener onClickListener) {
		mOnClickListener = onClickListener;
	}
	
	public interface MenuButtonOnClickListener {
		public void onClick(View v);
	}
}
