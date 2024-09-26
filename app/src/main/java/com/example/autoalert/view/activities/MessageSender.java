package com.example.autoalert.view.activities;

import android.util.Log;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.Socket;

public class MessageSender {

    public void sendMessage(String ipAddress, int port, String message) {
        new Thread(() -> {
            try {
                Log.i("Envio de mensaje", "Se envia mensaje a " + ipAddress + " con " + message);

                // Crear un socket TCP para enviar el mensaje al dispositivo con la IP recibida
                Socket socket = new Socket(ipAddress, port);
                OutputStream outputStream = socket.getOutputStream();

                // Enviar el mensaje predefinido
                outputStream.write(message.getBytes());
                outputStream.flush();

                // Cerrar el socket después de enviar el mensaje
                socket.close();

                Log.d("MessageSender", "Mensaje enviado: " + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

