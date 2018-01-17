/*
 * FFmpeg.cpp
 *
 */
#include "MyFFmpeg.h"

#define CODEC_FLAG_GLOBAL_HEADER  0x00400000

MyFFmpeg::MyFFmpeg() {
	pFormatCtx = NULL;
	pCodecCtx = NULL;
	pFrame = NULL;
	isRecord = false;
	isConnected = false;
	startRe=false;
	isThrow=false;
	isorder=false;
	startDecod=false;
	isCollect=false;
	IPnum=0;
	lastP=0;
	momentP=0;
	Idecodc=0;
	packet_queue_init(&packetQueue);
	pixel_queue_init(&pixelQueue);
}

MyFFmpeg::~MyFFmpeg() {
	if(pCodecCtx != NULL) {
		avcodec_close(pCodecCtx);
		pCodecCtx = NULL;
	}

	if(pFormatCtx != NULL) {
		avformat_close_input(&pFormatCtx);
	}
	if(pFrame != NULL) {
		av_free(pFrame);
		pFrame = NULL;
	}

	while(pixel_queue_get(&pixelQueue, NULL, 0) == 0); // 释放掉队列中的所有数据

	DeleteYUVTab();
	LOGD("THIS IS FFMPEG");
}



int MyFFmpeg::init(char *rtspURL,char * transport) {

	LOGE("THIS IS INIT");
	if (pCodecCtx != NULL) {
		//avcodec_close该函数释放AVCodecContext中有关的变量，并且调用了AVCodec的close()关闭了解码器
		avcodec_close(pCodecCtx);
		pCodecCtx = NULL;
	}

	if (pFrame != NULL) {
		//AVFrame实例由av_frame_free()释放
		av_frame_free(&pFrame);
	}

	if (pFormatCtx != NULL) {
		avformat_free_context(pFormatCtx);
	}

	//avcodec_register_all();

	//1  注册所有容器格式和CODEC
	av_register_all();

	avformat_network_init();

	pFormatCtx = avformat_alloc_context();
	//pFormatCtx->probesize=1024;
	pFormatCtx->flags |= AVFMT_FLAG_NOBUFFER;
	//	pFormatCtx->flags=AVFMT_FLAG_NONBLOCK;
	//	pFormatCtx->flags=AVFMT_FLAG_GENPTS;
	//	pFormatCtx->flags=AVFMT_GENERIC_INDEX;
	/*	pFormatCtx->interrupt_callback.callback = interrupt_cb;//注册回调函数
	 pFormatCtx->interrupt_callback.opaque = pFormatCtx;*/

	AVDictionary* options = NULL;
	av_dict_set(&options, "rtsp_transport", transport, 0);
	LOGE("transport modle  \"%s\"", transport);
	//2   这个api读出文件的头部信息，并做demux解复用
	if (avformat_open_input(&pFormatCtx, rtspURL, NULL, &options) < 0) {
		if (pFormatCtx) {
			//在程序结束之前必须调用 avformat_free_context(AVFormatContext *s);对结构体变量进行销毁（内存释放
			avformat_free_context(pFormatCtx);
		}
		LOGE("Unable to open \"%s\"", rtspURL);
		return -1;
	}
	LOGE("avformat_find_stream_info 开始执行 ");

	AVDictionary* pOptions = NULL;
	pFormatCtx->probesize = 100 * 1024;//8   64  100
	pFormatCtx->max_analyze_duration = 5 * AV_TIME_BASE;//5   62   5
	//3  该函数可以读取一部分视音频数据并且获得一些相关的信息，正常执行后返回值大于0
	if (avformat_find_stream_info(pFormatCtx, &pOptions) < 0) {
		avformat_close_input(&pFormatCtx);
		LOGE("Unable to get stream information");
		return -1;
	}
	LOGE("avformat_find_stream_info 执行完成 ");
	//	av_dump_format(pFormatCtx, 0, rtspURL, 0);

	//视屏字节流
	videoStream = -1;
	for (int i = 0; i < pFormatCtx->nb_streams; i++) {
		// 4  穷举所有的枚举找到AVMEDIA_TYPE_VIDEO
		if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
			videoStream = i;
			break;
		}
	}
	//LOGD("havsMissedPackets==\"%d\"",havsMissedPackets);

	if (videoStream == -1) {
		LOGE("Unable to find video stream");
		return -1;
	}

	pCodecCtx = pFormatCtx->streams[videoStream]->codec;

	iWidth = pCodecCtx->width;
	iHeight = pCodecCtx->height;
	LOGI(
			"video size : width=%d height=%d \n", pCodecCtx->width, pCodecCtx->height);
	pixel_size = iWidth * iHeight * 4;

	//5    找到对应的解码器
	pCodec = avcodec_find_decoder(AV_CODEC_ID_H264);
	if (pCodec == NULL) {
		LOGE("Unsupported");
		return -1;
	}

	//6  avcodec_open2该函数用于初始化一个视音频编解码器的AVCodecContext，打开解码器
	if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
		LOGE("Unable to open codec");
		return -1;
	}


	//avcodec_alloc_frame初始化AVFrame的时候AVFrame为空，元素data和linesize均为空
	//7  为解码器分配内存
	pFrame = av_frame_alloc();
	//picture= malloc(sizeof(AVFrame));av_frame_alloc();avcodec_alloc_frame();

	if (pFrame == NULL) {
		LOGE("avcodec_alloc_frame error");
		return -1;
	}


	CreateYUVTab_16();

	isConnected = true;
	isFirstFrame = true;

	LOGE("this is init");

	return 0;

}

