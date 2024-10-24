package com.example.autoalert.view.activities;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class SistemaVotacion {

    private MenuInicioActivity mainActivity;

    public SistemaVotacion(MenuInicioActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public boolean iniciarConteo(HashMap<String, String> mapVotes){
        int contPositivo = 0;
        int contNegativo = 0;
        Log.i("Conteo de votos", "Conteo de votos iniciado.");
        for (Map.Entry<String, String> ipVoto : mapVotes.entrySet()) {
            String voto = ipVoto.getValue();
            Log.i("Conteo de votos", "El voto es: " + voto);
            if ("SI".equals(voto)) {
                contPositivo++;
            } else if ("NO".equals(voto)) {
                contNegativo++;
            }
        }
        Log.d("Conteo de votos", "Contador Positivo: " + contPositivo + ". Contador Negativo: " + contNegativo);
        if (contPositivo >= contNegativo) {
            Log.i("Conteo de votos", "Hay Accidente");
            return true;
        } else {
            Log.i("Conteo de votos", "No hubo accidente");
            return false;
        }
    }
}
