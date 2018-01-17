package cn.sharelink.view;

import java.util.HashMap;

import cn.sharelink.DoubleCameras.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;


public class PlayVoice {
	
	private SoundPool soundPool;
	private HashMap<Integer, Integer> hmSoundPool;
	private boolean isFinishedLoad = false;
	private Context context;
	
	public PlayVoice(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC,0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				// TODO Auto-generated method stub
				isFinishedLoad = true;
			}
		});
		hmSoundPool = new HashMap<Integer,Integer>();//创建HashMap对象 
		hmSoundPool.put(0, soundPool.load(context, R.raw.trim_voice0, 0));
		hmSoundPool.put(1, soundPool.load(context, R.raw.trim_voice1, 0));
		hmSoundPool.put(2, soundPool.load(context, R.raw.roll_voice, 0));
		
	}
	
	public void play(int sound_index) {
		if(!isFinishedLoad || soundPool == null) {
			return;
		}
		AudioManager am = (AudioManager) context
				.getSystemService(context.AUDIO_SERVICE);// 实例化AudioManager对象
		float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
		float audioCurrentVolumn = am
				.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
		float volumnRatio = audioCurrentVolumn / audioMaxVolumn;
		soundPool.play(hmSoundPool.get(sound_index), // 播放的音乐id
				volumnRatio, // 左声道音量
				volumnRatio, // 右声道音量
				1, // 优先级，0为最低
				0, // 循环次数，0无不循环，-1无永远循环
				1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
				);
	}
	
	public void release() {
		if(soundPool != null){
			soundPool.release();
		}
	}
}
