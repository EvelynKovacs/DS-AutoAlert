package com.example.autoalert.view.activities;

import android.util.Log;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

public class MessageSender {

    int port = 12345; // Puedes definir el puerto a utilizar

    public void sendMessage(String ipAddress, String message) {
        new Thread(() -> {
            try {
                // Obtener la hora actual
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);

                String timeString = String.format("%02d:%02d:%02d", hour, minute, second);

                Log.i("Envio de mensaje", "Se envia mensaje a " + ipAddress + " con " + timeString + "-" + message);

                String timestampMessage = timeString + "-" + message;

                // Crear un socket TCP para enviar el mensaje al dispositivo con la IP recibida
                Socket socket = new Socket(ipAddress, port);
                OutputStream outputStream = socket.getOutputStream();

                // Enviar el mensaje predefinido
                outputStream.write(timestampMessage.getBytes());
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

