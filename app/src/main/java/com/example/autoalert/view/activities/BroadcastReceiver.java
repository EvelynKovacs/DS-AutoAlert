package com.example.autoalert.view.activities;

import android.util.Log;

import com.example.autoalert.utils.NetworkUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;

public class BroadcastReceiver {
    private static final int BROADCAST_PORT = 8888;
    private static final String BROADCAST_RESPONSE = "DISCOVER_IP_RESPONSE";

    private static final int RESPONSE_PORT = 8888; // Puerto para responder

    private MenuInicioActivity mainActivity;

    private NetworkUtils networkUtils;


    public BroadcastReceiver(MenuInicioActivity mainActivity) {
        this.networkUtils = new NetworkUtils();
        this.mainActivity = mainActivity;
    }

    public void startListening() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(BROADCAST_PORT);
                socket.setBroadcast(true);
                byte[] receiveBuffer = new byte[1024];


                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(receivePacket);

                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String myIpAddress = networkUtils.getDeviceIpAddress(); // Método para obtener la IP del dispositivo
                    String senderIp = receivePacket.getAddress().getHostAddress();
                    if(!senderIp.equals(myIpAddress)){
                        // timestampOriginal - timestampFormateado [HH:MM:SS] - mensaje - alias
                        Log.d("BroadcastReceiver", "Mensaje recibido: " + message);
                        String[] messagePartido = message.split("-");
                        message = messagePartido[2];
                        String timestamp = messagePartido[0];
                        String alias = messagePartido[3];

                        if (message.equals("DISCOVER_IP_REQUEST")) {
                            mainActivity.agregarIpYActualizarArchivo(senderIp);
                            mainActivity.ipList.add(senderIp);
                            mainActivity.updateIpList(senderIp);
                            mainActivity.storeMessageFromIp(senderIp, message);
                            mainActivity.actualizarIpTimeStamp(senderIp, timestamp);
                            // esto es lo del alias
                            mainActivity.addAndRefreshMap("map-ip-alias",senderIp,alias);
                            sendResponse(senderIp, RESPONSE_PORT, myIpAddress);
                        }

                        if (message.equals(BROADCAST_RESPONSE)){
                            senderIp = receivePacket.getAddress().getHostAddress();
                            mainActivity.agregarIpYActualizarArchivo(senderIp);
                            mainActivity.actualizarIpTimeStamp(senderIp, timestamp);
                            mainActivity.updateIpList(senderIp);
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
                DatagramSocket socket = new DatagramSocket();
                InetAddress receiverAddress = InetAddress.getByName(senderIp);
                Calendar calendar = Calendar.getInstance();
                long primerTimestamp = calendar.getTimeInMillis();
                String primerTimestampString = Long.toString(primerTimestamp);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                String timeString = String.format("%02d:%02d:%02d", hour, minute, second);
                String alias = mainActivity.getAlias();

                String messageToSend = primerTimestampString + "-" + timeString + "-" + BROADCAST_RESPONSE + "-" + alias;
                byte[] message = messageToSend.getBytes();

                DatagramPacket responsePacket = new DatagramPacket(message, message.length, receiverAddress, port);

                socket.send(responsePacket);
                socket.close();

                Log.d("BroadcastReceiver", "Respuesta enviada a " + senderIp + " con mi IP: " + myIpAddress);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BroadcastReceiver", "Error al enviar la respuesta: " + e.getMessage());
            }
        }).start();
    }
}

