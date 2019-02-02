// IMyAidlInterface.aidl
package com.emin.digit.mobile.android.meris.service;

// Declare any non-default types here with import statements
import com.emin.digit.mobile.android.meris.service.Book;
interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    int getCounter();

    void addBook(in Book book);


}
