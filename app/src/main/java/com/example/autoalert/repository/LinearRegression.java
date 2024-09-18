package com.example.autoalert.repository;
import java.util.List;

public class LinearRegression {

        public static double calculateLinearRegressionSlope(List<Double> data, double deltaTime) {
            int n = data.size();
            double sumX = 0, sumY = 0, sumXY = 0, sumXSquare = 0;

            // Se asume que los valores de tiempo (X) son los índices multiplicados por deltaTime
            for (int i = 0; i < n; i++) {
                double time = i * deltaTime; // El tiempo depende del deltaTime (que puede variar)
                double value = data.get(i);  // El valor dependiente (eje Y)

                sumX += time;
                sumY += value;
                sumXY += time * value;
                sumXSquare += time * time;
            }

            // Fórmula de la pendiente (m) de una regresión lineal
            return (n * sumXY - sumX * sumY) / (n * sumXSquare - sumX * sumX);
        }
    }


