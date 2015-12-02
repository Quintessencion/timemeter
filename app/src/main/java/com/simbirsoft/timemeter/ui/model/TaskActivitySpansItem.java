package com.simbirsoft.timemeter.ui.model;


import android.content.Context;
import android.text.Html;

import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class TaskActivitySpansItem extends TaskActivityEmptyItem {
    public interface OnTaskActivityChangedListener {
        public void onTaskActivityChanged(int index);
    }

    private static final char EM_DASH = 0x2014;
    private static final String TIME_TEST_STRING = String.format("00:00 %c 00:00", EM_DASH);
    private static final long DURATION_TEST_VALUE = (20 * 3600 + 20 * 60 + 20) * 1000;

    private List<TaskTimeSpan> mList;
    private final Calendar mCalendar;
    private OnTaskActivityChangedListener mOnChangedListener;

    public TaskActivitySpansItem() {
        mCalendar = Calendar.getInstance();
    }

    @Override
    public int getItemType() {
        return SPANS_ITEM_TYPE;
    }

    public List<TaskTimeSpan> getList() {
        return (mList == null) ? Collections.<TaskTimeSpan>emptyList() : mList;
    }

    public void setList(List<TaskTimeSpan> list) {
        mList = list;
    }

    public TaskTimeSpan getSpan(int index) {
        Preconditions.checkElementIndex(index, mList.size(), "index is out of items range");
        return mList.get(index);
    }

    public String getSpanTimeTestLabel() {
        return TIME_TEST_STRING;
    }

    public String getSpanTimeLabel(int index) {
        Preconditions.checkElementIndex(index, mList.size(), "index is out of items range");
        TaskTimeSpan span = mList.get(index);
        return String.format("%s %c %s", getHourLabel(span.getStartTimeMillis()), EM_DASH, getHourLabel(span.getEndTimeMillis()));
    }

    public String getSpanDurationTestLabel(Context context) {
        return Html.fromHtml(TimerTextFormatter.formatTaskSpanText(
                context.getResources(), DURATION_TEST_VALUE)).toString();
    }

    public String getSpanDurationLabel(int index, Context context) {
        Preconditions.checkElementIndex(index, mList.size(), "index is out of items range");
        TaskTimeSpan span = mList.get(index);
        return Html.fromHtml(TimerTextFormatter.formatTaskSpanText(
                context.getResources(), span.getDuration())).toString();
    }

    public int getSpansCount() {
        return (mList == null) ? 0 : mList.size();
    }

    public void updateSpanEndTime(int index, long endTime) {
        Preconditions.checkElementIndex(index, mList.size(), "index is out of items range");
        mList.get(index).setEndTimeMillis(endTime);
        if (mOnChangedListener != null) {
            mOnChangedListener.onTaskActivityChanged(index);
        }
    }

    public void setOnChangedListener(OnTaskActivityChangedListener onChangedListener) {
        mOnChangedListener = onChangedListener;
    }

    public int indexOfSpan(TaskTimeSpan span) {
        return (mList != null) ? mList.indexOf(span) : -1;
    }

    public int indexOfSpan(long spanId) {
        if (mList == null) {
            return -1;
        }

        for (int i = 0; i < mList.size(); i++) {
            final TaskTimeSpan span = mList.get(i);
            if (span.getId().equals(spanId)) {
                return i;
            }
        }

        return -1;
    }

    private String getHourLabel(long millis) {
        mCalendar.setTimeInMillis(millis);
        return String.format("%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
    }
}
