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
    private SistemaVotacion sistemaVotacion;
    private FileUtils archivoUtils;


    // Constructor que recibe el contexto de la actividad
    public MessageReceiver(Context context) {
        this.context = context;
        this.sistemaVotacion = new SistemaVotacion((MainActivity) context);
        this.archivoUtils = new FileUtils(context);
    }

    public void startListening() {
        new Thread(() -> {
            try {

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

                    String[] messagePartido = message.split("-");

                    message = messagePartido[2];

                    Log.i("Recepción de mensajes", "Se obtuvo mensaje de " + clientIp + " a las " + messagePartido[1] + " con " + message);


                    // Almacenar la IP y el mensaje recibido en el HashMap
                    ((MainActivity)context).storeMessageFromIp(clientIp, message);

                    ((MainActivity)context).addAndRefreshMap("map-ip-message", clientIp, message);

                    if (message.startsWith("VOTO:")) {
                        Log.i("Recepción de mensajes", "Es un mensaje de ESTADO. Mensaje: " + message);
                        //(MainActivity)context).guardarVoto(clientIp, message);
                        ((MainActivity)context).saveVote(clientIp, message);
                        //((MainActivity)context).addAndRefreshMap("map-ip-voto", clientIp, message);

                    }

                    if(message.equals("SI")) {
                        Log.i("Recepción de mensajes", "Es un mensaje de ACCIDENTE. Mensaje: " + message);
                        //((MainActivity)context).enviarEstado();
                        //sistemaVotacion.enviarEstado();
                        ((MainActivity)context).enviarEstado();
                        //((MainActivity)context).iniciarTemporizador();
                    }

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

