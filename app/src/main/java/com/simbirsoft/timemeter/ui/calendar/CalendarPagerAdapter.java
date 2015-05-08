package com.simbirsoft.timemeter.ui.calendar;


import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.views.CalendarViewPager;
import com.simbirsoft.timemeter.ui.views.WeekCalendarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CalendarPagerAdapter extends PagerAdapter {
    private Context mContext;
    private CalendarViewPager mViewPager;
    private HashMap<Integer, WeekCalendarView> mViews = Maps.newHashMap();
    private ArrayList<WeekCalendarView> mCachedViews = Lists.newArrayList();
    private WeekCalendarView.OnCellClickListener mListener;

    public CalendarPagerAdapter(Context context, CalendarViewPager viewPager, WeekCalendarView.OnCellClickListener listener) {
        mContext = context;
        mViewPager = viewPager;
        mListener = listener;
        mViewPager.setAdapter(this);
        mViewPager.setCurrentItem(Integer.MAX_VALUE / 2, false);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        WeekCalendarView v = dequeueView(position);
        container.addView (v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        WeekCalendarView v = getView(position);
        if (v != null) {
            container.removeView (v);
            mViews.remove(position);
            mCachedViews.add(v);
        }
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setCurrentViewActivityCalendar(ActivityCalendar activityCalendar) {
        WeekCalendarView v = getView(mViewPager.getCurrentItem());
        if (v != null) {
            v.setActivityCalendar(activityCalendar);
        }
    }

    public void deselectCurrentViewCell() {
        WeekCalendarView v = getView(mViewPager.getCurrentItem());
        if (v != null) {
            v.deselectCell();
        }
    }

    public void moveNext(Date startDate, Date endDate) {
        WeekCalendarView v = dequeueView(mViewPager.getCurrentItem() + 1);
        setDates(v, startDate, endDate);
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    public void movePrev(Date startDate, Date endDate) {
        WeekCalendarView v = dequeueView(mViewPager.getCurrentItem() - 1);
        setDates(v, startDate, endDate);
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
    }

    public void removeSpansFromCurrentView(long taskId) {
        WeekCalendarView view = getView(mViewPager.getCurrentItem());
        if (view != null) {
            view.getActivityCalendar().removeSpans(taskId);
            view.invalidate();
        }
    }

    private void setDates(WeekCalendarView view, Date startDate, Date endDate) {
        ActivityCalendar ac = view.getActivityCalendar();
        if (startDate.compareTo(ac.getStartDate()) == 0 && endDate.compareTo(ac.getEndDate()) == 0) return;
        ac = new ActivityCalendar();
        ac.setStartDate(startDate);
        ac.setEndDate(endDate);
        view.setActivityCalendar(ac);
    }

    private WeekCalendarView getView(int position) {
        try {
            return mViews.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    private WeekCalendarView dequeueView(int position) {
        WeekCalendarView v = getView(position);
        if (v == null) {
            if (!mCachedViews.isEmpty()) {
                v = mCachedViews.get(0);
                mCachedViews.remove(v);
            } else {
                v = new WeekCalendarView(mContext);
                v.setOnCellClickListener(mListener);
            }
            mViews.put(position, v);
        }
        return v;
    }
}
