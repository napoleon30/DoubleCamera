package cn.sharelink.view;

import java.util.HashMap;

import cn.sharelink.DoubleCameras.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;


public class TrimView extends RelativeLayout implements OnClickListener {

	private Context context;
	
	private ImageButton btn_increase;
	private ImageButton btn_decrease;
	private TrimBar trimBar;
	private PlayVoice playVoice;
	
	private boolean isHorz;

	public TrimView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		initView();
	}

	public TrimView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray a = context
				.obtainStyledAttributes(attrs, R.styleable.myview);
		isHorz = a.getBoolean(R.styleable.myview_orientation, true);
		this.context = context;
		initView();
	}

	public TrimView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.context = context;
		TypedArray a = context
				.obtainStyledAttributes(attrs, R.styleable.myview);
		isHorz = a.getBoolean(R.styleable.myview_orientation, true);
		initView();
	}

	private void initView() {
		if (isHorz) {
			View.inflate(context, R.layout.layout_trimview0, this);

			btn_increase = (ImageButton) findViewById(R.id.btn_increase_horztrim);
			btn_decrease = (ImageButton) findViewById(R.id.btn_decrease_horztrim);
			trimBar = (TrimBar) findViewById(R.id.sb_horztrim);

			btn_increase.setOnClickListener(this);
			btn_decrease.setOnClickListener(this);
		} else {
			View.inflate(context, R.layout.layout_trimview1, this);

			btn_increase = (ImageButton) findViewById(R.id.btn_increase_verttrim);
			btn_decrease = (ImageButton) findViewById(R.id.btn_decrease_verttrim);
			trimBar = (TrimBar) findViewById(R.id.sb_verttrim);

			btn_increase.setOnClickListener(this);
			btn_decrease.setOnClickListener(this);
		}
	}
	
	public void setPlayVoice(PlayVoice playVoice) {
		this.playVoice = playVoice;
	}
	public int getMax() {
		return trimBar.getMax();
	}
	
	public int getProgress() {
		return trimBar.getProgress();
	}

	public void setProgress(int progress) {
		trimBar.setProgress(progress);
		if (trimChangeListener != null) {
			trimChangeListener.report(this, trimBar.getProgress() - (trimBar.getMax() / 2));
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int tmp = trimBar.getProgress();
		
		if (v.getId() == R.id.btn_increase_horztrim) {
			trimBar.setProgress(tmp + 1);
		} else if (v.getId() == R.id.btn_decrease_horztrim) {
			trimBar.setProgress(tmp - 1);
		} else if (v.getId() == R.id.btn_increase_verttrim) {
			trimBar.setProgress(tmp + 1);
		} else if (v.getId() == R.id.btn_decrease_verttrim) {
			trimBar.setProgress(tmp - 1);
		}
		
		if(trimBar.getProgress() * 2 == trimBar.getMax()) {
			playVoice.play(0);
		} else {
			playVoice.play(1);
		}
		if (trimChangeListener != null) {
			trimChangeListener.report(this, trimBar.getProgress() - (trimBar.getMax() / 2));
		}
	}

	private TrimChangeListener trimChangeListener = null;

	public void setTrimChangeListener(TrimChangeListener trimChangeListener) {
		this.trimChangeListener = trimChangeListener;
	}

	public interface TrimChangeListener {
		public void report(View v, int progress);
	}
	
}
