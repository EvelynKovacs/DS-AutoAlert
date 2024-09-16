package com.example.autoalert.view.activities;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageReceiver {
    private static final int SERVER_PORT = 8889;  // Puerto en el que el servidor está escuchando

    public void startListening(int port) {
        new Thread(() -> {
            try {
                // Crear un socket servidor que escucha en el puerto definido
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    // Esperar a que un cliente se conecte
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    // Leer el mensaje que envió el cliente
                    String message = input.readLine();
                    System.out.println("Mensaje recibido: " + message);

                    // Cerrar la conexión con el cliente
                    input.close();
                    clientSocket.close();
                    Log.d("MessageReceiver", "Mensaje recibido: " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

