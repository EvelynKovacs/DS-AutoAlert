package com.example.autoalert.view.activities;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageReceiver {

    private Context context;

    // Constructor que recibe el contexto de la actividad
    public MessageReceiver(Context context) {
        this.context = context;
    }

    public void startListening(int port) {
        new Thread(() -> {
            try {
                // Crear un socket servidor que escucha en el puerto definido
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    // Esperar a que un cliente se conecte
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    // Obtener la IP del cliente
                    String clientIp = clientSocket.getInetAddress().getHostAddress();


                    // Leer el mensaje que envió el cliente
                    String message = input.readLine();

                    // Almacenar la IP y el mensaje recibido en el HashMap
                    ((MainActivity)context).storeMessageFromIp(clientIp, message);

//                    if(message=="ACCIDENTE"){
//                        ((MainActivity)context).iniciarVotacion();
//                    }

                    if (message.startsWith("VOTO:")) {
                        ((MainActivity)context).guardarVoto(clientIp, message);
                    }



                    if(message.equals("SI")) {
                        ((MainActivity)context).enviarEstado();
                    }


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

