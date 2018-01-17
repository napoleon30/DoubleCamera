package cn.sharelink.DoubleCameras;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.sharelink.MyPagerAdapter;
import cn.sharelink.fragment.Fragment1;
import cn.sharelink.fragment.Fragment11;
import cn.sharelink.fragment.Fragment2;
import cn.sharelink.fragment.Fragment22;
import cn.sharelink.fragment.Fragment3;
import cn.sharelink.fragment.Fragment33;
import cn.sharelink.DoubleCameras.R;

public class HelpAcitivity extends FragmentActivity implements OnClickListener,OnPageChangeListener {
	private ImageButton mBtn_exit;
	private List<Fragment> fragments = new ArrayList<>();
	private ViewPager pager;
	private RelativeLayout layout_help;
	private FragmentManager fm = getSupportFragmentManager();

	private ImageView[] dots;// 底部小圆点图片
	private int currentIndex;// 记录当前选中位置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		// 控件初始化
		init();
		// 退出按钮做点击事件
		// 判断当前系统语言，选定相应的图片
		judgeLanguage();

		// 初始化ViewPager
		pager = (ViewPager) findViewById(R.id.pager);
		// 创建PagerAdapter的适配器
		MyPagerAdapter adapter = new MyPagerAdapter(fm, fragments);
		// viewPager加载适配器
		pager.setAdapter(adapter);
		// 绑定回调
		pager.setOnPageChangeListener((OnPageChangeListener) this);
		// 初始化底部小圆点
		initDots();
		
		
	}

	
	private void initDots() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
		dots = new ImageView[fragments.size()];

		// 循环取得小圆点图片
		for (int i = 0; i < fragments.size(); i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(true);//小圆点都设为灰色
			dots[i].setOnClickListener(this);
			dots[i].setTag(i);//设置位置tag，方便取出与当前位置对应
		}
		currentIndex = 0;
		dots[currentIndex].setEnabled(false);//第一个小圆点设为白色

	}

	private void init() {
		mBtn_exit = (ImageButton) findViewById(R.id.btn_helpExit);
		layout_help = (RelativeLayout) findViewById(R.id.layout_help);

	}

	private void judgeLanguage() {
		Locale curLocale = getResources().getConfiguration().locale;
		// 通过Locale的equals方法，判断出当前语言环境
		if (curLocale.equals(Locale.US)) {
			// 英文背景图片
			fragments.add(new Fragment11());
			fragments.add(new Fragment22());
			fragments.add(new Fragment33());
		} else {
			// 中文背景图片
			// 通过View对象做为viewpager的数据源
			fragments.add(new Fragment1());
			fragments.add(new Fragment2());
			fragments.add(new Fragment3());
		}

	}

	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll:
			int position =(int) v.getTag();
			setCurView(position);
			setCurDot(position);

		default:
			break;
		}
	}
	//设置帮助图片
	private void setCurView(int position) {
		if(position<0 || position>=fragments.size()){
			return;
		}
		pager.setCurrentItem(position);
	}
	//设置小圆点的选中
	private void setCurDot(int position) {
		if(position < 0 || position>fragments.size()-1 || currentIndex == position){
			return;
		}
		dots[position].setEnabled(false);
		dots[currentIndex].setEnabled(true);
		currentIndex = position;
	}

	@Override
	protected void onDestroy() {
		dots= null;
		layout_help.removeAllViews();
		super.onDestroy();
	}

	//当滑动状态改变时调用
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	//当当前页面被滑动时调用
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	//当新的页面被选中时调用
	@Override
	public void onPageSelected(int arg0) {
		//设置底部小点选中状态
		setCurDot(arg0);
		
	}
}
