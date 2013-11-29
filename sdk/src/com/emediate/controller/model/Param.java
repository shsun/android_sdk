package com.emediate.controller.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Param implements Parcelable {

    public static final Parcelable.Creator<Param> CREATOR = new Creator<Param>() {

	@Override
	public Param[] newArray(int size) {
	    return new Param[size];
	}

	@Override
	public Param createFromParcel(Parcel source) {
	    return new Param(source);
	}
    };

    public String key, value;

    public Param() {
	
    }
    
    public Param(String key, String value) {
	this.key = key;
	this.value = value;
    }

    private Param(Parcel parcel) {
	this.key = parcel.readString();
	this.value = parcel.readString();
    }

    @Override
    public int describeContents() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
	dest.writeString(key);
	dest.writeString(value);
    }
}
