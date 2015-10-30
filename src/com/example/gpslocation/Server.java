package com.example.gpslocation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

public class Server implements Runnable {

    private static final String TAG = "rong.yuan";

    @SuppressWarnings("resource")
    @Override
    public void run() {
        try {
            InetAddress serverAddr = InetAddress.getByName("255.255.255.255");
            Log.d(TAG, "\nServer: Start connecting\n");
            DatagramSocket socket = new DatagramSocket(8000, serverAddr);
            byte[] buf = new byte[17];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            Log.d(TAG, "Server: Receiving\n");
            socket.receive(packet);
            Log.d(TAG, "Server: Message received: ‘" + new String(packet.getData(),"UTF-8") + "’\n");
            Log.d(TAG, "Server: Succeed!\n");
        } catch (Exception e) {
            Log.d(TAG, "Server: Error!\n");
        }
    }
}
