package sg.gov.dsta.mobileC3.ventilo.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

import org.qap.ctimelineview.R.plurals;
import org.qap.ctimelineview.R.string;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;

public class DateTimeUtil {

    public static final String TAG = "DateTimeUtil";
    public static final String STANDARD_ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    //    public static final String STANDARD_ISO_8601_DATE_TIME_FORMAT = DateTimeFormatter.ISO_ZONED_DATE_TIME.toString();
    private static final String CUSTOM_DATE_TIME_FORMAT = "dd MMM yyyy, HH:mm";
    private static final String CUSTOM_DATE_FORMAT = "dd MMM yyyy";
    private static final String CUSTOM_TIME_FORMAT = "HH:mm";
    private static final String CUSTOM_DATE_TIME_FILE_FORMAT = "ddMMMyyyy_HHmmss";

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

        return calendar.getTime();
    }

    public static String getTimeDifference(Date date) {
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
                dateText.append(MainApplication.getAppContext().getResources().getQuantityString(
                        plurals.seconds, (int) seconds, new Object[]{(int) seconds}));
                dateText.append(' ').append(MainApplication.getAppContext().
                        getResources().getString(R.string.timeline_ago));
            } else {
                dateText = getTimeAhead(date);
            }

            return dateText.toString();
        }
    }

    private static void appendPastTime(StringBuilder s, long timespan, int nameId, long timespanNext, int nameNextId) {
        s.append(MainApplication.getAppContext().getResources().getQuantityString(
                nameId, (int) timespan, new Object[]{timespan}));

        if (timespanNext > 0L) {
            s.append(' ').append(MainApplication.getAppContext().getResources().getString(string.AND)).append(' ');
            s.append(MainApplication.getAppContext().getResources().getQuantityString(
                    nameNextId, (int) timespanNext, new Object[]{timespanNext}));
        }

        s.append(' ').append(MainApplication.getAppContext().getResources().getString(R.string.timeline_ago));
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
            dateText.append(MainApplication.getAppContext().getResources().getQuantityString(
                    plurals.seconds, (int) seconds, new Object[]{(int) seconds}));
        }

        return dateText;
    }

    private static void appendFutureTime(StringBuilder s, long timespan, int nameId, long timespanNext, int nameNextId) {
        s.append(MainApplication.getAppContext().getResources().getString(
                R.string.timeline_commencing_in)).append(' ');
        s.append(MainApplication.getAppContext().getResources().getQuantityString(
                nameId, (int) timespan, new Object[]{timespan}));
        if (timespanNext > 0L) {
            s.append(' ').append(MainApplication.getAppContext().getResources().getString(string.AND)).append(' ');
            s.append(MainApplication.getAppContext().getResources().getQuantityString(
                    nameNextId, (int) timespanNext, new Object[]{timespanNext}));
        }
    }

    public static String getCurrentDateTime() {
        return ZonedDateTime.now().toString();
    }

    public static Date stringToDate(String stringToConvert) {
        Date date = null;

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(stringToConvert, dtf);
            date = Date.from(zonedDateTime.toInstant());

        } catch (DateTimeParseException e) {
            Log.d(TAG, "stringToConvert is " + stringToConvert + ". Unparseable to Date format.");
        }

        return date;
    }

    public static Date stringToISO8601Date(String stringToConvert) {
        Date date = null;

        try {
            date = new SimpleDateFormat(STANDARD_ISO_8601_DATE_TIME_FORMAT).
                    parse(stringToConvert);

        } catch (ParseException e) {
            Log.d(TAG, "stringToConvert is " + stringToConvert + ". Unparseable to Date format.");
        }

        return date;
    }

    /**
     * Add milli-seconds to ZonedDateTime; Returns resulting date
     *
     * @param zonedDateTime
     * @param secondsToAdd
     * @return
     */
    public static String addMilliSecondsToZonedDateTime(String zonedDateTime, int secondsToAdd) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(stringToDate(zonedDateTime));
        calendar.add(Calendar.MILLISECOND, secondsToAdd);

        return dateToStandardIsoDateTimeStringFormat(calendar.getTime());
    }

    /** ----- Date To String Format Conversion ----- **/
    public static String dateToStandardIsoDateTimeStringFormat(Date dateToConvert) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(STANDARD_ISO_8601_DATE_TIME_FORMAT);
        return dateTimeFormat.format(dateToConvert);
    }

    public static String dateToCustomDateTimeStringFormat(Date dateToConvert) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(CUSTOM_DATE_TIME_FORMAT);
        return dateTimeFormat.format(dateToConvert);
    }

    public static String dateToCustomDateStringFormat(Date dateToConvert) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(CUSTOM_DATE_FORMAT);
        return dateTimeFormat.format(dateToConvert);
    }

    public static String dateToCustomTimeStringFormat(Date dateToConvert) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(CUSTOM_TIME_FORMAT);
        return dateTimeFormat.format(dateToConvert);
    }

    public static String dateToCustomDateTimeFileStringFormat(Date dateToConvert) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(CUSTOM_DATE_TIME_FILE_FORMAT);
        return dateTimeFormat.format(dateToConvert);
    }
}
