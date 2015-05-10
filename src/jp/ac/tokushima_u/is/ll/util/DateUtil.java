package jp.ac.tokushima_u.is.ll.util;

import java.sql.Time;
import java.util.Calendar;

public class DateUtil {
	public static Integer getSeconds(Time t1){
		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(t1.getTime());
		int h1 = c1.get(Calendar.HOUR_OF_DAY);
		int m1 = c1.get(Calendar.MINUTE);
		int s1 = c1.get(Calendar.SECOND);
		return h1*60*60+m1*60+s1;
	}

}
