package com.galaxy.score.utils;

public class NumberUtil {
	/**
	 * 前面补0
	 * @param number  要补充的数字
	 * @param length  补充后的长度
	 * @return
	 */
	public static String makeup0(int number,int length) {
		String rs = number + "";
		int rslength = rs.length();
		if(rslength < length) {
			for(int i=0; i < (length - rslength); i++) {
				rs = "0" + rs;
			}
		}
		return rs;
	}
	
	/**
	 * 结尾补0
	 * @param number  要补充的数字
	 * @param length  补充后的长度
	 * @return
	 */
	public static String makedown0(int number,int length) {
		String rs = number+"";
		int rslength = rs.length();
		if(rslength<length) {
			for(int i=0 ;i<(length-rslength) ;i++) {
				rs = rs+"0";
			}
		}
		return rs;
	}

	/**
	 * 字符串首字母大写
	 * @param str
	 * @return
	 */
	public static String upperCase(String str) {
		char[] ch = str.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] = (char) (ch[0] - 32);
		}
		return new String(ch);
	}
}
