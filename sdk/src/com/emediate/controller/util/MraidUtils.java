package com.emediate.controller.util;

import android.os.Bundle;

public class MraidUtils {
	
	private static final String CHAR_SET = "ISO-8859-1";
	
	static public String byteToHex(byte b) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}
	
	/**
	 * Does String encoding
	 * 
	 * @param str - String to be encoded
	 * @return
	 */
	public static String convert(String str) {
		try {
			final byte[] array = str.getBytes();

			StringBuffer buffer = new StringBuffer();
			for (int k = 0; k < array.length; k++) {
				if ((array[k] & 0x80) > 0) {
					buffer.append("%" + byteToHex(array[k]));
				} else {
					buffer.append((char) (array[k]));
				}
			}
			return new String(buffer.toString().getBytes(), CHAR_SET);
		} catch (Exception ex) {

		}
		return null;
	}
	
	/**
	 * Get data from bundle
	 * @param key - key to fetch data
	 * @param data - Bundle containing data
	 * @return
	 */
	public static String getData(String key,Bundle data){
		return data.getString(key);
	}		
	
}
