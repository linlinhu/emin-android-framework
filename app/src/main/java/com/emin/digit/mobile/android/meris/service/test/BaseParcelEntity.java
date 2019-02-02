package com.emin.digit.mobile.android.meris.service.test;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Samson on 2016/10/20.
 */
public class BaseParcelEntity implements Parcelable{

    public BaseParcelEntity() {
    }

    public BaseParcelEntity(Parcel pl){
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator<BaseParcelEntity> CREATOR = new Creator<BaseParcelEntity>(){
        public BaseParcelEntity createFromParcel(Parcel source){
            return new BaseParcelEntity(source);
        }

        public BaseParcelEntity[] newArray(int size){
            return new BaseParcelEntity[size];
        }
    };
}
