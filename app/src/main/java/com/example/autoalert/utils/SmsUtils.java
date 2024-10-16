package com.example.autoalert.utils;


import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class SmsUtils {

    public static void enviarSMS(Context context, String[] numeros, String mensaje) {
        SmsManager smsManager = SmsManager.getDefault();
        System.out.println("LLego al enviar SMS");

        // Crear constantes para los intents
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        // Registrar los intentos de envío
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case AppCompatActivity.RESULT_OK:
                            Toast.makeText(context, "SMS enviado", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(context, "Error genérico en el envío", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(context, "No hay servicio disponible", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, "PDU nulo", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(context, "Error: Radio apagada", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(SENT), Context.RECEIVER_NOT_EXPORTED);
        }

        // Registrar los intentos de entrega
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case AppCompatActivity.RESULT_OK:
                            Toast.makeText(context, "SMS entregado", Toast.LENGTH_SHORT).show();
                            break;
                        case AppCompatActivity.RESULT_CANCELED:
                            Toast.makeText(context, "Error: SMS no entregado", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(DELIVERED), Context.RECEIVER_NOT_EXPORTED);
        }

        for (int i = 0; i < numeros.length; i++) {
            String numero = numeros[i];
            int delay = i * 1000; // Retraso de 2 segundos entre cada mensaje

            // Crear PendingIntents para el envío y la entrega con FLAG_IMMUTABLE
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            ArrayList<PendingIntent> deliveredIntents = new ArrayList<>();

            // Crear un PendingIntent para cada parte del mensaje
            for (int j = 0; j < smsManager.divideMessage(mensaje).size(); j++) {
                sentIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SENT), PendingIntent.FLAG_IMMUTABLE));
                deliveredIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE));
            }

            new Handler().postDelayed(() -> {
                // Dividir el mensaje si es muy largo
                ArrayList<String> partes = smsManager.divideMessage(mensaje);

                // Enviar las partes del mensaje
                smsManager.sendMultipartTextMessage(numero, null, partes, sentIntents, deliveredIntents);
            }, delay);
        }
    }

    public static void checkAndSendSms(AppCompatActivity activity, String[] numeros, String mensaje) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, 1);
        } else {
            enviarSMS(activity, numeros, mensaje);
        }
    }
}
