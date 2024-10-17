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

    private DatagramSocket socket;
    private byte[] buffer = new byte[1024];
    private boolean running;

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

                        Log.d("BroadcastReceiver", "Mensaje recibido: " + message);
                        String[] messagePartido = message.split("-");
                        //Obtengo mensaje sin el timestamp
                        message = messagePartido[1];
                        Log.d("BroadcastReceiver", "Mensaje recibido, segunda parte: " + message);
                        String timestamp = messagePartido[0];
                        String senderAlias = messagePartido[2];
                        if (message.equals("DISCOVER_IP_REQUEST")) {

                            Log.d("BroadCastReceiver", "El mensaje tiene: "+message);

                            String[] parts = message.split(":");
                            String requestType = parts[0];
                            //String senderAlias = parts.length > 1 ? parts[1] : "SinAlias"; // Si no hay alias, usar "SinAlias"

                            // Almacenar la IP y el alias en MainActivity
                            mainActivity.storeAliasFromIp(senderIp, senderAlias);


                            mainActivity.ipList.add(senderIp);
                            mainActivity.updateIpList(senderIp);

                            // Guardar la IP del emisor
                            mainActivity.storeMessageFromIp(senderIp, message);
                            mainActivity.actualizarIpTimeStamp(senderIp, timestamp);
                            // Enviar respuesta con la IP del receptor

                            sendResponse(senderIp, RESPONSE_PORT, myIpAddress);
                        }

                        if (message.equals(BROADCAST_RESPONSE)){

                            // Almacenar la IP y el alias en MainActivity
                            mainActivity.storeAliasFromIp(senderIp, senderAlias);

                            mainActivity.actualizarIpTimeStamp(senderIp, timestamp);
                            //mainActivity.ipList.add(senderIp);
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

                // Obtener el alias desde MainActivity
                String alias = mainActivity.getAlias();


                // Obtener la hora actual
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);

                String timeString = String.format("%02d:%02d:%02d", hour, minute, second);
                // Crear el mensaje de respuesta con la IP del receptor
                String messageToSend = timeString+"-"+BROADCAST_RESPONSE+"-"+alias;
                byte[] message = messageToSend.getBytes();
                //byte[] message = BROADCAST_RESPONSE.getBytes();
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

    private InetAddress getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                        return addr;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

