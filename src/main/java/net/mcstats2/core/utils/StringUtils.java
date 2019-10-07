package net.mcstats2.core.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StringUtils {
    public static Timestamp string2Timestamp(String str_date) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(str_date);

            return new Timestamp(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return new Timestamp(System.currentTimeMillis());
        }
    }
    public static Timestamp timestampAddSeconds(Timestamp timestamp, int seconds) {
        Timestamp original = timestamp;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(original.getTime());
        cal.add(Calendar.SECOND, seconds);
        return new Timestamp(cal.getTime().getTime());
    }
}
