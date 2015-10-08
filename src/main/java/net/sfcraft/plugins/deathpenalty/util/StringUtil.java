package net.sfcraft.plugins.deathpenalty.util;

public class StringUtil {
	/**
	 * 拼接字符串
	 * @param str
	 * @return
	 */
	public static String appendStr(Object... objects){
		StringBuffer sb = new StringBuffer();
		for (Object object : objects) {
			sb.append(object);
		}
		return sb.toString();
	}
}
