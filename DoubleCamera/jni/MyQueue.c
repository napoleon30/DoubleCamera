
#include "MyQueue.h"

#define MAX_PIXEL_COUNT 20

void pixel_queue_init(pixelQueue_t *q) {
	memset(q, 0, sizeof(pixelQueue_t));

    pthread_mutex_init(&q->queue_lock,NULL);
    pthread_cond_init(&q->wake_cond, NULL);
}

int pixel_queue_put(pixelQueue_t *q, pixelPkt_t pkt) {

	pixelNode_t *temp_node;

	temp_node = (pixelNode_t *)malloc(sizeof(pixelNode_t));

	temp_node->pkt = pkt;
	temp_node->next = NULL;

	pthread_mutex_lock(&q->queue_lock);

	if(!q->tail) {
		q->head = temp_node;
	} else {
		q->tail->next = temp_node;
	}
	q->tail = temp_node;
	q->count++;

    pthread_cond_signal(&q->wake_cond);
    pthread_mutex_unlock(&q->queue_lock);

	if(q->count > MAX_PIXEL_COUNT) { // 避免队列数据堆积过多
		pixel_queue_get(q, NULL, 0);
		//LOGD("THIS IS PIXEL QUEUE");
	}

    return 0;
}

int pixel_queue_get(pixelQueue_t *q, pixelPkt_t *pkt, int block) {
	pixelNode_t *temp_node;
	int ret = -1;

	if(block == 0 && q->count == 0) {
		return -1;
	}

	pthread_mutex_lock(&q->queue_lock);

	while(1) {
		temp_node = q->head;
		if(temp_node) {
			q->head = temp_node->next;
			if(!q->head) {
				q->tail = NULL;
			}
			q->count--;

			if(pkt != NULL) {
				*pkt = temp_node->pkt;
			} else {
				free(temp_node->pkt.pixel);
			}

			free(temp_node);

			pthread_mutex_unlock(&q->queue_lock);
			ret = 0;
			break;
		} else {
			pthread_cond_wait(&q->wake_cond, &q->queue_lock);
		}
 	}
	return ret;
}

PacketQueue packetQueue;

void packet_queue_init(PacketQueue *q) {
    memset(q, 0, sizeof(PacketQueue));
    pthread_mutex_init(&q->queue_lock,NULL);
    pthread_cond_init(&q->wake_cond, NULL);
}
int packet_queue_put(PacketQueue *q, AVPacket pkt) {

    AVPacketList *pkt1;
    if(av_dup_packet(&pkt) < 0) {
        return -1;
    }
    pkt1 = (AVPacketList*)av_malloc(sizeof(AVPacketList));
    if (!pkt1)
        return -1;
    pkt1->pkt = pkt;
    pkt1->next = NULL;

    pthread_mutex_lock(&q->queue_lock);

    if (!q->last_pkt)
        q->first_pkt = pkt1;
    else
        q->last_pkt->next = pkt1;
    q->last_pkt = pkt1;
    q->nb_packets++;
    q->size += pkt1->pkt.size;

    pthread_cond_signal(&q->wake_cond);
    pthread_mutex_unlock(&q->queue_lock);
    return 0;
}
int packet_queue_get(PacketQueue *q, AVPacket *pkt, int block)
{
    AVPacketList *pkt1;

    int ret = -1;

    if(block == 0 && q->nb_packets == 0) {
    	return -1;
    }

    pthread_mutex_lock(&q->queue_lock);
    for(;;) {
        pkt1 = q->first_pkt;
        if (pkt1) {
            q->first_pkt = pkt1->next;
            if (!q->first_pkt)
                q->last_pkt = NULL;
            q->nb_packets--;
            q->size -= pkt1->pkt.size;
            *pkt = pkt1->pkt;
            av_free(pkt1);
//            LOGI("%d", q->size);
            pthread_mutex_unlock(&q->queue_lock);
            ret = 0;
            break;
        } else {
        	pthread_cond_wait(&q->wake_cond, &q->queue_lock);
        }
    }

    return ret;
}
