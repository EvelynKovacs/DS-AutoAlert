package com.example.autoalert.view.activities;

import android.util.Log;

import com.example.autoalert.utils.NetworkUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;

public class BroadcastSender {
    private static final int BROADCAST_PORT = 8888;
    private static final String BROADCAST_MESSAGE = "DISCOVER_IP_REQUEST";
    private MainActivity mainActivity;

    private NetworkUtils networkUtils;

    public BroadcastSender(MainActivity mainActivity){
        this.networkUtils = new NetworkUtils();
        this.mainActivity = mainActivity;
    }
    public void sendBroadcast() {
        new Thread(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                long primerTimestamp = calendar.getTimeInMillis();
                String primerTimestampString = Long.toString(primerTimestamp);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                String timeString = String.format("%02d:%02d:%02d", hour, minute, second);
                String message = primerTimestampString + "-" +  timeString + "-" + BROADCAST_MESSAGE;

                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                InetAddress broadcastAddress = this.networkUtils.getBroadcastAddress();
                byte[] sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, BROADCAST_PORT);
                socket.send(sendPacket);
                socket.close();
                Log.d("BroadcastSender", "Mensaje de peticion broadcast enviada.");
                Log.i("Verificacion Conexion", "Empieza timer de 4seg");
                Thread.sleep(2000);
                Log.i("Verificacion Conexion", "Terminó Timer. Comienza verificacion de conexion");
                mainActivity.verificarConexion();

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BroadcastSender", "Error al enviar el mensaje de broadcast: " + e.getMessage());

            }
        }).start();
    }

}

