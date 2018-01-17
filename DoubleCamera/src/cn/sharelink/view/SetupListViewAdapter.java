package cn.sharelink.view;

import java.util.ArrayList;

import cn.sharelink.DoubleCameras.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SetupListViewAdapter extends BaseAdapter {

	private static final String TAG = "SetupListViewAdapter";

	public ArrayList<SetupItem> arrayList;
	LayoutInflater layoutf;

	public SetupListViewAdapter(Context context, int language,
			boolean isAddBalance, int[] checkIndex) {
		// TODO Auto-generated constructor stub
		this.layoutf = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		arrayList = new ArrayList<SetupItem>();

		if (isAddBalance) {
			arrayList.add(new SetupItem(
					AppUtil.text_dialog_setting_content0[0][language], null,
					null, -1));
			arrayList.add(new SetupItem(
					AppUtil.text_dialog_setting_content0[1][language], null,
					null, -1));
		} else {
			arrayList.add(new SetupItem(
					AppUtil.text_dialog_setting_content0[1][language], null,
					null, -1));
		}

		for (int i = 0; i < AppUtil.text_dialog_setting_content.length; i++) {

			String name = AppUtil.text_dialog_setting_content[i][0][language];
			String setup0 = AppUtil.text_dialog_setting_content[i][1][language];
			String setup1 = AppUtil.text_dialog_setting_content[i][2][language];

			int setupNum = i < checkIndex.length ? checkIndex[i] : 0;
			arrayList.add(new SetupItem(name, setup0, setup1, setupNum));
		}

	}

	public SetupListViewAdapter(Context context, int language, int[] checkIndex) {
		// TODO Auto-generated constructor stub
		this.layoutf = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		arrayList = new ArrayList<SetupItem>();

		SetupItem item = new SetupItem(
				AppUtil.text_dialog_setting_content0[2][language], null, null,
				-1);
		arrayList.add(item);

		for (int i = 1; i < AppUtil.text_dialog_setting_content.length; i++) {
				String name = AppUtil.text_dialog_setting_content[i][0][language];
				String setup0 = AppUtil.text_dialog_setting_content[i][1][language];
				String setup1 = AppUtil.text_dialog_setting_content[i][2][language];

				int setupNum = i - 1 < checkIndex.length ? checkIndex[i - 1]
						: 0;
				arrayList.add(new SetupItem(name, setup0, setup1, setupNum));
		}

		SetupItem version = new SetupItem(
				AppUtil.text_dialog_setting_content0[3][language], null, null,
				-1);
		arrayList.add(version);
		SetupItem ota = new SetupItem(
				AppUtil.text_dialog_setting_content0[4][language], null, null,
				-1);
		arrayList.add(ota);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder;

		if (convertView == null) {
			convertView = layoutf.inflate(R.layout.item_setup, null);

			holder = new ViewHolder();
			holder.tv_name = (TextView) convertView
					.findViewById(R.id.tv_setupName);
			holder.rg_setup = (RadioGroup) convertView
					.findViewById(R.id.radioGroup_setup);
			holder.rb_setup0 = (RadioButton) convertView
					.findViewById(R.id.rb_setup0);
			holder.rb_setup1 = (RadioButton) convertView
					.findViewById(R.id.rb_setup1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		SetupItem item = arrayList.get(position);
		holder.tv_name.setText(item.name);
		if (item.setupNum == -1 && item.setup0 == null && item.setup1 == null) {
			holder.rb_setup0.setVisibility(View.INVISIBLE);
			holder.rb_setup1.setVisibility(View.INVISIBLE);
		} else {
			holder.rb_setup0.setVisibility(View.VISIBLE);
			holder.rb_setup1.setVisibility(View.VISIBLE);
			holder.rb_setup0.setText(item.setup0);
			holder.rb_setup1.setText(item.setup1);

			if (item.setupNum == 0) {
				holder.rg_setup.check(R.id.rb_setup0);
			} else if (item.setupNum == 1) {
				holder.rg_setup.check(R.id.rb_setup1);
			}
		}

		final int index = position;

		OnClickListener rbOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v.getId() == R.id.rb_setup0) {
					arrayList.get(index).setupNum = 0;
					arrayList.get(index).isChanged = arrayList.get(index).setupNum0 != 0;
				} else if (v.getId() == R.id.rb_setup1) {
					arrayList.get(index).setupNum = 1;
					arrayList.get(index).isChanged = arrayList.get(index).setupNum0 != 1;
				}
			}
		};

		holder.rb_setup0.setOnClickListener(rbOnClickListener);
		holder.rb_setup1.setOnClickListener(rbOnClickListener);
		return convertView;
	}

	class ViewHolder {
		TextView tv_name;
		RadioGroup rg_setup;
		RadioButton rb_setup0;
		RadioButton rb_setup1;
	}

	public class SetupItem {
		String name;
		String setup0;
		String setup1;
		int setupNum0;
		int setupNum;
		boolean isChanged;

		public SetupItem(String name, String setup0, String setup1, int setupNum) {
			// TODO Auto-generated constructor stub
			this.name = name;
			this.setup0 = setup0;
			this.setup1 = setup1;
			this.setupNum = setupNum;
			this.setupNum0 = setupNum;
		}

		public int getSetupNum() {
			return setupNum;
		}

		public boolean isChanged() {
			return isChanged;
		}

		public String toString() {
			return name + "/" + setup0 + "/" + setup1 + "/" + setupNum;
		}
	}

}
