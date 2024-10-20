package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;

public class BroadcastSender {
    private static final int BROADCAST_PORT = 8888;
    private static final String BROADCAST_MESSAGE = "DISCOVER_IP_REQUEST";
    private MainActivity mainActivity;

    public void sendBroadcast() {
        new Thread(() -> {
            try {

                //ESTO PUEDO SACARLO Y PONERLO EN UNA FUNCION
                // Obtener la hora actual en milisegundos
                Calendar calendar = Calendar.getInstance();
                long primerTimestamp = calendar.getTimeInMillis();
                String primerTimestampString = Long.toString(primerTimestamp);
                Log.i("Diferencia de Tiempo", "Primer timestamp en String: " + primerTimestampString);


                //ESTO ES LO QUE LE MUESTRO AL USUARIO
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                String timeString = String.format("%02d:%02d:%02d", hour, minute, second);
                Log.i("Diferencia de Tiempo","Primer tiempo: " + timeString);

                String message = primerTimestampString + "-" +  timeString + "-" + BROADCAST_MESSAGE;
                Log.i("Diferencia de Tiempo","Primer tiempo: " + timeString);
                //HASTA ACA

                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                InetAddress broadcastAddress = getBroadcastAddress();

                byte[] sendData = message.getBytes();
                //byte[] sendData = BROADCAST_MESSAGE.getBytes();


                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, BROADCAST_PORT);
                socket.send(sendPacket);

                socket.close();
                Log.d("BroadcastSender", "Mensaje a enviar: " + message);
                Log.d("BroadcastSender", "Mensaje de peticion broadcast enviada.");

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BroadcastSender", "Error al enviar el mensaje de broadcast: " + e.getMessage());

            }
        }).start();
    }

    // Obtener la dirección de broadcast de la red a la cual estás conectado
    private InetAddress getBroadcastAddress() throws Exception {
        // Recorre las interfaces de red disponibles y obtén la dirección de broadcast
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) {
                        return broadcast;
                    }
                }
            }
        }
        throw new Exception("No se pudo obtener la dirección de broadcast");
    }
}

