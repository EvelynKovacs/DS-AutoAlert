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
    private RedActivity redActivity; // Referencia a la MainActivity para actualizar la lista de IPs

    public ResponseListener(RedActivity redActivity) {
        this.redActivity = redActivity;
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
                    if (!redActivity.ipList.contains(senderIp)) {
                        // Agregar la IP a la lista si no está presente
                        redActivity.ipList.add(senderIp);
                        // Actualizar el TextView en la interfaz de usuario
                        //redActivity.updateIpList(redActivity.ipList);
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
