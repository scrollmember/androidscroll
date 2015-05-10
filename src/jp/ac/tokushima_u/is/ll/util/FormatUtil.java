package jp.ac.tokushima_u.is.ll.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FormatUtil {
	private static final SimpleDateFormat sTimeFormat = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");
	public static SimpleDateFormat getJpTimeFormat(){
		sTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT+9"));
		return sTimeFormat;
	}
	
	public static final String DATE_TIME_FORMAT_YYYYMMDDHHMMSS = "yyyyMMdd-HHmmss";

	public static String formatYYYYMMDDhhmmss(Date v){
		SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT_YYYYMMDDHHMMSS);
		return format.format(v);
	}
}
