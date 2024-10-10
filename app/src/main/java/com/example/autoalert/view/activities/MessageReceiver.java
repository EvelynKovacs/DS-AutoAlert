package com.example.autoalert.view.activities;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageReceiver {

    private Context context;
    private final int LISTEN_PORT = 12345; // Puerto donde escuchar los mensajes


    // Constructor que recibe el contexto de la actividad
    public MessageReceiver(Context context) {
        this.context = context;
    }

    public void startListening() {
        new Thread(() -> {
            try {
                Log.i("Recepcion de mensajes", "Se creo hilo de recepcion de mensajes en el puerto: " + LISTEN_PORT);

                // Crear un socket servidor que escucha en el puerto definido
                ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);

                while (true) {
                    // Esperar a que un cliente se conecte
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    // Obtener la IP del cliente
                    String clientIp = clientSocket.getInetAddress().getHostAddress();

                    // Leer el mensaje que envió el cliente
                    String message = input.readLine();

                    Log.i("Recepción de mensajes", "Se obtuvo mensaje de " + clientIp + " con " + message);


                    // Almacenar la IP y el mensaje recibido en el HashMap
                    ((MainActivity)context).storeMessageFromIp(clientIp, message);

                    if (message.startsWith("VOTO:")) {
                        Log.i("Recepción de mensajes", "Es un mensaje de ESTADO. Mensaje: " + message);
                        ((MainActivity)context).guardarVoto(clientIp, message);
                    }

                    if(message.equals("SI")) {
                        Log.i("Recepción de mensajes", "Es un mensaje de ACCIDENTE. Mensaje: " + message);
                        ((MainActivity)context).enviarEstado();
                    }

                    /*if(message.equals("Desconexion")) {
                        Log.i("Recepción de mensajes", "Es un mensaje de DESCONEXION. Mensaje: " + message);
                        ((MainActivity)context).eliminarIp(clientIp);
                    }
                    */

                    // Cerrar la conexión con el cliente
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

