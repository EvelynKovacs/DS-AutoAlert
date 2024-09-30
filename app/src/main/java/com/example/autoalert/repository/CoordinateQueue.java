package com.example.autoalert.repository;
// Clase para manejar una cola fija de 3 puntos de coordenadas (latitud y longitud)
public class CoordinateQueue {
    private final double[][] coordinatesQueue = new double[2][3]; // Matriz 2x3
    private int coordinateCount = 0; // Para contar cuántos puntos hay almacenados

    // Método para agregar coordenadas (simula una cola)
    public void addCoordinates(double latitude, double longitude) {
        if (coordinateCount == 3) {
            // Desplazar datos hacia la izquierda (FIFO)
            coordinatesQueue[0][0] = coordinatesQueue[0][1];
            coordinatesQueue[1][0] = coordinatesQueue[1][1];
            coordinatesQueue[0][1] = coordinatesQueue[0][2];
            coordinatesQueue[1][1] = coordinatesQueue[1][2];
        } else {
            coordinateCount++;
        }

        // Agregar nueva coordenada en la tercera posición
        coordinatesQueue[0][coordinateCount - 1] = latitude;
        coordinatesQueue[1][coordinateCount - 1] = longitude;
    }

    // Obtener la cantidad actual de coordenadas almacenadas
    public int getCoordinateCount() {
        return coordinateCount;
    }

    // Método para obtener una latitud por su índice (0, 1 o 2)
    public double getLatitude(int index) {
        return coordinatesQueue[0][index];
    }

    // Método para obtener una longitud por su índice (0, 1 o 2)
    public double getLongitude(int index) {
        return coordinatesQueue[1][index];
    }
}

