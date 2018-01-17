#include <jni.h>

#ifdef __cplusplus

extern "C" {
int Java_cn_sharelink_view_RtspVideoView_StartRtsp(JNIEnv* env, jobject thizz, jstring url,jstring trans);
int Java_cn_sharelink_view_RtspVideoView_GetWidth(JNIEnv* env, jobject thizz,int index);
int Java_cn_sharelink_view_RtspVideoView_GetHeight(JNIEnv* env, jobject thizz,int index);
jboolean Java_cn_sharelink_view_RtspVideoView_Decoder(JNIEnv* env, jobject thizz,int index);
jboolean Java_cn_sharelink_view_RtspVideoView_IsConnected(JNIEnv* env, jobject thizz,int index);
jboolean Java_cn_sharelink_view_RtspVideoView_GetYUV(JNIEnv* env, jobject thizz, jbyteArray yuv_out,int index);
void Java_cn_sharelink_view_RtspVideoView_StopRecord(JNIEnv* env, jobject thizz,int index);
jboolean Java_cn_sharelink_view_RtspVideoView_StartRecord(JNIEnv* env, jobject thizz, jstring path,int index);
jboolean Java_cn_sharelink_view_RtspVideoView_IsRecording(JNIEnv* env, jobject thizz,int index);
void Java_cn_sharelink_view_RtspVideoView_Release(JNIEnv* env, jobject thizz,int index);
}


#endif
