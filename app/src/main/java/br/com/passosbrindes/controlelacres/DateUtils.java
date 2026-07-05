package br.com.passosbrindes.controlelacres;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat BR = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
    static {
        ISO.setTimeZone(TimeZone.getDefault());
        BR.setTimeZone(TimeZone.getDefault());
    }

    public static String todayIso() {
        return ISO.format(new Date());
    }

    public static String nowIsoDateTime() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        return f.format(new Date());
    }

    public static String addDays(String iso, int days) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(ISO.parse(iso));
            c.add(Calendar.DAY_OF_MONTH, days);
            return ISO.format(c.getTime());
        } catch (Exception e) {
            return iso;
        }
    }

    public static String toBrDate(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "";
        try {
            return BR.format(ISO.parse(iso));
        } catch (ParseException e) {
            return iso;
        }
    }

    public static int daysBetweenToday(String iso) {
        try {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            Calendar target = Calendar.getInstance();
            target.setTime(ISO.parse(iso));
            target.set(Calendar.HOUR_OF_DAY, 0);
            target.set(Calendar.MINUTE, 0);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);
            long diff = target.getTimeInMillis() - today.getTimeInMillis();
            return Math.round(diff / 86400000f);
        } catch (Exception e) {
            return 0;
        }
    }
}
