package com.example.autoalert.utils;

import com.example.autoalert.model.entities.DatosMovimiento;

public class CalcularAngulo {

//    public static double calcularAngulo(DatosMovimiento punto1, DatosMovimiento punto2, DatosMovimiento punto3) {
//        // Diferencias en latitud y longitud entre los puntos
//        double angulo1 = Math.atan2(punto2.getLatitud() - punto1.getLatitud(), punto2.getLongitud() - punto1.getLongitud());
//        double angulo2 = Math.atan2(punto3.getLatitud() - punto2.getLatitud(), punto3.getLongitud() - punto2.getLongitud());
//
//        // Calculamos la diferencia de los ángulos y convertimos a grados
//        double anguloDiferencia = Math.toDegrees(angulo2 - angulo1);
//
//        // Aseguramos que el ángulo esté entre 0 y 360 grados
//        if (anguloDiferencia < 0) {
//            anguloDiferencia += 360;
//        }
//
//        return anguloDiferencia;
//    }

    public static double calcularAngulo(DatosMovimiento punto1, DatosMovimiento punto2, DatosMovimiento punto3) {
        // Coordenadas de los puntos
        double x1 = punto1.getLongitud();
        double y1 = punto1.getLatitud();
        double x2 = punto2.getLongitud();
        double y2 = punto2.getLatitud();
        double x3 = punto3.getLongitud();
        double y3 = punto3.getLatitud();

        // Vectores AB y BC
        double[] AB = {x2 - x1, y2 - y1};
        double[] BC = {x3 - x2, y3 - y2};

        // Producto punto
        double productoPunto = AB[0] * BC[0] + AB[1] * BC[1];

        // Magnitudes
        double magnitudAB = Math.sqrt(AB[0] * AB[0] + AB[1] * AB[1]);
        double magnitudBC = Math.sqrt(BC[0] * BC[0] + BC[1] * BC[1]);

        // Cálculo del coseno
        double cosTheta = productoPunto / (magnitudAB * magnitudBC);

        // Cálculo del ángulo en radianes
        double anguloRadianes = Math.acos(cosTheta);

        // Convertir a grados
        double anguloGrados = Math.toDegrees(anguloRadianes);
        return anguloGrados;
    }


}
