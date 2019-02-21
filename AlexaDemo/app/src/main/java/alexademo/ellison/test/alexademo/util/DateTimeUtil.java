package alexademo.ellison.test.alexademo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
    private final static String PATTERN = "yyyy/MM/dd hh:mm:ss";

    public static String getDateString(long timeInMills) {
        long expireTime = LoginManager.getExpireTime();
        Date date = new Date(expireTime);
        SimpleDateFormat df = new SimpleDateFormat(PATTERN);
        String expireText = df.format(date);
        return expireText;
    }
}
