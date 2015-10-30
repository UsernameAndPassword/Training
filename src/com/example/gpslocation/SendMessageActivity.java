package com.example.gpslocation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SendMessageActivity extends Activity {
    private static String LOG_TAG = "WifiBroadcastActivity";
    private WifiManager.MulticastLock lock;
    private WifiManager manager;
    private boolean start = true;
    private EditText IPAddress;
    EditText numberEdit;
    private String address;
    public static final int DEFAULT_PORT = 8000;
    private static final int MAX_DATA_PACKET_LENGTH = 40;
    private byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];
    private byte[] redata = new byte[MAX_DATA_PACKET_LENGTH];
    Button startButton;
    Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_broadcast);
        IPAddress = (EditText) this.findViewById(R.id.address);
        startButton = (Button) this.findViewById(R.id.start);
        stopButton = (Button) this.findViewById(R.id.stop);
        numberEdit = (EditText) findViewById(R.id.number);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        address = getLocalIPAddress();
        if (address != null) {
            IPAddress.setText(address);
        } else {
            IPAddress.setText("Can not get IP address");
            return;
        }
        manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        lock = manager.createMulticastLock("test wifi");
        // numberEdit.setText(number);
        startButton.setOnClickListener(listener);
        stopButton.setOnClickListener(listener);

    }

    private String number = getRandomNumber();
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == startButton) {
                start = true;
                new BroadCastUdp("I'm chinese!").start();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            } else if (v == stopButton) {
                start = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        }
    };

    private String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
        return null;
    }

    private String getRandomNumber() {
        int num = new Random().nextInt(65536);
        String numString = String.format("x", num);
        return numString;
    }

    public class BroadCastUdp extends Thread {
        private String dataString;
        private DatagramSocket udpSocket;

        public BroadCastUdp(String dataString) {
            this.dataString = dataString;
        }

        public void run() {
            DatagramPacket dataPacket = null, recivedata = null;
            lock.acquire();
            try {
                udpSocket = new DatagramSocket(DEFAULT_PORT);

                dataPacket = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
                byte[] data = dataString.getBytes();
                dataPacket.setData(data);
                dataPacket.setLength(data.length);
                dataPacket.setPort(DEFAULT_PORT);

                InetAddress broadcastAddr;

                broadcastAddr = InetAddress.getByName("255.255.255.255");
                dataPacket.setAddress(broadcastAddr);

                recivedata = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
                // recivedata.setData(redata);
                // recivedata.setLength(redata.length);
                // recivedata.setPort(DEFAULT_PORT);
                // recivedata.setAddress(broadcastAddr);

            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }
            try {
                udpSocket.send(dataPacket);
                System.out.print("dataPacket地址为：" + dataPacket.getAddress().toString()); // 此为IP地址
                System.out.print("dataPacketsock地址为：" + dataPacket.getSocketAddress().toString()); // 此为IP加端口号
                sleep(100);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }
            while (start) {
                try {
                    udpSocket.receive(recivedata);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.e(LOG_TAG, e.toString());
                }
                if (recivedata.getLength() != 0) {
                    String codeString = new String(buffer, 0, recivedata.getLength());

                    System.out.println("接收到数据为：" + codeString);
                    System.out.print("recivedataIP地址为：" + recivedata.getAddress().toString()); // 此为IP地址
                    System.out.print("recivedata_sock地址为：" + recivedata.getSocketAddress().toString()); // 此为IP加端口号
                }
            }
            lock.release();
            udpSocket.close();
        }
    }
}
