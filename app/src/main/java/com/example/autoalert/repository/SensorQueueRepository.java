// SensorQueueRepository.java
package com.example.autoalert.repository;

import java.util.LinkedList;
import java.util.Queue;

public class SensorQueueRepository {

    private static final int MAX_SIZE = 10;

    // Colas para diferentes sensores
    private Queue<String> accelerometerDataQueue;
    private Queue<String> gyroscopeDataQueue;
    private Queue<String> speedDataQueue;

    public SensorQueueRepository() {
        accelerometerDataQueue = new LinkedList<>();
        gyroscopeDataQueue = new LinkedList<>();
        speedDataQueue = new LinkedList<>();
    }

    // Métodos para el Acelerómetro
    public void addAccelerometerData(String data) {
        manageQueueSize(accelerometerDataQueue);
        accelerometerDataQueue.add(data);
    }

    public Queue<String> getAllAccelerometerData() {
        return new LinkedList<>(accelerometerDataQueue);
    }

    // Métodos para el Giroscopio
    public void addGyroscopeData(String data) {
        manageQueueSize(gyroscopeDataQueue);
        gyroscopeDataQueue.add(data);
    }

    public Queue<String> getAllGyroscopeData() {
        return new LinkedList<>(gyroscopeDataQueue);
    }

    // Métodos para la Velocidad
    public void addSpeedData(String data) {
        manageQueueSize(speedDataQueue);
        speedDataQueue.add(data);
    }

    public Queue<String> getAllSpeedData() {
        return new LinkedList<>(speedDataQueue);
    }

    // Método común para gestionar el tamaño de las colas
    private void manageQueueSize(Queue<String> queue) {
        if(queue.size() == MAX_SIZE){
            String removedData = queue.poll(); // Remover el dato más antiguo
            System.out.println("Se eliminó el dato más antiguo: " + removedData);
        }
    }

    // Limpiar todas las colas
    public void clearAllData() {
        accelerometerDataQueue.clear();
        gyroscopeDataQueue.clear();
        speedDataQueue.clear();
    }
}
