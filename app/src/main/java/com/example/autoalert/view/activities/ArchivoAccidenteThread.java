package com.example.autoalert.view.activities;

import android.location.Location;
import android.util.Log;

import com.example.autoalert.viewmodel.SpeedViewModel;

import java.util.List;

public class ArchivoAccidenteThread {

    private MenuInicioActivity mainActivity;
    List<Location> lista;
    private SpeedViewModel speedViewModel;

    public ArchivoAccidenteThread(MenuInicioActivity mainActivity, List<Location> listaUbicaciones,SpeedViewModel speedViewModel) {
        this.mainActivity = mainActivity;
        this.lista = listaUbicaciones;
        this.speedViewModel = speedViewModel;
    }

    public void startAccidente() {
        new Thread(() -> {
                try {
                    for (int i = 0; i < lista.size(); i++) {
                        Location location = lista.get(i);
                        System.out.println("Latitud: " + location.getLatitude() + ", Longitud: " + location.getLongitude());
                        speedViewModel.updateLocation(location);
                        Thread.sleep(1000);
                    }
                    Log.e("ArchivoAccidenteThread", "Termino de Leer");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("BroadcastTimer", "Error al enviar el mensaje de broadcast: " + e.getMessage());

                }
        }).start();
    }

}
