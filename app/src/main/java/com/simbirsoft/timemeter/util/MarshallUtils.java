package com.simbirsoft.timemeter.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

public class MarshallUtils {
    public static byte[] marshall(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        return creator.createFromParcel(parcel);
    }

    public static <T> byte[] marshall(T[] array) {
        Parcel parcel = Parcel.obtain();
        parcel.writeArray(array);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static <T> T[] unmarshall(byte[] bytes, T[] array) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        Object[] temp = parcel.readArray(array.getClass().getClassLoader());
        ArrayList<Object> list = new ArrayList<>(Arrays.asList(temp));
        parcel.recycle();

        return list.toArray(array);
    }
}
