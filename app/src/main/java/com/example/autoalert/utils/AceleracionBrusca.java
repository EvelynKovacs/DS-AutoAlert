package com.example.autoalert.utils;

import com.example.autoalert.model.entities.DatosMovimiento;

public class AceleracionBrusca {



    public static boolean esAceleracionBrusca(DatosMovimiento punto1, DatosMovimiento punto2, double umbral) {
        double diferenciaVelocidad = punto2.getVelocidad() - punto1.getVelocidad();  // Diferencia en km/h
        return diferenciaVelocidad >= umbral;  // Verifica si la aceleración fue brusca
    }
}