int MyFFmpeg::decoder() {

	int frameFinished = 0;

	AVPacket packet;

	//从音频和视频流中读取出基本数据流packet
	//8  不停地从码流中提取出帧数据
	//LOGD("pFormatCtx size \"%d\"", pFormatCtx->max_picture_buffer);
	if (av_read_frame(pFormatCtx, &packet) < 0) {
		LOGD("THIS IS INIT FF");
		return -1;
	}

	if (packet.flags == AV_PKT_FLAG_KEY && !startDecod) {
		startDecod = true;
	}



	if (startDecod) {
		if (packet.stream_index == videoStream) {
			int pktSize = packet.size;

			while (pktSize > 0) {
				int gotframe = 0;
				//黄色是+
				//然后将packet送到avcodec_decode_video2()和相对应的api进行解码s
				//9  判断帧的类型，对于视频帧调用avcodec_decode_video2
				//LOGD("AVPacket pts \"%d\"",packet.pts);

				int len = avcodec_decode_video2(pCodecCtx, pFrame, &gotframe,
						&packet);

				if (pFrame->pict_type == 1) {
					startRe = true;
				}

				if (startRe) {
					//		IPnum=IPnum+1;
					lastP = momentP;
					momentP = pFrame->coded_picture_number;
				}

				LOGD(
						"pFrame->coded_picture_number\"%d\"", pFrame->coded_picture_number);

				if (momentP == 1) {
					startMoment = true;
				}

				if (startMoment) {
					if (momentP - lastP != 1) {
						isThrow = true;
						break;
					}
				}


				if (len < 0) {
					LOGE("decode video error, skip packet");
					break;
				}

				if (isThrow && pFrame->pict_type == AV_PICTURE_TYPE_I) {

					//	IPnum=1;
					isThrow = false;
					LOGD("THIS IS ITHROW");
				}

				if (isThrow) {
					LOGD("THIS IS THROW P");
					break;
				}

				if (gotframe) {
					//为pixel分配内存，内存大小为pixel_size
					pixel = (unsigned char *) malloc((pixel_size));
					DisplayYUV_16((unsigned int*) pixel, pFrame->data[0],
							pFrame->data[1], pFrame->data[2], iWidth, iHeight,
							pFrame->linesize[0], pFrame->linesize[1], iWidth);
					pixelPkt_t pixelPkt;
					pixelPkt.pixel = pixel;
					pixelPkt.size = pixel_size;
					pixel_queue_put(&pixelQueue, pixelPkt);
				}
				av_frame_unref(pFrame);

				if (len == 0) {
					LOGD("len =  0 ");
					break;
				}
				pktSize -= len;
			}
		}
	}

	if (isRecord) {
		packet_queue_put(&packetQueue, packet);
	} else {
		av_packet_unref(&packet);
	}

	return 0;
}



