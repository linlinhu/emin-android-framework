package com.emin.digit.mobile.android.meris.platform.components.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * author: Samson
 * created on: 2017/11/10 下午 1:46
 * description:
 */
public class LocationCenter {

    private static final String TAG = LocationCenter.class.getCanonicalName();

    private LocationManager locationManager;
    private Context mContext;
    private static volatile LocationCenter instance = null;
    private LocationCenter(Context context) {
        mContext = context;
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static LocationCenter getInstance(Context context) {
        if(instance == null) {
            synchronized (LocationCenter.class) {
                if(instance == null) {
                    instance = new LocationCenter(context);
                }
            }
        }
        return instance;
    }

    public Location get() {
        checkPermission();
//        String provider = LocationManager.GPS_PROVIDER;
//        Location location = locationManager.getLastKnownLocation(provider);

        // 设置位置服务信息
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // 取得效果最好的位置服务
        String provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(provider);
        while (location == null) {
            locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
        }
        return location;
    }

    private boolean checkPermission() {
        Log.d(TAG, "check permission");
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"定位权限需要再AndroidManifest.xml声明,并开启app定位权限");
            return false;
        }
        return true;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            double longitude = location.getLongitude();
            double latitude  = location.getLatitude();
            Log.d(TAG,"Location longitude:"+ longitude +" latitude: "+ latitude );
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled");
        }
    };
}
