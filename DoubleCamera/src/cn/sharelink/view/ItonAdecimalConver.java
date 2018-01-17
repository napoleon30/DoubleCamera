package cn.sharelink.view;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class ItonAdecimalConver {
	private static final String mHexStr = "0123456789ABCDEF";
	private static final char[] mChars = mHexStr.toCharArray();

	public static String byte2HexStr(byte[] paramArrayOfByte, int paramInt) {
		StringBuilder localStringBuilder = new StringBuilder("");
		for (int i = 0; i < paramInt; i++) {
			String str = Integer.toHexString(0xFF & paramArrayOfByte[i]);
			if (str.length() == 1)
				str = "0" + str;

			localStringBuilder.append(str);
			localStringBuilder.append(" ");
		}
		return localStringBuilder.toString().toUpperCase().trim();
	}

	public static String int2HexStr(int b) {
		String str = Integer.toHexString(0xFF & b);
		if (str.length() == 1)
			str = "0" + str;
		str = str + " ";

		return str.toUpperCase();
	}

	public static boolean checkHexStr(String paramString) {
		String str = paramString.toString().trim().replace(" ", "")
				.toUpperCase();
		int j = str.length();
		int i;
		if ((j <= 1) || (j % 2 != 0)) {
			i = 0;
		} else {
			i = 0;
			while (i < j) {
				if (mHexStr.contains(str.substring(i, i + 1))) {
					i++;
					continue;
				}
				i = 0;
				break;
			}
			i = 1;
		}
		return i == 0 ? false : true;
	}

	public static byte[] hexStr2Bytes(String paramString)
			throws NumberFormatException {
		String str = paramString.trim().replace(" ", "").toUpperCase();
		int k = str.length() / 2;
		byte[] arrayOfByte = new byte[k];
		for (int j = 0; j < k; j++) {
			int i = 1 + j * 2;
			int m = i + 1;
			// try {

			arrayOfByte[j] = (byte) (0xFF & Integer.decode(
					"0x" + str.substring(j * 2, i) + str.substring(i, m))
					.intValue());
			// } catch (NumberFormatException e) {
			// // TODO: handle exception
			//
			// }
		}
		return arrayOfByte;
	}

	public static String hexStr2Str(String paramString) {
		char[] arrayOfChar = paramString.toCharArray();
		byte[] arrayOfByte = new byte[paramString.length() / 2];
		for (int i = 0; i < arrayOfByte.length; i++)
			arrayOfByte[i] = (byte) (0xFF & 16
					* mHexStr.indexOf(arrayOfChar[(i * 2)])
					+ mHexStr.indexOf(arrayOfChar[(1 + i * 2)]));
		return new String(arrayOfByte);
	}

	public static String str2HexStr(String paramString) {
		StringBuilder localStringBuilder = new StringBuilder("");
		byte[] arrayOfByte = paramString.getBytes();
		for (int i = 0; i < arrayOfByte.length; i++) {
			int j = (0xF0 & arrayOfByte[i]) >> 4;
			localStringBuilder.append(mChars[j]);
			j = 0xF & arrayOfByte[i];
			localStringBuilder.append(mChars[j]);
			localStringBuilder.append(' ');
		}
		return localStringBuilder.toString().trim();
	}


	public static String unicodeToString(String paramString) {
		int i = paramString.length() / 6;
		StringBuilder localStringBuilder = new StringBuilder();
		for (int j = 0; j < i; j++) {
			String str2 = paramString.substring(j * 6, 6 * (j + 1));
			String str1 = str2.substring(2, 4) + "00";
			str2 = str2.substring(4);
			localStringBuilder.append(new String(Character.toChars(Integer
					.valueOf(str1, 16).intValue()
					+ Integer.valueOf(str2, 16).intValue())));
		}
		return localStringBuilder.toString();
	}
	
	private static final int ENCODE_BITS = 16;;

	/*
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src byte[] data
	 * 
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * 数字字符串转ASCII码字符串
	 * 
	 * @param String
	 *            字符串
	 * @return ASCII字符串
	 */
	public static String StringToAsciiString(String content) {
		String result = "";
		int max = content.length();
		for (int i = 0; i < max; i++) {
			char c = content.charAt(i);
			String b = Integer.toHexString(c);
			result = result + b;
		}
		return result;
	}

	/**
	 * 十六进制转字符串
	 * 
	 * @param hexString
	 *            十六进制字符串
	 * @param encodeType
	 *            编码类型4：Unicode，2：普通编码
	 * @return 字符串
	 */
	public static String hexStringToString(String hexString, int encodeType) {
		String result = "";
		int max = hexString.length() / encodeType;
		for (int i = 0; i < max; i++) {
			char c = (char) hexStringToAlgorism(hexString
					.substring(i * encodeType, (i + 1) * encodeType));
			result += c;
		}
		return result;
	}

	/**
	 * 十六进制字符串装十进制
	 * 
	 * @param hex
	 *            十六进制字符串
	 * @return 十进制数值
	 */
	public static int hexStringToAlgorism(String hex) {
		hex = hex.toUpperCase();
		int max = hex.length();
		int result = 0;
		for (int i = max; i > 0; i--) {
			char c = hex.charAt(i - 1);
			int algorism = 0;
			if (c >= '0' && c <= '9') {
				algorism = c - '0';
			} else {
				algorism = c - 55;
			}
			result += Math.pow(16, max - i) * algorism;
		}
		return result;
	}

	/**
	 * 十六转二进制
	 * 
	 * @param hex
	 *            十六进制字符串
	 * @return 二进制字符串
	 */
	public static String hexStringToBinary(String hex) {
		hex = hex.toUpperCase();
		String result = "";
		int max = hex.length();
		for (int i = 0; i < max; i++) {
			char c = hex.charAt(i);
			switch (c) {
			case '0':
				result += "0000";
				break;
			case '1':
				result += "0001";
				break;
			case '2':
				result += "0010";
				break;
			case '3':
				result += "0011";
				break;
			case '4':
				result += "0100";
				break;
			case '5':
				result += "0101";
				break;
			case '6':
				result += "0110";
				break;
			case '7':
				result += "0111";
				break;
			case '8':
				result += "1000";
				break;
			case '9':
				result += "1001";
				break;
			case 'A':
				result += "1010";
				break;
			case 'B':
				result += "1011";
				break;
			case 'C':
				result += "1100";
				break;
			case 'D':
				result += "1101";
				break;
			case 'E':
				result += "1110";
				break;
			case 'F':
				result += "1111";
				break;
			}
		}
		return result;
	}

	/**
	 * ASCII码字符串转数字字符串
	 * 
	 * @param String
	 *            ASCII字符串
	 * @return 字符串
	 */
	public static String AsciiStringToString(String content) {
		String result = "";
		int length = content.length() / 2;
		for (int i = 0; i < length; i++) {
			String c = content.substring(i * 2, i * 2 + 2);
			int a = hexStringToAlgorism(c);
			char b = (char) a;
			String d = String.valueOf(b);
			result += d;
		}
		return result;
	}

	/**
	 * 将十进制转换为指定长度的十六进制字符串
	 * 
	 * @param algorism
	 *            int 十进制数字
	 * @param maxLength
	 *            int 转换后的十六进制字符串长度
	 * @return String 转换后的十六进制字符串
	 */
	public static String algorismToHEXString(int algorism, int maxLength) {
		String result = "";
		result = Integer.toHexString(algorism);

		if (result.length() % 2 == 1) {
			result = "0" + result;
		}
		return patchHexString(result.toUpperCase(), maxLength);
	}

	/**
	 * 字节数组转为普通字符串（ASCII对应的字符）
	 * 
	 * @param bytearray
	 *            byte[]
	 * @return String
	 */
	public static String bytetoString(byte[] bytearray) {
		String result = "";
		char temp;

		int length = bytearray.length;
		for (int i = 0; i < length; i++) {
			temp = (char) bytearray[i];
			result += temp;
		}
		return result;
	}

	/**
	 * 二进制字符串转十进制
	 * 
	 * @param binary
	 *            二进制字符串
	 * @return 十进制数值
	 */
	public static int binaryToAlgorism(String binary) {
		int max = binary.length();
		int result = 0;
		for (int i = max; i > 0; i--) {
			char c = binary.charAt(i - 1);
			int algorism = c - '0';
			result += Math.pow(2, max - i) * algorism;
		}
		return result;
	}

	/**
	 * 十进制转换为十六进制字符串
	 * 
	 * @param algorism
	 *            int 十进制的数字
	 * @return String 对应的十六进制字符串
	 */
	public static String algorismToHEXString(int algorism) {
		String result = "";
		result = Integer.toHexString(algorism);

		if (result.length() % 2 == 1) {
			result = "0" + result;

		}
		result = result.toUpperCase();

		return result;
	}

	/**
	 * HEX字符串前补0，主要用于长度位数不足。
	 * 
	 * @param str
	 *            String 需要补充长度的十六进制字符串
	 * @param maxLength
	 *            int 补充后十六进制字符串的长度
	 * @return 补充结果
	 */
	static public String patchHexString(String str, int maxLength) {
		String temp = "";
		for (int i = 0; i < maxLength - str.length(); i++) {
			temp = "0" + temp;
		}
		str = (temp + str).substring(0, maxLength);
		return str;
	}

	/**
	 * 将一个字符串转换为int
	 * 
	 * @param s
	 *            String 要转换的字符串
	 * @param defaultInt
	 *            int 如果出现异常,默认返回的数字
	 * @param radix
	 *            int 要转换的字符串是什么进制的,如16 8 10.
	 * @return int 转换后的数字
	 */
	public static int parseToInt(String s, int defaultInt, int radix) {
		int i = 0;
		try {
			i = Integer.parseInt(s, radix);
		} catch (NumberFormatException ex) {
			i = defaultInt;
		}
		return i;
	}

	/**
	 * 将一个十进制形式的数字字符串转换为int
	 * 
	 * @param s
	 *            String 要转换的字符串
	 * @param defaultInt
	 *            int 如果出现异常,默认返回的数字
	 * @return int 转换后的数字
	 */
	public static int parseToInt(String s, int defaultInt) {
		int i = 0;
		try {
			i = Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			i = defaultInt;
		}
		return i;
	}

	/**
	 * 十六进制字符串转为Byte数组,每两个十六进制字符转为一个Byte
	 * 
	 * @param hex
	 *            十六进制字符串
	 * @return byte 转换结果
	 */
	public static byte[] hexStringToByte(String hex) {
		int max = hex.length() / 2;
		byte[] bytes = new byte[max];
		String binarys =hexStringToBinary(hex);
		for (int i = 0; i < max; i++) {
			bytes[i] = (byte) binaryToAlgorism(binarys.substring(
					i * 8 + 1, (i + 1) * 8));
			if (binarys.charAt(8 * i) == '1') {
				bytes[i] = (byte) (0 - bytes[i]);
			}
		}
		return bytes;
	}

	/**
	 * 十六进制串转化为byte数组
	 * 
	 * @return the array of byte
	 */
	public static final byte[] hex2byte(String hex)
			throws IllegalArgumentException {
		if (hex.length() % 2 != 0) {
			throw new IllegalArgumentException();
		}
		char[] arr = hex.toCharArray();
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
			String swap = "" + arr[i++] + arr[i];
			int byteint = Integer.parseInt(swap, 16) & 0xFF;
			b[j] = new Integer(byteint).byteValue();
		}
		return b;
	}

	/**
	 * 字节数组转换为十六进制字符串
	 * 
	 * @param b
	 *            byte[] 需要转换的字节数组
	 * @return String 十六进制字符串
	 */
	public static final String byte2hex(byte b[]) {
		if (b == null) {
			throw new IllegalArgumentException(
					"Argument b ( byte array ) is null! ");
		}
		String hs = "";
		String stmp = "";
		StringBuffer buffer = new StringBuffer();
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xff);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		String string = hs.toUpperCase().toString();
		String str1 = null;
		for (int j = 0; j < b.length; j++) {
			str1 = string.substring((j * 2), (j + 1) * 2);
			buffer.append(" " + str1);
		}

		return "[" + buffer.toString() + " ]";
	}

	public static String strToUnicode(String strText) throws Exception {
		char c;
		StringBuilder str = new StringBuilder();
		int intAsc;
		String strHex;
		for (int i = 0; i < strText.length(); i++) {
			c = strText.charAt(i);
			intAsc = (int) c;
			strHex = Integer.toHexString(intAsc);
			if (intAsc > 128)
				str.append("\\u" + strHex);
			else
				// 低位在前面补00
				str.append("\\u00" + strHex);
		}
		return str.toString();
	}

	public static int byte2Int(byte[] b) {
		int intValue = 0, tempValue = 0xFF;
		for (int i = 0; i < b.length; i++) {
			intValue += (b[i] & tempValue) << (8 * (3 - i));
			// System.out.print(Integer.toBinaryString(intValue)+" ");
		}
		return intValue;
	}

	public static int[] convertByteArrToIntArr(byte[] byteArr) {

		int remained = 0;
		int intNum = 0;

		remained = byteArr.length % 4;
		if (remained != 0) {
			throw new RuntimeException();
		}

		// 把字节数组转化为int[]后保留的个数.
		intNum = byteArr.length / 4;

		//
		int[] intArr = new int[intNum];

		int ch1, ch2, ch3, ch4;
		for (int j = 0, k = 0; j < intArr.length; j++, k += 4) {

			ch1 = byteArr[k];
			ch2 = byteArr[k + 1];
			ch3 = byteArr[k + 2];
			ch4 = byteArr[k + 3];

			// 以下内容用于把字节的8位, 不按照正负, 直接放到int的后8位中.
			if (ch1 < 0) {
				ch1 = 256 + ch1;
			}
			if (ch2 < 0) {
				ch2 = 256 + ch2;
			}
			if (ch3 < 0) {
				ch3 = 256 + ch3;
			}
			if (ch4 < 0) {
				ch4 = 256 + ch4;
			}

			intArr[j] = (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
		}

		return intArr;
	}

	public static byte[] convertIntArrToByteArr(int[] intArr) {

		int byteNum = intArr.length * 4;
		byte[] byteArr = new byte[byteNum];

		int curInt = 0;
		for (int j = 0, k = 0; j < intArr.length; j++, k += 4) {
			curInt = intArr[j];
			byteArr[k] = (byte) ((curInt >>> 24) & 0xFF);
			byteArr[k + 1] = (byte) ((curInt >>> 16) & 0xFF);
			byteArr[k + 2] = (byte) ((curInt >>> 8) & 0xFF);
			byteArr[k + 3] = (byte) ((curInt >>> 0) & 0xFF);
		}

		return byteArr;

	}

	// byteToInt
	public static int byte2int(byte b[], int offset) {
		return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8
				| (b[offset + 1] & 0xff) << 16 | (b[offset] & 0xff) << 24;
	}

	public static int bytesToInt(byte[] intByte) {

		int fromByte = 0;

		for (int i = 0; i < 2; i++) {
			int n = (intByte[i] < 0 ? (int) intByte[i] + 256 : (int) intByte[i]) << (8 * i);

			System.out.println(n);

			fromByte += n;
		}
		return fromByte;
	}

	public static int[] bytesToInts(byte[] src, int len) {
		int[] dst = new int[len];
		for (int i = 0; i < len; i++) {
			dst[i] = (int) src[i] & 0xFF;
		}
		return dst;

	}

	public int[] convert(byte buf[]) {
		int intArr[] = new int[buf.length / 4];
		int offset = 0;
		for (int i = 0; i < intArr.length; i++) {
			intArr[i] = (buf[3 + offset] & 0xFF)
					| ((buf[2 + offset] & 0xFF) << 8)
					| ((buf[1 + offset] & 0xFF) << 16)
					| ((buf[0 + offset] & 0xFF) << 24);
			offset += 4;
		}
		return intArr;
	}

	public static int[] stringToInts(String dist) {
		int len = dist.length();
		int[] ints = new int[ENCODE_BITS * len];
		for (int i = 0; i < len; i++) {
			char[] cs = charToInts(dist.charAt(i)).toCharArray();
			int j = i * ENCODE_BITS;
			for (int k = j; k < j + ENCODE_BITS; k++) {
				ints[k] = Integer.parseInt(cs[k - j] + "");
			}
		}
		return ints;
	}

	private static String charToInts(char c) {
		String bin = Integer.toBinaryString(c);
		int len = ENCODE_BITS - bin.length();
		for (int i = 1; i <= len; i++) {
			bin = "0" + bin;
		}
		return bin;
	}

	public static byte[] integersToBytes(int[] values) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		for (int i = 0; i < values.length; ++i) {
			try {
				dos.writeInt(values[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return baos.toByteArray();
	}

	public static boolean judgeEqual(byte[] bt1, byte[] bt2) {
		int length = bt1.length;
		if (length != bt2.length) {
			return false;
		}
		for (int i = 0; i < 14; i++) {
			if (bt1[i] != bt2[i]) {
				return true;
			}
		}
		return false;
	}

	public static String getHexString(String s) {
		byte[] buf = s.getBytes();

		StringBuffer sb = new StringBuffer();

		for (byte b : buf) {
			sb.append(String.format("%x", b));
		}

		return sb.toString();
	}
}