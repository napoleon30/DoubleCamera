#include <myjni.h>
#include <string.h>
#include <stdio.h>
#include "logcat.h"

#include "MyFFmpeg.h"

MyFFmpeg *ffmpeg = NULL;
MyFFmpeg *ff=NULL;

int Java_cn_sharelink_view_RtspVideoView_StartRtsp(JNIEnv* env, jobject thizz, jstring url,jstring trans) {
	jboolean isCopy;
	char *rtspURL = (char *)env->GetStringUTFChars(url, &isCopy);
	char *transport = (char *)env->GetStringUTFChars(trans, &isCopy);

	LOGI("%s",rtspURL);
	LOGE("Start Client !!!");
	if(ffmpeg!=NULL){
		ff=new MyFFmpeg();
		//LOGE("THIS IS FFMPEG NULL");
		ff->init(rtspURL,transport);
		return 2;
	}
	ffmpeg = new MyFFmpeg();
	ffmpeg->init(rtspURL,transport);

	return 1;

}

int Java_cn_sharelink_view_RtspVideoView_GetWidth(JNIEnv* env, jobject thizz,int index) {
	if(index==1){
		return ffmpeg->iWidth;
	}

	return ff->iWidth;
}

int Java_cn_sharelink_view_RtspVideoView_GetHeight(JNIEnv* env, jobject thizz,int index) {
	if(index==1){
		return ffmpeg->iHeight;
	}
	return ff->iHeight;
}

jboolean Java_cn_sharelink_view_RtspVideoView_Decoder(JNIEnv* env, jobject thizz,int index) {
	if(index==1){
		return ffmpeg->decoder() == 0;
	}
	return ff->decoder() == 0;
}

jboolean Java_cn_sharelink_view_RtspVideoView_IsConnected(JNIEnv* env, jobject thizz,int index) {
	if(index==1){
		return ffmpeg->isConnected;
	}
	return ff->isConnected;
}


jboolean Java_cn_sharelink_view_RtspVideoView_GetYUV(JNIEnv* env, jobject thizz, jbyteArray yuv_out,int index) {

	jboolean ret = false;

	if(index==1){
		pixelPkt_t pixelPkt;
		if(ffmpeg != NULL && pixel_queue_get(&ffmpeg->pixelQueue, &pixelPkt, 0) >= 0) {
				jbyte *pixel = env->GetByteArrayElements(yuv_out, 0);
				memcpy(pixel, pixelPkt.pixel, pixelPkt.size);
				free(pixelPkt.pixel);
				env->ReleaseByteArrayElements(yuv_out, pixel, 0);
				ret = true;
				//LOGE("THIS IS FFMPEG GETYUV");
				return ret;
			}
	}else{
		pixelPkt_t pixelPkt;
		if(ff != NULL && pixel_queue_get(&ff->pixelQueue, &pixelPkt, 0) >= 0) {
				jbyte *pixel = env->GetByteArrayElements(yuv_out, 0);
				memcpy(pixel, pixelPkt.pixel, pixelPkt.size);
				free(pixelPkt.pixel);
				env->ReleaseByteArrayElements(yuv_out, pixel, 0);
			//	LOGE("THIS IS FF GETYUV");
				ret = true;
			}
	}

	return ret;
}
jboolean Java_cn_sharelink_view_RtspVideoView_StartRecord(JNIEnv* env, jobject thizz, jstring path,int index) {
	/*if(ffmpeg->isRecord) {
		return false;
	}
	if(ff->isRecord) {
			return false;
		}*/

	jboolean isCopy;
	char *videopath = (char *)env->GetStringUTFChars(path, &isCopy);
	LOGI("video: %s", videopath);
	int ret;
	if ( index==1) {
	 ret = ffmpeg->encoder(videopath);
	}else{
	ret=ff->encoder(videopath);}
	LOGE("video: %d", ret);
	return ret < 0 ? false : true;
}

void Java_cn_sharelink_view_RtspVideoView_StopRecord(JNIEnv* env, jobject thizz,int index) {
	if(index==1){
		ffmpeg->isRecord = false;
		return ;
	}else{
	ff->isRecord = false;}
}

jboolean Java_cn_sharelink_view_RtspVideoView_IsRecording(JNIEnv* env, jobject thizz,int index) {
	if(index ==1){
		return ffmpeg->isRecord;
	}
	return ff->isRecord;
}

void Java_cn_sharelink_view_RtspVideoView_Release(JNIEnv* env, jobject thizz,int index) {
	if(index==1){
		delete ffmpeg;
		ffmpeg = NULL;
		return ;
	}
	delete ff;
	ff = NULL;
}
