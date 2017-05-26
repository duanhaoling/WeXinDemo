package com.example.weixin50.test;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * @author stone
 * @date 16/4/29
 */
public class ExtraValues implements Parcelable {

    private String preferCommunities; //小区偏好
    private String preferProperties; //房源偏好

    public ExtraValues() {
    }

    public ExtraValues(@NonNull String preferCommunities, @NonNull String preferProperties) {
        this.preferCommunities = preferCommunities;
        this.preferProperties = preferProperties;
    }

    @NonNull
    public String getPreferCommunities() {
        return preferCommunities == null ? "" : preferCommunities;
    }

    public void setPreferCommunities(String preferCommunities) {
        this.preferCommunities = preferCommunities;
    }

    @NonNull
    public String getPreferProperties() {
        return preferProperties == null ? "" : preferProperties;
    }

    public void setPreferProperties(String preferProperties) {
        this.preferProperties = preferProperties;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.preferCommunities);
        dest.writeString(this.preferProperties);
    }

    protected ExtraValues(Parcel in) {
        this.preferCommunities = in.readString();
        this.preferProperties = in.readString();
    }

    public static final Parcelable.Creator<ExtraValues> CREATOR = new Parcelable.Creator<ExtraValues>() {
        @Override
        public ExtraValues createFromParcel(Parcel source) {
            return new ExtraValues(source);
        }

        @Override
        public ExtraValues[] newArray(int size) {
            return new ExtraValues[size];
        }
    };
}
