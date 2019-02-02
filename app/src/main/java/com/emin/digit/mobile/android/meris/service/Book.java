package com.emin.digit.mobile.android.meris.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Samson on 2016/10/20.
 */
public class Book implements Parcelable{

    private static final String TAG = Book.class.getSimpleName();

    private String name;

    private String author;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Book() {
    }

    public Book(Parcel pl){
        name = pl.readString();
        author = pl.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(author);
    }

    public static final Creator<Book> CREATOR = new Creator<Book>(){
        public Book createFromParcel(Parcel source){
            return new Book(source);
        }

        public Book[] newArray(int size){
            return new Book[size];
        }
    };
}
