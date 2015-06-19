package com.simbirsoft.timemeter.ui.util;

import android.content.res.Resources;

import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;

import java.util.concurrent.TimeUnit;

public final class TimerTextFormatter {

    private static class SplitTime {
        long hours;
        long minutes;
        long seconds;
    }

    public static String formatTaskTimerText(Resources res, long pastTimeMillis) {
        SplitTime t = fetchSplitTime(pastTimeMillis);

        StringBuilder sb = new StringBuilder();
        if (t.hours > 0 || t.minutes >= Consts.TASK_ACTIVITY_CLOCK_SWITCH_THRESHOLD_MINUTES) {
            sb.append(t.hours)
                    .append("<small>")
                    .append(res.getString(R.string.hours_mark))
                    .append("&nbsp;</small>");
        }

        sb.append(String.format("%02d", t.minutes))
                .append("<small>")
                .append(res.getString(R.string.minutes_mark))
                .append("&nbsp;</small>");

        if (t.hours < 1 && t.minutes < Consts.TASK_ACTIVITY_CLOCK_SWITCH_THRESHOLD_MINUTES) {
            sb.append(String.format("%02d", t.seconds))
                    .append("<small>")
                    .append(res.getString(R.string.seconds_mark))
                    .append("&nbsp;</small>");
        }

        return sb.toString();
    }

    public static String formatTaskNotificationTimer(long pastTimeMillis) {
        SplitTime t = fetchSplitTime(pastTimeMillis);

        StringBuilder sb = new StringBuilder();
        if (t.hours > 0) {
            sb.append(t.hours)
                    .append(':');
        }

        sb.append(String.format("%02d", t.minutes))
                .append(':')
                .append(String.format("%02d", t.seconds));

        return sb.toString();
    }

    public static String formatTaskSpanText(Resources res, long durationMillis) {
        SplitTime t = fetchSplitTime(durationMillis);

        StringBuilder sb = new StringBuilder();
        if (t.hours > 0) {
            sb.append(t.hours)
                    .append("<small>")
                    .append(res.getString(R.string.hours_mark))
                    .append("&nbsp;</small>");
        }

        sb.append(String.format("%02d", t.minutes))
                .append("<small>")
                .append(res.getString(R.string.minutes_mark))
                .append("&nbsp;</small>");

        sb.append(String.format("%02d", t.seconds))
                .append("<small>")
                .append(res.getString(R.string.seconds_mark))
                .append("&nbsp;</small>");

        return sb.toString();
    }

    public static String formatOverallTimePlain(Resources res, long durationMillis) {
        SplitTime t = fetchSplitTime(durationMillis);

        StringBuilder sb = new StringBuilder();
        if (t.hours > 0) {
            sb.append(t.hours).append(res.getString(R.string.hours_mark));
        }

        sb.append(" ")
                .append(String.format("%02d", t.minutes))
                .append(res.getString(R.string.minutes_mark))
                .append(" ");

        return sb.toString();
    }

    public static String formatHoursText(Resources res, int hours) {
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours)
                    .append("<small>")
                    .append(res.getString(R.string.hours_mark))
                    .append("&nbsp;</small>");
        }

        return sb.toString();
    }

    private static SplitTime fetchSplitTime(long timeMillis) {
        SplitTime t = new SplitTime();

        t.hours = TimeUnit.MILLISECONDS.toHours(timeMillis);
        long millis = TimeUnit.HOURS.toMillis(t.hours);
        t.minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis - millis);
        millis += TimeUnit.MINUTES.toMillis(t.minutes);
        t.seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis - millis);

        return t;
    }
}
