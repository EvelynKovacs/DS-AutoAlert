package com.example.autoalert.utils;


public class KalmanFilter {
    private double q; // Proceso de ruido
    private double r; // Ruido de medida
    private double x; // Valor estimado
    private double p; // Estimación de error

    public KalmanFilter(double processNoise, double measurementNoise) {
        q = processNoise;
        r = measurementNoise;
        x = 0; // Valor inicial
        p = 1; // Estimación inicial
    }

    public double update(double measurement) {
        // Predicción
        p = p + q;

        // Ganancia de Kalman
        double k = p / (p + r);

        // Actualización de la estimación
        x = x + k * (measurement - x);
        p = (1 - k) * p;

        return x; // Valor estimado
    }
}
