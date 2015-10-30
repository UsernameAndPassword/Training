package com.example.gpslocation;

import java.io.UnsupportedEncodingException;
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

public class ReceiveService extends Service {

    private boolean start = true;
    private static final String TAG = "rong.yuan";
    private static final int DEFAULT_PORT = 8000;
    private static final int MAX_DATA_PACKET_LENGTH = 100;
    private byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];

    private ReceiveMsgTask mReceiveMsgTask;

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
        while (start) {
            if (checkNetwork()) {
                if (mReceiveMsgTask != null && mReceiveMsgTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mReceiveMsgTask.cancel(true);
                    mReceiveMsgTask = null;
                }
                mReceiveMsgTask = new ReceiveMsgTask();
                mReceiveMsgTask.execute();

            } else {
                Log.d(TAG, "连接网络失败！");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.d(TAG, "InterruptedException : " + e.getMessage());
            }
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

    class ReceiveMsgTask extends AsyncTask<String, Object, Boolean> {

        private DatagramSocket mUdpSocket;

        @Override
        protected Boolean doInBackground(String... arg0) {
            DatagramPacket recivedata = null;
            try {
                mUdpSocket = new DatagramSocket(DEFAULT_PORT, InetAddress.getByName("255.255.255.255"));
                recivedata = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
                mUdpSocket.receive(recivedata);
            } catch (Exception e) {
                Log.d(TAG, "ReceiveService Exception : " + e.getMessage());
                return false;
            }
            try {
                Log.d(TAG, "接收到数据为：" + new String(recivedata.getData(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, "UnsupportedEncodingException : " + e.getMessage());
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
                Log.d(TAG, "接收成功！");
            } else {
                Log.d(TAG, "接收失败！");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiveMsgTask != null && mReceiveMsgTask.getStatus() == AsyncTask.Status.RUNNING) {
            mReceiveMsgTask.cancel(true);
            mReceiveMsgTask = null;
        }
    }
}
