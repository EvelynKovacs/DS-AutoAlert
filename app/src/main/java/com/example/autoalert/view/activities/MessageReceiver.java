package com.example.autoalert.view.activities;

import android.content.Context;
import android.util.Log;

import com.example.autoalert.utils.FileUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageReceiver {

    private Context context;
    private final int LISTEN_PORT = 12345;

    public MessageReceiver(Context context) {
        this.context = context;
    }

    public void startListening() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String clientIp = clientSocket.getInetAddress().getHostAddress();
                    String message = input.readLine();

                    String[] messagePartido = message.split("-");
                    message = messagePartido[2];
                    Log.i("Recepción de mensajes", "Se obtuvo mensaje de " + clientIp + " a las " + messagePartido[1] + " con " + message);

                    ((MenuInicioActivity)context).storeMessageFromIp(clientIp, message);
                    ((MenuInicioActivity)context).addAndRefreshMap("map-ip-message", clientIp, message);

                    if (message.startsWith("VOTO:")) {
                        Log.i("Recepción de mensajes", "Es un mensaje de ESTADO. Mensaje: " + message);
                        ((MenuInicioActivity)context).saveVote(clientIp, message);
                    }

                    if(message.equals("SI")) {
                        Log.i("Recepción de mensajes", "Es un mensaje de ACCIDENTE. Mensaje: " + message);
                        ((MenuInicioActivity)context).enviarEstado();
                    }
                    input.close();
                    clientSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Recepción de mensajes", "Error en el hilo.");
            }
        }).start();
    }
}
