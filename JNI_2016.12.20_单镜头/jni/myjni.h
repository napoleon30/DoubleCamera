#include <jni.h>

#ifdef __cplusplus

extern "C" {
jboolean Java_cn_sharelink_view_RtspVideoView_StartRtsp(JNIEnv* env, jobject thizz, jstring url);
int Java_cn_sharelink_view_RtspVideoView_GetWidth();
int Java_cn_sharelink_view_RtspVideoView_GetHeight();
jboolean Java_cn_sharelink_view_RtspVideoView_Decoder();
jboolean Java_cn_sharelink_view_RtspVideoView_IsConnected(JNIEnv* env, jobject thizz);
jboolean Java_cn_sharelink_view_RtspVideoView_GetYUV(JNIEnv* env, jobject thizz, jbyteArray yuv_out);
void Java_cn_sharelink_view_RtspVideoView_StopRecord(JNIEnv* env, jobject thizz);
jboolean Java_cn_sharelink_view_RtspVideoView_StartRecord(JNIEnv* env, jobject thizz, jstring path);
jboolean Java_cn_sharelink_view_RtspVideoView_IsRecording(JNIEnv* env, jobject thizz);
void Java_cn_sharelink_view_RtspVideoView_Release(JNIEnv* env, jobject thizz);
}


#endif
