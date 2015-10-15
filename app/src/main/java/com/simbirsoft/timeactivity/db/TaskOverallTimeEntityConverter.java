package com.simbirsoft.timeactivity.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.simbirsoft.timeactivity.model.TaskOverallActivity;

import java.lang.reflect.Field;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.ReflectiveEntityConverter;


public class TaskOverallTimeEntityConverter extends ReflectiveEntityConverter<TaskOverallActivity> {
    public TaskOverallTimeEntityConverter(Cupboard cupboard, Class<TaskOverallActivity> entityClass) {
        super(cupboard, entityClass);
    }

    @Override
    protected FieldConverter<?> getFieldConverter(Field field) {
        if (field.getName().equals(TaskOverallActivity.COLUMN_OVERALL_DURATION)) {
            return new FieldConverter<Object>() {
                @Override
                public Object fromCursorValue(Cursor cursor, int i) {
                    return cursor.getLong(i);
                }

                @Override
                public void toContentValue(Object o, String s, ContentValues contentValues) {
                    contentValues.put(s, (Long) o);
                }

                @Override
                public ColumnType getColumnType() {
                    return ColumnType.JOIN;
                }
            };
        }

        return super.getFieldConverter(field);
    }
}
