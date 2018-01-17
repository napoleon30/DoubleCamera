package cn.sharelink.DoubleCameras;

import java.io.File;
import java.util.ArrayList;

import cn.sharelink.view.AppUtil;
import cn.sharelink.view.MyToast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class FileActivity extends Activity {

	static final String TAG = "FileActivity";
	
	ListView mListView_pic;
	ListView mListView_video;
	FileAdapter mFileAdapter_pic;
	FileAdapter mFileAdapter_video;
	ArrayList<File> mArrayList_pic;
	ArrayList<File> mArrayList_video;
	
	private MyToast mToast;
	
	
	private static final String[][] text_fileTittle = {
		{"图片", "Pictures","圖片"},
		{"视频", "Videos","視頻"},
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_file);
		
		listView_init();
		
		TextView tv_pic = (TextView) findViewById(R.id.tv_file_pic);
		TextView tv_vid = (TextView) findViewById(R.id.tv_file_video);
		
		tv_pic.setText(text_fileTittle[0][AppUtil.s_Language]);
		tv_vid.setText(text_fileTittle[1][AppUtil.s_Language]);
		
		mToast = new MyToast(this);
	}

	void listView_init() {
		mListView_pic = (ListView) findViewById(R.id.lv_pic);
		mListView_video = (ListView) findViewById(R.id.lv_video);
		
		mArrayList_pic = new ArrayList<File>();
		mArrayList_video = new ArrayList<File>();
		init_arraylist(0);
		init_arraylist(1);
		
		mFileAdapter_pic = new FileAdapter(this, mArrayList_pic, true);
		mFileAdapter_video = new FileAdapter(this, mArrayList_video, false);
		
		mListView_pic.setAdapter(mFileAdapter_pic);
		mListView_video.setAdapter(mFileAdapter_video);

		mListView_pic.setOnItemClickListener(new OnItemClickListener_Pic());
		mListView_pic.setOnCreateContextMenuListener(new OnItemClickListener_Pic());
		
		mListView_video.setOnItemClickListener(new OnItemClickListener_Video());
		mListView_video.setOnCreateContextMenuListener(new OnItemClickListener_Video());
		
	}
	
	private static final int MENU_GROUPID_PIC = 1;
	private static final int MENU_GROUPID_VID = 2;
	private static final int MENU_INDEX_OPEN = 0;
	private static final int MENU_INDEX_SHARE = 1;
	private static final int MENU_INDEX_RENAME = 2;
	private static final int MENU_INDEX_DELETE = 3;
	private static final String[][] Menu_TEXT = {
		{"打开", "Open","打開"},
		{"分享", "Share","分享"},
		{"重命名","Rename","重命名"},
		{"删除", "Delete","刪除"},
		{"分享文件", "Share file","分享文件"},
	};
	
	class OnItemClickListener_Pic implements OnItemClickListener, OnCreateContextMenuListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			openFile(mArrayList_pic.get(arg2));
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			// TODO Auto-generated method stub
			menu.add(MENU_GROUPID_PIC, MENU_INDEX_OPEN, 0, Menu_TEXT[MENU_INDEX_OPEN][AppUtil.s_Language]);
			menu.add(MENU_GROUPID_PIC, MENU_INDEX_SHARE, 1, Menu_TEXT[MENU_INDEX_SHARE][AppUtil.s_Language]);
			menu.add(MENU_GROUPID_PIC, MENU_INDEX_RENAME, 2, Menu_TEXT[MENU_INDEX_RENAME][AppUtil.s_Language]);
			menu.add(MENU_GROUPID_PIC, MENU_INDEX_DELETE, 3, Menu_TEXT[MENU_INDEX_DELETE][AppUtil.s_Language]);
		}
	}
	
	class OnItemClickListener_Video implements OnItemClickListener, OnCreateContextMenuListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			openFile(mArrayList_video.get(arg2));
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			// TODO Auto-generated method stub
			menu.add(MENU_GROUPID_VID, MENU_INDEX_OPEN, 0, Menu_TEXT[MENU_INDEX_OPEN][AppUtil.s_Language]);
			menu.add(MENU_GROUPID_VID, MENU_INDEX_SHARE, 1, Menu_TEXT[MENU_INDEX_SHARE][AppUtil.s_Language]);
			menu.add(MENU_GROUPID_VID, MENU_INDEX_RENAME, 2, Menu_TEXT[MENU_INDEX_RENAME][AppUtil.s_Language]);
			menu.add(MENU_GROUPID_VID, MENU_INDEX_DELETE, 3, Menu_TEXT[MENU_INDEX_DELETE][AppUtil.s_Language]);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		int groupId = item.getGroupId();
		int itemId = item.getItemId();
		int post = info.position;
		
		File file = null;
		if(groupId == MENU_GROUPID_PIC) {
			file = mArrayList_pic.get(post);
		} else if(groupId == MENU_GROUPID_VID) {
			file = mArrayList_video.get(post);
		}
		
		if(itemId == MENU_INDEX_OPEN) {
			Log.i(TAG, "打开" + file.getName());
			openFile(file);
		} else if(itemId == MENU_INDEX_SHARE) {
			Log.i(TAG, "分享" + file.getName());
			shareFile(file);
		} else if(itemId == MENU_INDEX_RENAME){
			Log.i(TAG, "重命名" + file.getName());
			renameFile(file);
		} else if(itemId == MENU_INDEX_DELETE) {
			Log.i(TAG, "删除" + file.getName());
			dialog_deleteFile(file);
		}
		
		return super.onContextItemSelected(item);
	}
	
	private void refresh_listView(int index) {
		init_arraylist(index);
		if(index == 0) {
			mFileAdapter_pic.al = mArrayList_pic;
			mFileAdapter_pic.notifyDataSetChanged();
		} else if(index == 1) {
			mFileAdapter_video.al = mArrayList_video;
			mFileAdapter_video.notifyDataSetChanged();
		}
	}
	
	private void openFile(File file) {
		if(file == null) {
			return;
		}
		
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		
		String fileName = file.getName().toLowerCase();
		if(fileName.endsWith(".jpg")) {
			intent.setDataAndType(Uri.fromFile(file), "image/*");
		} else if(fileName.endsWith(".mp4")) {
			intent.setDataAndType(Uri.fromFile(file), "video/mp4");
		} 
		
		startActivity(intent);
	}
	
	private void shareFile(File file) {
		if(file == null) {
			return;
		}
		Uri fileUri = Uri.fromFile(file);
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, fileUri);
		
		String fileName = file.getName().toLowerCase();
		if(fileName.endsWith(".jpg")) {
			intent.setType("image/*");
		} else if(fileName.endsWith(".mp4")) {
			intent.setType("video/mp4");
		} 
		startActivity(Intent.createChooser(intent, Menu_TEXT[4][AppUtil.s_Language]));
	}
	
	private void renameFile(final File file) {
		if(file == null) {
			return;
		}
		
		String fileName = file.getName();
		String name = fileName.substring(0, fileName.lastIndexOf('.'));
		final String type = fileName.substring(fileName.lastIndexOf('.'));
		
		final EditText et_name = new EditText(this);
		et_name.setText(name);
		et_name.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		et_name.setSelection(name.length()); // 设置光标位置
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setTitle(AppUtil.text_dialog_renamefile_tittle[AppUtil.s_Language]);
		builder.setView(et_name);
		builder.setPositiveButton(AppUtil.text_dialog_renamefile_confirm[AppUtil.s_Language],
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
//						String name = file.getName().toLowerCase();
//						if(name.endsWith(".jpg")) {
//							index = 0;
//						} else if(name.endsWith(".mp4")) {
//							index = 1;
//						}
//						
//						file.delete();
						String newName = file.getParent() + "/" + et_name.getText().toString() + type;
						if(!file.renameTo(new File(newName))) {
							mToast.showToast(AppUtil.text_dialog_renamefile_failed[AppUtil.s_Language]);
						}
						
						int index = -1;
						if(type.toLowerCase().endsWith("jpg")) {
							index = 0;
						} else if(type.toLowerCase().endsWith("mp4")) {
							index = 1;
						} 
						
						refresh_listView(index);
						
					}
				});
		builder.setNegativeButton(AppUtil.text_dialog_renamefile_cancel[AppUtil.s_Language], null);
		builder.create();
		builder.show();
	}
	
	private void dialog_deleteFile(final File file) {
		
		if(file == null) {
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setTitle(AppUtil.text_dialog_delfile_tittle[AppUtil.s_Language]);
		builder.setMessage(AppUtil.text_dialog_delfile_content[AppUtil.s_Language] + file.getName() + "\"?");
		builder.setPositiveButton(AppUtil.text_dialog_delfile_confirm[AppUtil.s_Language],
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						int index = -1;
						String name = file.getName().toLowerCase();
						if(name.endsWith(".jpg")) {
							index = 0;
						} else if(name.endsWith(".mp4")) {
							index = 1;
						}
						
						if(!file.delete()) {
							mToast.showToast(AppUtil.text_dialog_delfile_failed[AppUtil.s_Language]);
						}
						refresh_listView(index);
					}
				});
		builder.setNegativeButton(AppUtil.text_dialog_delfile_cancel[AppUtil.s_Language], null);
		builder.create();
		builder.show();
	}
	
	private void init_arraylist(int index) {
		if(index == 0) {
			File file_pic = new File(AppUtil.getImagePath());
			if(file_pic.isDirectory()) {
				File[] files = file_pic.listFiles();
				mArrayList_pic.clear();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if(file.getName().endsWith(AppUtil.IMG_TYPE)) {
						mArrayList_pic.add(file);
					}
				}
			}
		} else if(index == 1) {
			File file_video = new File(AppUtil.getVideoPath());
			if(file_video.isDirectory()) {
				File[] files = file_video.listFiles();
				mArrayList_video.clear();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if(file.getName().endsWith(AppUtil.VID_TYPE)) {
						mArrayList_video.add(file);
					}
				}
			}
		}
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		// TODO Auto-generated method stub
//		if(keyCode == KeyEvent.KEYCODE_BACK) {
//			Intent intent = new Intent(FileActivity.this, VideoPlayerActivity.class);
//			startActivity(intent);
//			finish();
//		}
//		
//		return super.onKeyDown(keyCode, event);
//	}
	
	class FileAdapter extends BaseAdapter {

		ArrayList<File> al;
		LayoutInflater lf;
		boolean type;
		
		public FileAdapter(Context context, ArrayList<File> al, boolean type) {
			// TODO Auto-generated constructor stub
			this.al = al;
			this.lf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.type = type;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return al.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return al.get(position);
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
			
			if(convertView == null) {
				convertView = lf.inflate(R.layout.item_file, null);
				
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.file_ico);
				holder.text = (TextView) convertView.findViewById(R.id.file_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.icon.setImageResource(type ? R.drawable.image_ico : R.drawable.video_ico);
			holder.text.setText(al.get(position).getName());
			
			return convertView;
		}

		class ViewHolder {
			ImageView icon;
			TextView text;
		}
	}
}
