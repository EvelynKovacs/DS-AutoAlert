package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class BroadcastReceiver {
    private static final int BROADCAST_PORT = 8888;
    private static final String BROADCAST_RESPONSE = "DISCOVER_IP_RESPONSE";



    public void startListening() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(BROADCAST_PORT, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);

                while (true) {
                    byte[] receiveBuffer = new byte[15000];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(receivePacket);

                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    // Si el mensaje es el de descubrimiento, responder con la IP
                    if (message.equals("DISCOVER_IP_REQUEST")) {
                        InetAddress myIp = getLocalIPAddress();
                        if (myIp != null) {
                            byte[] sendData = BROADCAST_RESPONSE.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                    receivePacket.getAddress(), receivePacket.getPort());
                            socket.send(sendPacket);
                            Log.d("BroadcastReceiver", "Mensaje de peticion broadcast recibida. Contestar con: " + myIp);

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private InetAddress getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                        return addr;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

