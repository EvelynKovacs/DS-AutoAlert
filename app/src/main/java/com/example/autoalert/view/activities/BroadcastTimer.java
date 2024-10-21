package com.example.autoalert.view.activities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class BroadcastTimer {

    //private BroadcastSender broadcastSender = new BroadcastSender();
    private MainActivity mainActivity;

    public BroadcastTimer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public void startBroadcastTimer() {
        new Thread(() -> {
            while(true) {
                try {
                    // Pausa el hilo durante 30 segundos (30000 milisegundos)
                    Thread.sleep(30000);
//                    //////////////////////////////////////////////
//
//                    // Obtener la hora actual en milisegundos
//                    Calendar calendar = Calendar.getInstance();
//                    long primerTimestamp = calendar.getTimeInMillis();
//                    Log.i("Diferencia de Tiempo", "Primer timestamp en Long: " + primerTimestamp);
//                    String primerTimestampString = Long.toString(primerTimestamp);
//                    Log.i("Diferencia de Tiempo", "Primer timestamp en String: " + primerTimestampString);
//
//                    // Obtener la hora actual en formato HH:mm:ss para logueo
//                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
//                    int minute = calendar.get(Calendar.MINUTE);
//                    int second = calendar.get(Calendar.SECOND);
//                    String timeString = String.format("%02d:%02d:%02d", hour, minute, second);
//
//
//                    Log.i("Diferencia de Tiempo","Primer tiempo: " + timeString);
                    ///////////////////////////////////
                    mainActivity.sendBroadcast();
//                    Thread.sleep(4000);
//                    Log.i("Verificacion Conexion", "Terminó Timer. Comienza verificacion de conexion");
//                    mainActivity.verificarConexion();

                    /////////////////////////////////////////
//                    // Obtener el tiempo actual del sistema nuevamente
//                    long primerTimestampRecuperado = Long.parseLong(primerTimestampString);
//                    Log.i("Diferencia de Tiempo", "Primer tiempo de String a Long: " + primerTimestampRecuperado);
//                    long segundoTimestamp = Calendar.getInstance().getTimeInMillis();
//                    Log.i("Diferencia de Tiempo", "Segundo tiempo en milisegundos: " + segundoTimestamp);
//
//                    // Calcular la diferencia en milisegundos
//                    long diferenciaEnMilisegundos = segundoTimestamp - primerTimestampRecuperado;
//                    long diferenciaEnSegundos = diferenciaEnMilisegundos / 1000;
//
//                    Log.i("Diferencia de Tiempo", "Diferencia en segundos: " + diferenciaEnSegundos);
//
//                    if(diferenciaEnSegundos >= 4) {
//                        Log.i("Diferencia de Tiempo", "Me desconecte. Tiempo mayor o igual a 4 seg.");
//                    }



                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("BroadcastTimer", "Error al enviar el mensaje de broadcast: " + e.getMessage());

                }
            }
        }).start();
    }

}


