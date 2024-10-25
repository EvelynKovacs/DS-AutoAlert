package com.example.autoalert.utils;

import com.example.autoalert.model.entities.DatosMovimiento;

public class AutoParado {

    public  static boolean elAutoEstaParado(DatosMovimiento punto1, DatosMovimiento punto2, DatosMovimiento punto3, double UMBRAL_AUTO_PARADO){

        if(punto1.getVelocidad()<UMBRAL_AUTO_PARADO && punto2.getVelocidad()<UMBRAL_AUTO_PARADO && punto3.getVelocidad()<UMBRAL_AUTO_PARADO){
        return  true;
        }
        return false;
    }
}
