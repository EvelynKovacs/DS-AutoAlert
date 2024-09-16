// SensorQueueRepository.java
package com.example.autoalert.repository;

import android.content.Context;

import java.util.LinkedList;
import java.util.Queue;

public class SensorQueueRepository {

    private static final int MAX_SIZE = 10;

    // Colas para diferentes sensores
    private Queue<String> accelerometerDataQueue;
    private Queue<String> gyroscopeDataQueue;
    private Queue<String> speedDataQueue;
    private Context context; // Agregar contexto para el archivo

    public SensorQueueRepository(Context context) {
        this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas
        accelerometerDataQueue = new LinkedList<>();
        gyroscopeDataQueue = new LinkedList<>();
        speedDataQueue = new LinkedList<>();
    }

    // Métodos para el Acelerómetro
    public void addAccelerometerData(String data) {

        manageAccelerometerQueue(accelerometerDataQueue);
        accelerometerDataQueue.add(data);
        System.out.println("Se agregó un dato al Acelerómetro: " + data);
        SensorDataWriter.writeDataToFile(context,"Acelerómetro: " + data);

    }

    public Queue<String> getAllAccelerometerData() {
        return new LinkedList<>(accelerometerDataQueue);
    }

    // Métodos para el Giroscopio
    public void addGyroscopeData(String data) {
        manageGyroscopeQueue(gyroscopeDataQueue);
        gyroscopeDataQueue.add(data);
        System.out.println("Se agregó un dato al Giroscopio: " + data);
        SensorDataWriter.writeDataToFile(context,"Giroscopio: " + data);


    }

    public Queue<String> getAllGyroscopeData() {
        return new LinkedList<>(gyroscopeDataQueue);
    }

    // Métodos para la Velocidad
    public void addSpeedData(String data) {
        manageVelocityQueue(speedDataQueue);
        speedDataQueue.add(data);
        System.out.println("Se agregó un dato a la velocidad: " + data);
        SensorDataWriter.writeDataToFile(context,"Velocidad: " + data);


    }

    public Queue<String> getAllSpeedData() {
        return new LinkedList<>(speedDataQueue);
    }

    // Método para gestionar la cola del acelerómetro
    private void manageAccelerometerQueue(Queue<String> queue) {
        if (queue.size() == MAX_SIZE) {
            String removedData = queue.poll();
            System.out.println("Se eliminó un dato del Acelerómetro: " + removedData);
            SensorDataWriter.writeDataToFile(context,"Se eliminó un dato del Acelerómetro: " + removedData);

        }
    }

    // Método para gestionar la cola del giroscopio
    private void manageGyroscopeQueue(Queue<String> queue) {
        if (queue.size() == MAX_SIZE) {
            String removedData = queue.poll();
            System.out.println("Se eliminó un dato del Giroscopio: " + removedData);
            SensorDataWriter.writeDataToFile(context,"Se eliminó un dato del Giroscopio: " + removedData);

        }
    }

    // Método para gestionar la cola de la velocidad
    private void manageVelocityQueue(Queue<String> queue) {
        if (queue.size() == MAX_SIZE) {
            String removedData = queue.poll();
            System.out.println("Se eliminó un dato de la Velocidad: " + removedData);
            SensorDataWriter.writeDataToFile(context,"Se eliminó un dato de la Velocidad: " + removedData);

        }
    }

    // Limpiar todas las colas
    public void clearAllData() {
        accelerometerDataQueue.clear();
        gyroscopeDataQueue.clear();
        speedDataQueue.clear();
    }
}
