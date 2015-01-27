package com.simbirsoft.timemeter.ui.util;

import android.content.res.Resources;

import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;

import java.util.concurrent.TimeUnit;

public final class TimerTextFormatter {

    public static String formatTaskTimerText(Resources res, long pastTimeMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(pastTimeMillis);
        long millis = TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(pastTimeMillis - millis);
        millis += TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(pastTimeMillis - millis);

        StringBuilder sb = new StringBuilder();
        if (hours > 0 || minutes >= Consts.TASK_ACTIVITY_CLOCK_SWITCH_THRESHOLD_MINUTES) {
            sb.append(hours)
                    .append("<small>")
                    .append(res.getString(R.string.hours_mark))
                    .append("&nbsp;</small>");
        }

        sb.append(String.format("%02d", minutes))
                .append("<small>")
                .append(res.getString(R.string.minutes_mark))
                .append("&nbsp;</small>");

        if (minutes < Consts.TASK_ACTIVITY_CLOCK_SWITCH_THRESHOLD_MINUTES) {
            sb.append(String.format("%02d", seconds))
                    .append("<small>")
                    .append(res.getString(R.string.seconds_mark))
                    .append("&nbsp;</small>");
        }

        return sb.toString();
    }
}
