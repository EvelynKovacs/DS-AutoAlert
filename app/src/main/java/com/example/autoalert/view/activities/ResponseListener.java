package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class ResponseListener {
    private static final int BROADCAST_PORT = 9876;
    private List<String> ipList = new ArrayList<>();

    private DatagramSocket socket;
    private MainActivity mainActivity; // Referencia a la MainActivity para actualizar la lista de IPs

    public ResponseListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    public void listenForResponses() {
        new Thread(() -> {
            try {
                socket = new DatagramSocket(9876); // Puerto para escuchar respuestas
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // Recibir respuesta

                    String senderIp = packet.getAddress().getHostAddress(); // Obtener IP del remitente
                    Log.d("ResponseListener", "IP recibida: " + senderIp);

                    // Verificar si la IP ya está en la lista
                    if (!mainActivity.ipList.contains(senderIp)) {
                        // Agregar la IP a la lista si no está presente
                        mainActivity.ipList.add(senderIp);
                        // Actualizar el TextView en la interfaz de usuario
                        //mainActivity.updateIpList(mainActivity.ipList);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public List<String> getIpList() {
        return ipList;
    }
}