int MyFFmpeg::encoder(char *filename) {

	int ret;
	AVFormatContext *o_fmt_ctx;
	AVStream *o_video_stream;
	AVStream *i_video_stream;
	int num = 1;
	int o_stream_index;

	if(!isConnected) {
		return -1;
	}

	avformat_alloc_output_context2(&o_fmt_ctx, NULL, NULL, filename);
	if(o_fmt_ctx == NULL) {
		LOGE("Could not create output context");
		ret = -1;
		goto exit;
	}
	o_video_stream = avformat_new_stream(o_fmt_ctx, NULL);
	i_video_stream = pFormatCtx->streams[videoStream];

    AVCodecContext *c;
	c = o_video_stream->codec;
	//c->bit_rate = 20000;
	avcodec_copy_context(c, i_video_stream->codec);
	o_video_stream->time_base.num = i_video_stream->time_base.num;
	o_video_stream->time_base.den = i_video_stream->time_base.den;

	LOGI("(%d, %d)", o_video_stream->time_base.num, o_video_stream->time_base.den);

	o_video_stream->codec->codec_tag = 0;
	o_stream_index = o_video_stream->index;
	if (o_fmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
		o_video_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;

	av_dump_format(o_fmt_ctx, 0, filename, 1);

	ret = avio_open(&o_fmt_ctx->pb, filename, AVIO_FLAG_WRITE);
    if(ret < 0) {
    	LOGE("Error, avio_open \"%s\"", filename);
    	ret = -1;
    	goto err_avio_open;
    }

    ret = avformat_write_header(o_fmt_ctx, NULL);
    if(ret < 0) {
    	LOGE("Error, avformat_write_header");
    	ret = -1;
    	goto err_avformat_write_header;
    }

	isRecord = true;

	while (1)
	{
		AVPacket i_pkt;
		i_pkt.data = NULL;
		i_pkt.size = 0;
		av_init_packet(&i_pkt);
		if(packet_queue_get(&packetQueue, &i_pkt, 0) >= 0) {
			/*
			 * pts and dts should increase monotonically
			 * pts should be >= dts
			 */
			i_pkt.flags |= AV_PKT_FLAG_KEY;

			i_pkt.pts = av_rescale_q_rnd(i_pkt.pts, i_video_stream->time_base, o_video_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
			i_pkt.dts = av_rescale_q_rnd(i_pkt.dts, i_video_stream->time_base, o_video_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
			i_pkt.duration = av_rescale_q(i_pkt.duration, i_video_stream->time_base, o_video_stream->time_base);
			i_pkt.pos = -1;
			i_pkt.stream_index = o_stream_index;

			LOGW("frame %d", num++);
			av_interleaved_write_frame(o_fmt_ctx, &i_pkt);

			av_free_packet(&i_pkt);
		} else {
			if(!isRecord) {
				break;
			}
			usleep(10000); // 睡眠10ms
		}
	}

	ret = 0;
	av_write_trailer(o_fmt_ctx);

err_avformat_write_header:
	avio_close(o_fmt_ctx->pb);

err_avio_open:
	avcodec_close(o_fmt_ctx->streams[0]->codec);
	av_freep(&o_fmt_ctx->streams[0]->codec);
	av_freep(&o_fmt_ctx->streams[0]);

	av_free(o_fmt_ctx);
exit:
	return ret;
}

void MyFFmpeg::DeleteYUVTab() {
	av_free(colortab);
	av_free(rgb_2_pix);
}

void MyFFmpeg::CreateYUVTab_16()
{
	int i;
	int u, v;
//  最大 iWidth * iHeight * 16bits
//	tmp_pic = (short*)av_malloc(iWidth*iHeight*2);

	colortab = (int *)av_malloc(4*256*sizeof(int));
	u_b_tab = &colortab[0*256];
	u_g_tab = &colortab[1*256];
	v_g_tab = &colortab[2*256];
	v_r_tab = &colortab[3*256];

	for (i=0; i<256; i++)
	{
		u = v = (i-128);

		u_b_tab[i] = (int) ( 1.772 * u);
		u_g_tab[i] = (int) ( 0.34414 * u);
		v_g_tab[i] = (int) ( 0.71414 * v);
		v_r_tab[i] = (int) ( 1.402 * v);
	}

	rgb_2_pix = (unsigned int *)av_malloc(3*768*sizeof(unsigned int));

	r_2_pix = &rgb_2_pix[0*768];
	g_2_pix = &rgb_2_pix[1*768];
	b_2_pix = &rgb_2_pix[2*768];

	for(i=0; i<256; i++)
	{
		r_2_pix[i] = 0;
		g_2_pix[i] = 0;
		b_2_pix[i] = 0;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+256] = (i & 0xF8) << 8;
		g_2_pix[i+256] = (i & 0xFC) << 3;
		b_2_pix[i+256] = (i ) >> 3;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+512] = 0xF8 << 8;
		g_2_pix[i+512] = 0xFC << 3;
		b_2_pix[i+512] = 0x1F;
	}

	r_2_pix += 256;
	g_2_pix += 256;
	b_2_pix += 256;
}

void MyFFmpeg::DisplayYUV_16(unsigned int *pdst1, unsigned char *y, unsigned char *u, unsigned char *v, int width, int height, int src_ystride, int src_uvstride, int dst_ystride)
{
	int i, j;
	int r, g, b, rgb;

	int yy, ub, ug, vg, vr;

	unsigned char* yoff;
	unsigned char* uoff;
	unsigned char* voff;

	unsigned int* pdst=pdst1;

	int width2 = width/2;
	int height2 = height/2;

	if(width2>iWidth/2)
	{
		width2=iWidth/2;

		y+=(width-iWidth)/4*2;
		u+=(width-iWidth)/4;
		v+=(width-iWidth)/4;
	}

	if(height2>iHeight)
		height2=iHeight;

	for(j=0; j<height2; j++) // 涓�娆�2x2鍏卞洓涓儚绱�
	{
		yoff = y + j * 2 * src_ystride;
		uoff = u + j * src_uvstride;
		voff = v + j * src_uvstride;

		for(i=0; i<width2; i++)
		{
			yy  = *(yoff+(i<<1));
			ub = u_b_tab[*(uoff+i)];
			ug = u_g_tab[*(uoff+i)];
			vg = v_g_tab[*(voff+i)];
			vr = v_r_tab[*(voff+i)];

			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst[(j*dst_ystride+i)] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);

			yy = *(yoff+(i<<1)+src_ystride);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+src_ystride+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst [((2*j+1)*dst_ystride+i*2)>>1] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);
		}
	}
}

