package com.example.autoalert.view.activities;

import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

public class SistemaVotación {

    private MainActivity mainActivity;
    private MessageSender messageSender;

    public SistemaVotación(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void guardarVoto(String ip, String voto){

        Log.i("Guardado de votos", "Se inicia el guardado de votos.");
        String[] votoArray = voto.split(":");
        String resultadoVoto = votoArray[1];
        Log.i("Guardado de votos", "Se obtiene voto: " + resultadoVoto);

        mainActivity.storeMessageFromIp(ip, resultadoVoto);

        // Mostrar el mensaje recibido en la interfaz
        // Actualizar la interfaz con el contenido del HashMap

        mainActivity.sumarContador();
        Log.i("Guardado de votos", "El contador esta en " + mainActivity.getContador());

        if(mainActivity.getIpList().size() == mainActivity.getContador()){
            Log.i("Recoleccion de estados", "Se obtuvieron los estados de todos los dispositivos. Se inicia el conteo de votos.");
            iniciarConteo();
            mainActivity.reiniciarContador();

        }
    }

    public void iniciarConteo(){
        int contPositivo = 0;
        int contNegativo = 0;
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        Log.i("Conteo de votos", "Conteo de votos iniciado.");

        for (String ip: mainActivity.getIpMessageMap().keySet()) {
            String voto = mainActivity.getIpMessageMap().get(ip);
            if ("SI".equals(voto)) {
                contPositivo++;
                Log.i("Conteo de votos", "VOTO:SI");
            } else if ("NO".equals(voto)) {
                contNegativo++;
                Log.i("Conteo de votos", "VOTO:NO");
            }
        }
        Log.i("Conteo de votos", "Añadiendo voto del propio dispositivo.");
        if(mainActivity.getResponseText().equals("SI")){
            Log.i("Conteo de votos", "Se añade un VOTO:SI");
            contPositivo++;
        } else {
            Log.i("Conteo de votos", "Se añade un VOTO:NO");
            contNegativo++;
        }

        // Mostrar resultado basado en la cantidad de votos
        if (contPositivo >= contNegativo) {
            Log.i("Conteo de votos", "Hay Accidente");
            displayText.append("Resultado Votación: HAY ACCIDENTE").append("\n");
            mainActivity.setResultadoText("Resultado Votación: HAY ACCIDENTE");
        } else {
            Log.i("Conteo de votos", "No hubo accidente");
            displayText.append("Resultado Votación: NO HUBO ACCIDENTE").append("\n");
            mainActivity.setResultadoText("Resultado Votación: NO HAY ACCIDENTE");
        }
    }

    public void enviarEstado(){
        String message;
        Log.i("Envio de Estado", "Enviando estado");
        if(mainActivity.getResponseText().equals("SI")){
            message = "VOTO:SI";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:SI");

        } else {
            message = "VOTO:NO";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:NO");

        }



        // Supongamos que quieres enviar el mensaje a la primera IP de la lista
        if(mainActivity.getIpList().isEmpty()){
            //String targetIp = ipList.get(0); // Usar la IP que quieras de la lista
            for(String targetIp : mainActivity.getIpList()) {
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de Estado", "Enviando mensaje a " + targetIp + " con: VOTO:NO");
            }
        }
    }
}
