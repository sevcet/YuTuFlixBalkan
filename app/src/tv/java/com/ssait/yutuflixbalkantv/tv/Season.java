package com.ssait.yutuflixbalkantv;

import android.os.Parcel;
import android.os.Parcelable;

public class Season implements Parcelable {
    public Season() {}
    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {}
    public static final Creator<Season> CREATOR = new Creator<Season>() {
        @Override public Season createFromParcel(Parcel in) { return new Season(); }
        @Override public Season[] newArray(int size) { return new Season[size]; }
    };
}