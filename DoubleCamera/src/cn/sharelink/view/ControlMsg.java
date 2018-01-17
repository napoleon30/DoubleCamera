package cn.sharelink.view;

import android.util.Log;


/**
 * @author ChenJun
 * 自定义飞行器控制协议
 *
 * BYTE[0]:数据头，固定为0X66
 * BYTE[1]:AIL——副翼：中间值0x80，左边最大为00，右边最大为0xff，即00--0x80--0xff线性变换，      
 * BYTE[2]:ELE——升降舵：中间值0x80，前最大为0xff，后最大为0x00，即0x00--0x80--0xff线性变化    
 * BYTE[3]:THR——油门：00为最小，0xff为最大；油门平时置于中间，值为0x80;                                                                                  
 * BYTE[4]:RUDD——方向舵: 中间值0x80，左转最大0x00，右转最大0xff同AIL及ELE
 * BYTE[5]:标志位, bit0：保留；bit1:一键启动(自动起飞并定高）；bit2:一键着陆；bit3:飞行器紧急停止； bit4:保留 ；bit5:无头模式；bit6:一键翻滚；bit7:一键平衡；其余bit 保留
 * BYTE[6]=(BYTE[1]^BYTE[2]^BYTE[3]^BYTE[4]^BYTE[5])&0xff;
 * BYTE[7]：数据尾，固定为0x99
 * 
 */
public class ControlMsg {
	
	private static final int MAX = 0xff;
	private static final int MID = 0x80;
	
	private static ControlMsg sControlMsg;
	
	private byte[] data = new byte[8];
	
	//0
	private static final byte head = (byte) 0x66; 			// MSG 头
	//1
	private byte roll; 	// 侧滚 副翼
	private int  roll0 = MID;
	//2
	private byte pitch; //俯仰  升降翼
	private int  pitch0 = MID;
	//3 
	private byte throttle;	// 油门
	private int throttle0 = 0;
	//4
	private byte yaw;	// 偏航 方向舵
	private int  yaw0 = MID;
	//5
	private static final int startFly_bit = 1;
	private static final int land_bit = 2;
	private static final int stop_bit = 3;
	private static final int highLimit_bit = 4;
	private static final int noHead_bit = 5;
	private static final int doRoll_bit = 6;
	private static final int balance_bit = 7;

	//5.1
	private byte startFly; // 一键启动(自动起飞并定高）
	//5.2
	private byte land;	// 一键着陆
	//5.3
	private byte stop;	// 飞行器紧急停止
	//5.4
	private byte highLimit; // 定高
	//5.5
	private byte noHead;	// 无头模式
	//5.6
	private byte doRoll;	// 一键翻滚
	//5.7
	private byte balance;	// 一键平衡
	
	//6
	private byte checksum; // 校验位BYTE[1]^BYTE[2]^BYTE[3]^BYTE[4]^BYTE[5])&0xff;
	//7
	private static final byte tail = (byte) 0x99;
	
	// 油门限幅
	private float throttle_limit = 1.0f;
	private int speed_limit;
	// 侧滚微调
	private int roll_trim = 0;
	private int pitch_trim = 0;
	private int yaw_trim = 0;
	
	/**
	 * 获取ControlMsg的一个对象
	 * @return
	 */
	public static ControlMsg getInstance() {
		synchronized (ControlMsg.class) {
			if (sControlMsg == null) {
				sControlMsg = new ControlMsg();
			}
			return sControlMsg;
		}
	}
	
	public ControlMsg() {
		// TODO Auto-generated constructor stub
		roll = (byte) (roll0 & 0xff);
		pitch = (byte) (pitch0 & 0xff);
		throttle = (byte) (throttle0 & 0xff);;
		yaw = (byte) (yaw0 & 0xff);
		
		startFly = 0;
		land = 0;
		stop = 0;
		highLimit = 1 << highLimit_bit;
		noHead = 0;
		doRoll = 0;
		balance = 0;
	}
	
	public void setData() {
		data[0] = head;
		data[1] = roll;
		data[2] = pitch;
		data[3] = throttle;
		data[4] = yaw;
		data[5] = (byte) (startFly | land | stop | highLimit | noHead | doRoll | balance);
		data[6] = (byte) ((data[1] ^ data[2] ^ data[3] ^ data[4] ^ data[5]) & 0xff);
		data[7] = tail;
	}
	
	/**
	 * @return data数组
	 */
	public byte[] getData() {
		setData();
		return data;
	}
	
