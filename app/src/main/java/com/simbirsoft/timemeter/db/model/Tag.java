package com.simbirsoft.timemeter.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import nl.qbusict.cupboard.annotation.Column;

public class Tag implements Parcelable, Serializable {

    private static final long serialVersionUID = 2204828417696124694L;

    public static final String TABLE_NAME = "Tag";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_COLOR = "color";

    public static final Parcelable.Creator<Tag> CREATOR =
            new Parcelable.Creator<Tag>() {
                @Override
                public Tag createFromParcel(Parcel parcel) {
                    return new Tag(parcel);
                }

                @Override
                public Tag[] newArray(int sz) {
                    return new Tag[sz];
                }
            };

    @Column(COLUMN_ID)
    private Long _id;

    @Column(COLUMN_NAME)
    private String name;

    @Column(COLUMN_COLOR)
    private int color = -10453621; /* blue grey #607D8B */

    private boolean mChecked;

    public Tag() {
    }

    protected Tag(Parcel source) {
        readParcel(source);
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public boolean hasId() {
        return _id != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        if (_id != null ? !_id.equals(tag._id) : tag._id != null) return false;
        if (name != null ? !name.equals(tag.name) : tag.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (hasId() ? 1 : 0));
        if (hasId()) {
            parcel.writeLong(_id);
        }
        parcel.writeString(name);
        parcel.writeInt(color);
    }

    private void readParcel(Parcel parcel) {
        boolean hasId = parcel.readByte() == 1;
        if (hasId) {
            _id = parcel.readLong();
        }
        name = parcel.readString();
        color = parcel.readInt();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
