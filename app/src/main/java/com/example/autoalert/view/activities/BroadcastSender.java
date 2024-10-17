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

    private MainActivity mainActivity = new MainActivity();

    // Método setter para asignar redActivity
    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void sendBroadcast() {

        //setMainActivity(mainActivity);
        new Thread(() -> {
            try {

                if (mainActivity == null) {
                    Log.e("BroadcastSender", "MainActivity es nulo. No se puede obtener el alias.");
                    return;
                }
                // Obtener el alias desde MainActivity
                String alias = mainActivity.getAlias(); // Asegúrate de que este método esté devolviendo el valor correcto

                if (alias == null || alias.isEmpty()) {
                    Log.e("BroadcastSender", "Alias está vacío o nulo.");
                    return; // No continuar si el alias está vacío
                }

                // Obtener la hora actual
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);

                String timeString = String.format("%02d:%02d:%02d", hour, minute, second);


                // Verificar si el alias está vacío
                if (alias.isEmpty()) {
                    Log.e("BroadcastSender", "No se puede enviar el mensaje de broadcast: alias vacío.");
                    return; // Salir del método si el alias está vacío
                }

                String message = timeString + "-" + BROADCAST_MESSAGE + "-" + alias;

                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                InetAddress broadcastAddress = getBroadcastAddress();


                byte[] sendData = message.getBytes();
                //byte[] sendData = BROADCAST_MESSAGE.getBytes();

                Log.d("BroadCastSender", "El mensaje en SendData: "+sendData.toString());


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

