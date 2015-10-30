package com.example.gpslocation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

public class Client implements Runnable {

    private static final String TAG = "rong.yuan";

    @SuppressWarnings("resource")
    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        try {
            InetAddress serverAddr = InetAddress.getByName("255.255.255.255");
            Log.d(TAG, "Client: Start connecting\n");
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = ("Default message").getBytes("UTF8");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, 8000);
            Log.d(TAG, "Client: Sending ‘" + new String(buf) + "’\n");
            socket.send(packet);
            Log.d(TAG, "Client: Message sent\n");
            Log.d(TAG, "Client: Succeed!\n");
        } catch (Exception e) {
            Log.d(TAG, "Client: Error!\n");
        }
    }
}
