package com.example.gpslocation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

public class SendService extends Service {

    private static final String TAG = "rong.yuan";
    private boolean start = true;
    private SendMsgTask mSendMsgTask;
    private static final int DEFAULT_PORT = 8000;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        String macAddress = intent.getStringExtra("MACADDRESS");
        String longitude = intent.getStringExtra("LONGITUDE");
        String latitude = intent.getStringExtra("LATITUDE");
        if (macAddress != null && longitude != null && latitude != null) {
            if (checkNetwork()) {
                Log.d(TAG, "checkNetwork");
                if (mSendMsgTask != null && mSendMsgTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mSendMsgTask.cancel(true);
                    mSendMsgTask = null;
                }
                Log.d(TAG, "mSendMsgTask");
                mSendMsgTask = new SendMsgTask();
                mSendMsgTask.execute(macAddress, longitude, latitude);
            } else {
                Log.d(TAG, "连接网络失败！");
            }
        } else {
            Log.d(TAG, "信息不全，取消发送！");
        }
    }

    private boolean checkNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    class SendMsgTask extends AsyncTask<String, Object, Boolean> {

        private String mDataString;
        private DatagramSocket mUdpSocket;

        @Override
        protected Boolean doInBackground(String... arg0) {
            DatagramPacket sendData = null;
            mDataString = arg0[0] + " " + arg0[1] + " " + arg0[2];
            try {
                mUdpSocket = new DatagramSocket(DEFAULT_PORT);
                mUdpSocket.setReuseAddress(true);
                byte[] data = mDataString.getBytes("UTF8");
                sendData = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), DEFAULT_PORT);
                mUdpSocket.send(sendData);
            } catch (Exception e) {
                Log.d(TAG, "SendService Exception : " + e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mUdpSocket != null) {
                mUdpSocket.close();
            }
            if (result) {
                Log.d(TAG, "发送成功！");
            } else {
                Log.d(TAG, "发送失败！");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSendMsgTask != null && mSendMsgTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSendMsgTask.cancel(true);
            mSendMsgTask = null;
        }
    }
}
