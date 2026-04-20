package com.mobproj.habittracker.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private static final SimpleDateFormat ISO =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DISPLAY =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private DateUtils() {}

    public static String todayIso() {
        return ISO.format(new Date());
    }

    public static String formatIsoForDisplay(String iso) {
        try {
            Date d = ISO.parse(iso);
            return d != null ? DISPLAY.format(d) : iso;
        } catch (Exception e) {
            return iso;
        }
    }

    public static String formatMillis(long millis) {
        return DISPLAY.format(new Date(millis));
    }
}
