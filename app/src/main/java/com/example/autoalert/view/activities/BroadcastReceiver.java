package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Enumeration;

public class BroadcastReceiver {
    private static final int BROADCAST_PORT = 8888;
    private static final String BROADCAST_RESPONSE = "DISCOVER_IP_RESPONSE";

    private static final int RESPONSE_PORT = 8888; // Puerto para responder

    private MainActivity mainActivity;


    public BroadcastReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void startListening() {
        new Thread(() -> {
            try {
                Log.i("Hilo BroadcastReceiver", "Se creo hilo de BroadcastReceiver.");
                DatagramSocket socket = new DatagramSocket(BROADCAST_PORT);
                socket.setBroadcast(true);
                byte[] receiveBuffer = new byte[1024];


                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(receivePacket);

                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String myIpAddress = getDeviceIpAddress(); // Método para obtener la IP del dispositivo
                    String senderIp = receivePacket.getAddress().getHostAddress();
                    if(!senderIp.equals(myIpAddress)){
                        //Mensaje recibido de la forma:
                        // timestampOriginal - timestampFormateado [HH:MM:SS] - mensaje - alias
                        Log.d("BroadcastReceiver", "Mensaje recibido: " + message);
                        String[] messagePartido = message.split("-");

                        // Obtener el tiempo actual del sistema nuevamente
                        long timestampRecuperado = Long.parseLong(messagePartido[0]);
                        Log.i("Diferencia de Tiempo", "Primer tiempo de String a Long: " + timestampRecuperado);

                        //Obtengo mensaje sin el timestamp
                        message = messagePartido[2];
                        Log.d("BroadcastReceiver", "Mensaje recibido, segunda parte: " + message);
                        String timestamp = messagePartido[0];

                        if (message.equals("DISCOVER_IP_REQUEST")) {

//                            Log.d("BroadCastReceiver", "El mensaje tiene: "+message);
//
//                            String[] parts = message.split(":");
//                            String requestType = parts[0];
//                            String senderAlias = parts.length > 1 ? parts[1] : "SinAlias"; // Si no hay alias, usar "SinAlias"
//
//                            // Almacenar la IP y el alias en MainActivity
//                            mainActivity.storeAliasFromIp(senderIp, senderAlias);


                            mainActivity.ipList.add(senderIp);
                            mainActivity.updateIpList(senderIp);

                            // Guardar la IP del emisor
                            mainActivity.storeMessageFromIp(senderIp, message);
                            mainActivity.actualizarIpTimeStamp(senderIp, timestamp);
                            // Enviar respuesta con la IP del receptor

                            sendResponse(senderIp, RESPONSE_PORT, myIpAddress);
                        }

                        if (message.equals(BROADCAST_RESPONSE)){
                            senderIp = receivePacket.getAddress().getHostAddress();
                            mainActivity.actualizarIpTimeStamp(senderIp, timestamp);
                            mainActivity.updateIpList(senderIp);
                            // Guardar la IP del emisor
                            mainActivity.storeMessageFromIp(senderIp, message);
                        }
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Enviar una respuesta al emisor con la IP del receptor
    private void sendResponse(String senderIp, int port, String myIpAddress) {
        new Thread(() -> {
            try {
                Log.e("BroadcastReceiver", "Preparando mensaje de respuesta.");

                // Crear el socket para enviar la respuesta
                DatagramSocket socket = new DatagramSocket();
                InetAddress receiverAddress = InetAddress.getByName(senderIp);

//                // Obtener el alias desde MainActivity
//                String alias = mainActivity.getAlias();
//                // Crear el mensaje de respuesta con la IP del receptor y su alias
//                String responseMessage = BROADCAST_RESPONSE + ":" + alias;

                Calendar calendar = Calendar.getInstance();
                long primerTimestamp = calendar.getTimeInMillis();
                String primerTimestampString = Long.toString(primerTimestamp);
                Log.i("Diferencia de Tiempo", "Primer timestamp en String: " + primerTimestampString);


                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);

                String timeString = String.format("%02d:%02d:%02d", hour, minute, second);


                // Crear el mensaje de respuesta con la IP del receptor
                String messageToSend = primerTimestampString + "-" + timeString + "-" + BROADCAST_RESPONSE;
                byte[] message = messageToSend.getBytes();

                DatagramPacket responsePacket = new DatagramPacket(message, message.length, receiverAddress, port);

                // Enviar el paquete de respuesta
                socket.send(responsePacket);
                socket.close();

                Log.d("BroadcastReceiver", "Respuesta enviada a " + senderIp + " con mi IP: " + myIpAddress);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BroadcastReceiver", "Error al enviar la respuesta: " + e.getMessage());
            }
        }).start();
    }


    // Método para obtener la IP del dispositivo
    private String getDeviceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "IP no disponible";
    }
}

