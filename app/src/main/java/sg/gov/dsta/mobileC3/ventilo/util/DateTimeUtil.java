package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.Context;
//import android.net.ParseException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.qap.ctimelineview.R.plurals;
import org.qap.ctimelineview.R.string;

import sg.gov.dsta.mobileC3.ventilo.R;

public class DateTimeUtil {

    public static final String TAG = "DateTimeUtil";
    public static final String DATE_TIME_FORMAT = "EEE MMM d HH:mm:ss zz yyyy";
    private static Context mContext;

    public static Date getSpecifiedDateBySecond(int second) {
        return getSpecifiedDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.AM_PM),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), second);
    }

    public static Date getSpecifiedDateByMinute(int minute) {
        return getSpecifiedDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.AM_PM),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY), minute, Calendar.getInstance().get(Calendar.SECOND));
    }

    public static Date getSpecifiedDateByHourOfDay(int hour) {
        return getSpecifiedDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.AM_PM),
                hour, Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND));
    }

    public static Date getSpecifiedDateByAmOrPm(int amOrPm) {
        return getSpecifiedDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH), amOrPm, Calendar.getInstance().get(Calendar.HOUR),
                Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND));
    }

    public static Date getSpecifiedDateByDayOfMonth(int day) {
        return getSpecifiedDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                day, Calendar.getInstance().get(Calendar.AM_PM), Calendar.getInstance().get(Calendar.HOUR),
                Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND));
    }

    public static Date getSpecifiedDateByMonth(int month) {
        return getSpecifiedDate(Calendar.getInstance().get(Calendar.YEAR), month,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.AM_PM),
                Calendar.getInstance().get(Calendar.HOUR), Calendar.getInstance().get(Calendar.MINUTE),
                Calendar.getInstance().get(Calendar.SECOND));
    }

    public static Date getSpecifiedDateByYear(int year) {
        return getSpecifiedDate(year, Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.AM_PM),
                Calendar.getInstance().get(Calendar.HOUR), Calendar.getInstance().get(Calendar.MINUTE),
                Calendar.getInstance().get(Calendar.SECOND));
    }

    public static Date getSpecifiedDate(int year, int month, int day, int amOrPm, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.AM_PM, amOrPm);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        Date date = calendar.getTime();

        return date;
    }

    public static String getTimeDifference(Context context, Date date) {
        mContext = context;

        if (date == null) {
            return "";
        } else {
            StringBuilder dateText = new StringBuilder();
            Date today = new Date();
            long diffAgo = (today.getTime() - date.getTime()) / 1000L;
            long years = diffAgo / 31104000L;
            long months = diffAgo / 2592000L % 12L;
            long days = diffAgo / 86400L % 30L;
            long hours = diffAgo / 3600L % 24L;
            long minutes = diffAgo / 60L % 60L;
            long seconds = diffAgo % 60L;
            if (years > 0L) {
                appendPastTime(dateText, years, plurals.years, months, plurals.months);
            } else if (months > 0L) {
                appendPastTime(dateText, months, plurals.months, days, plurals.days);
            } else if (days > 0L) {
                appendPastTime(dateText, days, plurals.days, hours, plurals.hours);
            } else if (hours > 0L) {
                appendPastTime(dateText, hours, plurals.hours, minutes, plurals.minutes);
            } else if (minutes > 0L) {
                appendPastTime(dateText, minutes, plurals.minutes, seconds, plurals.seconds);
            } else if (seconds >= 0L) {
                dateText.append(mContext.getResources().getQuantityString(
                        plurals.seconds, (int) seconds, new Object[]{(int) seconds}));
                dateText.append(' ').append(mContext.getResources().getString(R.string.timeline_ago));
            } else {
                dateText = getTimeAhead(date);
            }

            return dateText.toString();
        }
    }

    private static void appendPastTime(StringBuilder s, long timespan, int nameId, long timespanNext, int nameNextId) {
        s.append(mContext.getResources().getQuantityString(nameId, (int) timespan, new Object[]{timespan}));

        if (timespanNext > 0L) {
            s.append(' ').append(mContext.getResources().getString(string.AND)).append(' ');
            s.append(mContext.getResources().getQuantityString(nameNextId, (int) timespanNext, new Object[]{timespanNext}));
        }

        s.append(' ').append(mContext.getResources().getString(R.string.timeline_ago));
    }

    private static StringBuilder getTimeAhead(Date date) {
        StringBuilder dateText = new StringBuilder();
        Date today = new Date();
        long diffAhead = (date.getTime() - today.getTime()) / 1000L;
        long years = diffAhead / 31104000L;
        long months = diffAhead / 2592000L % 12L;
        long days = diffAhead / 86400L % 30L;
        long hours = diffAhead / 3600L % 24L;
        long minutes = diffAhead / 60L % 60L;
        long seconds = diffAhead % 60L;
        if (years > 0L) {
            appendFutureTime(dateText, years, plurals.years, months, plurals.months);
        } else if (months > 0L) {
            appendFutureTime(dateText, months, plurals.months, days, plurals.days);
        } else if (days > 0L) {
            appendFutureTime(dateText, days, plurals.days, hours, plurals.hours);
        } else if (hours > 0L) {
            appendFutureTime(dateText, hours, plurals.hours, minutes, plurals.minutes);
        } else if (minutes > 0L) {
            appendFutureTime(dateText, minutes, plurals.minutes, seconds, plurals.seconds);
        } else if (seconds >= 0L) {
            dateText.append(mContext.getResources().getQuantityString(plurals.seconds, (int) seconds, new Object[]{(int) seconds}));
        }

        return dateText;
    }

    private static void appendFutureTime(StringBuilder s, long timespan, int nameId, long timespanNext, int nameNextId) {
        s.append(mContext.getResources().getString(R.string.timeline_commencing_in)).append(' ');
        s.append(mContext.getResources().getQuantityString(nameId, (int) timespan, new Object[]{timespan}));
        if (timespanNext > 0L) {
            s.append(' ').append(mContext.getResources().getString(string.AND)).append(' ');
            s.append(mContext.getResources().getQuantityString(nameNextId, (int) timespanNext, new Object[]{timespanNext}));
        }
    }

    public static Date stringToDate(String stringToConvert) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DateTimeUtil.DATE_TIME_FORMAT);
        Date date = new Date();
        try {
            date = dateTimeFormat.parse(stringToConvert);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "stringToConvert is " + stringToConvert + ". Unparseable to Date format.");
        } finally {
            return date;
        }
    }

    public static String dateToString(Date dateToConvert) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DateTimeUtil.DATE_TIME_FORMAT);
        String dateTime = dateTimeFormat.format(dateToConvert);

        return dateTime;
    }
}
