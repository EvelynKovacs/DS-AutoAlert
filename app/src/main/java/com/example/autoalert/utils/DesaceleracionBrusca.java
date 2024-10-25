package com.example.autoalert.utils;

import com.example.autoalert.model.entities.DatosMovimiento;

public class DesaceleracionBrusca {

    public static boolean esDesaceleracionBrusca(DatosMovimiento punto1, DatosMovimiento punto2, double umbralDesaceleracion) {
        double cambioVelocidad = punto1.getVelocidad() - punto2.getVelocidad();
        long tiempoDiferencia = punto2.getTiempo() - punto1.getTiempo();  // Tiempo en milisegundos
        double desaceleracion = cambioVelocidad / (tiempoDiferencia / 1000.0);  // metros/seg^2
        return desaceleracion > umbralDesaceleracion;
    }

}
