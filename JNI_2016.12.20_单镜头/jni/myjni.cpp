#include <myjni.h>
#include <string.h>
#include <stdio.h>
#include "logcat.h"

#include "MyFFmpeg.h"

MyFFmpeg *ffmpeg = NULL;


jboolean Java_cn_sharelink_view_RtspVideoView_StartRtsp(JNIEnv* env, jobject thizz, jstring url) {
	jboolean isCopy;
	char *rtspURL = (char *)env->GetStringUTFChars(url, &isCopy);
	LOGI("%s",rtspURL);
	LOGE("Start Client !!!");

	packet_queue_init(&packetQueue);
	pixel_queue_init(&pixelQueue);
	if(ffmpeg!=NULL){
		ffmpeg=NULL;
		LOGE("THIS IS FFMPEG NULL");
	}
	ffmpeg = new MyFFmpeg();


	return ffmpeg->init(rtspURL) == 0;

}

int Java_cn_sharelink_view_RtspVideoView_GetWidth() {
	return ffmpeg->iWidth;
}

int Java_cn_sharelink_view_RtspVideoView_GetHeight() {
	return ffmpeg->iHeight;
}

jboolean Java_cn_sharelink_view_RtspVideoView_Decoder() {
	return ffmpeg->decoder() == 0;
}

jboolean Java_cn_sharelink_view_RtspVideoView_IsConnected(JNIEnv* env, jobject thizz) {
	return ffmpeg->isConnected;
}


jboolean Java_cn_sharelink_view_RtspVideoView_GetYUV(JNIEnv* env, jobject thizz, jbyteArray yuv_out) {
	jboolean ret = false;

	pixelPkt_t pixelPkt;

	if(ffmpeg != NULL && pixel_queue_get(&pixelQueue, &pixelPkt, 0) >= 0) {
		jbyte *pixel = env->GetByteArrayElements(yuv_out, 0);
		memcpy(pixel, pixelPkt.pixel, pixelPkt.size);
		free(pixelPkt.pixel);
		env->ReleaseByteArrayElements(yuv_out, pixel, 0);
		ret = true;
	}

	return ret;
}
jboolean Java_cn_sharelink_view_RtspVideoView_StartRecord(JNIEnv* env, jobject thizz, jstring path) {
	if(ffmpeg->isRecord) {
		return false;
	}
	jboolean isCopy;
	char *videopath = (char *)env->GetStringUTFChars(path, &isCopy);
	LOGI("video: %s", videopath);
	int ret = ffmpeg->encoder(videopath);

	return ret < 0 ? false : true;
}

void Java_cn_sharelink_view_RtspVideoView_StopRecord(JNIEnv* env, jobject thizz) {
	ffmpeg->isRecord = false;
}

jboolean Java_cn_sharelink_view_RtspVideoView_IsRecording(JNIEnv* env, jobject thizz) {
	return ffmpeg->isRecord;
}

void Java_cn_sharelink_view_RtspVideoView_Release(JNIEnv* env, jobject thizz) {

	delete ffmpeg;
	ffmpeg = NULL;
}
