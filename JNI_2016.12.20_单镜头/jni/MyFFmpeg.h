#ifndef __MYFFMPEG_H_
#define __MYFFMPEG_H_

#include <jni.h>
#include <android/log.h>
#include "logcat.h"

extern "C" {
#include "MyQueue.h"
#include "unistd.h"
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>
#include <libavutil/common.h>
#include <libavutil/buffer.h>
#include <libswscale/swscale.h>
}


class MyFFmpeg {
private:
	AVCodec *pCodec;
	AVFormatContext *pFormatCtx;
	AVCodecContext *pCodecCtx;
	AVFrame *pFrame;
	struct RTPStatistics *sta;

	int videoStream;
	AVStream *i_video_stream;
	AVCodecContext *i_video_ctx;

	int *colortab;
	int *u_b_tab;
	int *u_g_tab;
	int *v_g_tab;
	int *v_r_tab;

	//short *tmp_pic=NULL;



	unsigned int *rgb_2_pix;
	unsigned int *r_2_pix;
	unsigned int *g_2_pix;
	unsigned int *b_2_pix;

	unsigned char *pixel;
	unsigned int pixel_size;
	void DeleteYUVTab();
	void CreateYUVTab_16();
	void DisplayYUV_16(unsigned int *pdst1, unsigned char *y, unsigned char *u, unsigned char *v, int width, int height, int src_ystride, int src_uvstride, int dst_ystride);
	int flush_encoder(AVFormatContext *fmt_ctx,unsigned int stream_index);
public:
	int iWidth;
	int iHeight;
	AVPicture picture;

	bool startRe;
	bool isThrow;
	bool isorder;
	bool isCollect;
	bool startDecod;
	bool startMoment;
	int IPnum;
	int lastP;
	int momentP;
	int Idecodc;

	AVFrame *avs[21];


	MyFFmpeg();
	virtual ~MyFFmpeg();
	bool isRecord;
	bool isDecoder;
	bool isConnected;
	bool isFirstFrame;

	int init(char *rtspURL);
	int decoder();
	int encoder(char *path);


};


#endif
