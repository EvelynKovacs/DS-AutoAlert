package com.example.autoalert.utils;

public class CambioBrusco {
    public static boolean esCambioBrusco(double angulo, double umbralCambio) {
        // Si el cambio de ángulo supera el umbral, consideramos que es brusco
        return Math.abs(angulo) >= umbralCambio;
    }

}