	/**
	 * @param index 第index个元素
	 * @return data[index]的HEX字符串
	 */
	public String getHexData(int index) {
		return String.format("%02x", data[index]);
	}
	/**
	 * 将data数组转换成HEX字符串
	 * @param data间分割符
	 * @return 
	 */
	public String getDataHexString(String separator) {
		setData();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < data.length; i++) {
			sb.append(getHexData(i));
			if(i < data.length - 1) { 
				sb.append(separator);
			}
		}
		return sb.toString();
	}
	
	static int getCtlData(float f) {
//		int mid = MID + trim;
//		if(f > 0) {
//			tmp = (int) (mid + f * (MAX - mid));
//		} else {
//			tmp = (int) (mid + f * mid);
//		}

		int tmp = (int) (MID + f * MID);
		if(tmp > MAX) {
			tmp = MAX;
		} else if(tmp < 0) {
			tmp = 0;
		}
		return tmp;
	}
	static byte getCtlData(int dat, int trim) {
		
		int data;
		
		if(dat > MID) {
			data = (dat - MID) * (MID - trim) / MID + (MID + trim);
		} else {
			data = (dat - MID) * (MID + trim) / MID + (MID + trim);
		}
//		Log.i("data", dat + "/" + trim + "//" + data);
		return (byte) (data & 0xff);
	}

	public void setRoll(float f) {
		roll0 = getCtlData(f);
		roll = getCtlData(roll0, roll_trim);
	}
	public int getRoll() {
		return roll & 0xff;
	}

	public void setPitch(float f) {
		pitch0 = getCtlData(f);
		pitch = getCtlData(pitch0, pitch_trim);
	}
	public int getPitch() {
		return pitch & 0xff;
	}

	public void setThrottle(float f) {
//		int tmp = (int) ((f * MID) + MID);
//		if(tmp > MAX) {
//			tmp = MAX;
//		} else if(tmp < 0) {
//			tmp = 0;
//		}
//		tmp = (int) (tmp * throttle_limit);
//		throttle = (byte) (tmp & 0xff);
		throttle0 = getCtlData(f);
		throttle = (byte) ((int)(throttle0 * throttle_limit) & 0xff);
	}
	public int getThrottle() {
		return throttle & 0xff;
	}

	public void setYaw(float f) {
		yaw0 = getCtlData(f);
		yaw = getCtlData(yaw0, yaw_trim);
	}
	public int getYaw() {
		return yaw & 0xff;
	}

	public void setStartFly(int start) {
		this.startFly = (byte) (start << startFly_bit);
	}
	public int getStart() {
		return (startFly & 0xff) >> startFly_bit;
	}

	public void setLand(int land) {
		this.land = (byte) (land << land_bit);
	}
	public int getLand() {
		return (land & 0xff) >> land_bit;
	}

	public void setStop(int stop) {
		this.stop = (byte) (stop << stop_bit);
	}
	public int getStop() {
		return (stop & 0xff) >> stop_bit;
	}
	
	public void setHighLimit(int highLimit) {
		this.highLimit = (byte) (highLimit << highLimit_bit);
	}
	public int getHighLimit() {
		return (highLimit & 0xff) >> highLimit_bit;
	}

	public void setNoHead(int noHead) {
		this.noHead = (byte) (noHead << noHead_bit);
	}
	public int getNoHead() {
		return (noHead & 0xff) >> noHead_bit;
	}

	public void setDoRoll(int doRoll) {
		this.doRoll = (byte) (doRoll << doRoll_bit);
	}
	public int getDoRoll() {
		return (doRoll & 0xff) >> doRoll_bit;
	}
	
	public void setBalance(int balance) {
		this.balance = (byte) (balance << balance_bit);
	}
	public int getBalance() {
		return (balance & 0xff) >> balance_bit;
	}
	
	
	public void setSpeedLimit(int speed_limit) {
		this.speed_limit = speed_limit;
		if(speed_limit == 0) {
			this.throttle_limit = 0.3f;
		} else if(speed_limit == 1) {
			this.throttle_limit = 0.6f;
		} else if(speed_limit == 2) {
			this.throttle_limit = 1.0f;
		}
		throttle = (byte) ((int)(throttle0 * throttle_limit) & 0xff);
//		Log.i("aa", String.format("%.2f", throttle_limit));
	}
	
	public int getSpeedLimit() {
		return speed_limit;
	}
	
	public void setRoll_trim(int roll_trim) {
		this.roll_trim = roll_trim;
		roll = getCtlData(roll0, roll_trim);
	}
	public void setPitch_trim(int pitch_trim) {
		this.pitch_trim = pitch_trim;
		pitch = getCtlData(pitch0, pitch_trim);
	}
	public void setYaw_trim(int yaw_trim) {
		this.yaw_trim = yaw_trim;
		yaw = getCtlData(yaw0, yaw_trim);
	}
	
}
