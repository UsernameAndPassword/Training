package com.example.gpslocation;

import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity {

    private int mCount = 0;
    private String mProvider;
    private Location mLocation;
    private LocationManager mLocationManager;

    private TextView mTextView;

    private boolean start = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textView);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // while (start) {
        // SendMessage("111", "111");
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // ReceiveMessage();
        new Thread(new Server()).start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
        while(start){
        new Thread(new Client()).start();}
    }

    private void ReceiveMessage() {
        Intent intent = new Intent();
        intent.setAction("GPS_LOCATION_RECEIVE_MESSAGE");
        startService(intent);
    }

    private void SendMessage(String longitude, String latitude) {
        Intent intent = new Intent();
        intent.putExtra("MACADDRESS", getMacAddress());
        intent.putExtra("LONGITUDE", longitude);
        intent.putExtra("LATITUDE", latitude);
        intent.setAction("GPS_LOCATION_SEND_MESSAGE");
        startService(intent);
    }

    private String getMacAddress() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isEnableGPS();
        mProvider = getProvider();
        mLocationManager.requestLocationUpdates(mProvider, 1000, 0, mLocationListener);
        mLocationManager.addGpsStatusListener(mGpsStatusListener);
        updateLocation(mLocationManager.getLastKnownLocation(mProvider));
    }

    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            updateLocation(mLocationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateLocation(null);
        }

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }
    };

    GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int arg0) {
            switch (arg0) {
                case GpsStatus.GPS_EVENT_STARTED:
                    mTextView.append("GPS_EVENT_STARTED" + "\n");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    mTextView.append("GPS_EVENT_STOPPED" + "\n");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    mTextView.append("GPS_EVENT_FIRST_FIX" + "\n");
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    mTextView.append("GPS_EVENT_SATELLITE_STATUS" + "\n");
                    break;
                default:
                    mTextView.append("default" + "\n");
                    break;
            }
        }
    };

    private void updateLocation(Location location) {
        mLocation = location;
        mCount++;
        if (mLocation != null) {
            mTextView.append(mCount + ": 经度 = " + mLocation.getLongitude() + " 纬度 = " + mLocation.getLatitude() + "\n");
        } else {
            mTextView.append(mCount + ": mLocation == null" + "\n");
        }
    }

    private void isEnableGPS() {
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mTextView.append("位置源已设置！" + "\n");
            return;
        }
        mTextView.append("位置源未设置！" + "\n");
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, 0);
    }

    private String getProvider() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return mLocationManager.getBestProvider(criteria, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager.removeGpsStatusListener(mGpsStatusListener);
        }
        mTextView.setText("");
    }

}
