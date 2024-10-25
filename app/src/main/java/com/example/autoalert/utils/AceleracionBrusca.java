package com.example.autoalert.utils;

import com.example.autoalert.model.entities.DatosMovimiento;

public class AceleracionBrusca {

    public static boolean esAceleracionBrusca(DatosMovimiento punto1, DatosMovimiento punto2, double umbralAceleracion) {
        double cambioVelocidad = punto2.getVelocidad() - punto1.getVelocidad();
        long tiempoDiferencia = punto2.getTiempo() - punto1.getTiempo();
        double aceleracion = cambioVelocidad / (tiempoDiferencia / 1000.0);
        return aceleracion > umbralAceleracion;
    }
}
