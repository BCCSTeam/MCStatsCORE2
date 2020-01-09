package net.mcstats2.core.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        } else {
            int sz = str.length();

            for(int i = 0; i < sz; ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }


    private static final Random random = new Random();
    private static final String CHARS = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ234567890";
    public static String randomString(int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return token.toString();
    }


    public static int getExpire(String timestr) {
        if (timestr.equals("0"))
            return 0;

        Pattern tsr = Pattern.compile("([0-9]+[yMwdhms])");
        Matcher m = tsr.matcher(timestr);

        int time = 0;
        int i = 0;
        while (m.find()) {
            i++;
            String group = m.group(0);
            String key = group.substring(group.length() - 1);
            int value = Integer.parseInt(group.substring(0, group.length() - 1));

            switch (key) {
                case "y":
                    time += value * 12 * 4 * 7 * 24 * 60 * 60;
                    break;
                case "M":
                    time += value * 4 * 7 * 24 * 60 * 60;
                    break;
                case "w":
                    time += value * 7 * 24 * 60 * 60;
                    break;
                case "d":
                    time += value * 24 * 60 * 60;
                    break;
                case "h":
                    time += value * 60 * 60;
                    break;
                case "m":
                    time += value * 60;
                    break;
                case "s":
                    time += value;
                    break;
                default:
                    break;
            }
        }
        return i == 0 ? -1 : time;
    }

    public static String replace(String message, HashMap<String, Object> replace) {
        for (Map.Entry r : replace.entrySet()) {
            if (r.getKey() != null)
                message = message.replace("%" + r.getKey().toString() + "%", r.getValue() != null ? r.getValue().toString() : "?");
        }

        return message;
    }
}
