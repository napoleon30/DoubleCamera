#ifndef __PacketQueue_H__
#define __PacketQueue_H__

#include <pthread.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/common.h>

typedef struct pixel_pkt {
	unsigned char *pixel;
	unsigned int size;
	unsigned int width;
	unsigned int height;
} pixelPkt_t;

typedef struct pixel_node {
	pixelPkt_t pkt;
	struct pixel_node *next;
} pixelNode_t;

typedef struct pixel_queue {
	pixelNode_t *head, *tail;
	int count;
	pthread_mutex_t queue_lock;
	pthread_cond_t  wake_cond;

} pixelQueue_t;

typedef struct picture_node {
	AVFrame *pFrame;
	struct picture_node *next;
} pictureNode_t;

typedef struct picture_queue {
	pictureNode_t *head, *tail;
	int count;
	pthread_mutex_t queue_lock;
	pthread_cond_t  wake_cond;
} pictureQueue_t;

typedef struct PictureQueue {
    AVPacketList *first_pkt, *last_pkt;
    int nb_packets;
    int size;
    pthread_mutex_t queue_lock;
    pthread_cond_t  wake_cond;
} PacketQueue;



extern void packet_queue_init(PacketQueue *q);
extern int packet_queue_put(PacketQueue *q, AVPacket pkt);
extern int packet_queue_get(PacketQueue *q, AVPacket *pkt, int block);
extern PacketQueue packetQueue;

extern void pixel_queue_init(pixelQueue_t *q);
extern int pixel_queue_put(pixelQueue_t *q, pixelPkt_t pkt);
extern int pixel_queue_get(pixelQueue_t *q, pixelPkt_t *pkt, int block);
extern pixelQueue_t pixelQueue;

#endif
