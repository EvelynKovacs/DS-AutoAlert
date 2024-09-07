// SensorQueueRepository.java
package com.example.autoalert.repository;

import java.util.LinkedList;
import java.util.Queue;

public class SensorQueueRepository {

    private static final int MAX_SIZE = 10;
    private Queue<String> sensorDataQueue;

    public SensorQueueRepository() {
        sensorDataQueue = new LinkedList<>();
    }

    // Agregar datos a la cola
    public void addSensorData(String data) {
        if(sensorDataQueue.size() == MAX_SIZE){
            String removedData = sensorDataQueue.poll(); // Remover el dato más antiguo
            System.out.println("Se eliminó el dato más antiguo: " + removedData);
        }
        sensorDataQueue.add(data);
    }


    // Obtener datos de la cola
    public String pollSensorData() {
        return sensorDataQueue.poll(); // Devuelve y remueve el primer elemento
    }

    // Verificar si la cola está vacía
    public boolean isQueueEmpty() {
        return sensorDataQueue.isEmpty();
    }
    // Obtener todos los datos sin eliminarlos
    public Queue<String> getAllSensorData() {
        return new LinkedList<>(sensorDataQueue);  // Devolver una copia de la cola
    }

    // Obtener el tamaño de la cola
    public int getQueueSize() {
        return sensorDataQueue.size();
    }

    // Limpiar la cola
    public void clearQueue() {
        sensorDataQueue.clear();
    }
}
