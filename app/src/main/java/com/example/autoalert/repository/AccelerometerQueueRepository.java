package com.example.autoalert.repository;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AccelerometerQueueRepository {

    private static final int MAX_SIZE = 10;

    // Colas para diferentes sensores
    private Queue<String> accelerometerDataQueue;
    private Queue<Double> xValueQueue;
    private Queue<Double> yValueQueue;
    private Queue<Double> zValueQueue;


    private Context context; // Agregar contexto para el archivo

    public AccelerometerQueueRepository(Context context) {
        this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas
        accelerometerDataQueue = new LinkedList<>();
        xValueQueue = new LinkedList<>();
        yValueQueue = new LinkedList<>();
        zValueQueue = new LinkedList<>();
    }

    // Métodos para el Acelerómetro
    public void addAccelerometerData(String data) {

        manageAccelerometerQueue(accelerometerDataQueue);
        accelerometerDataQueue.add(data);
        System.out.println("Se agregó un dato al Acelerómetro: " + data);
        SensorDataWriter.writeDataToFile(context,"Acelerómetro: " + data);

    }
    public void xValueAdd(Double x,double deltaTime){
        manageValueX(xValueQueue,deltaTime);
        xValueQueue.add(x);
        System.out.println( "Se agregó valor X del acelerometro: " + x); // Log para el valor X
    }
    public void yValueAdd(Double y, double deltaTime) {
        manageValueY(yValueQueue,deltaTime);
        yValueQueue.add(y);
        System.out.println("Se agregó valor Y del acelerometro: " + y); // Log para el valor X    }
    }
    public void zValueAdd(Double z,double deltaTime){
        manageValueZ(zValueQueue,deltaTime);
        zValueQueue.add(z);
        System.out.println( "Se agregó valor Z del acelerometro: " + z); // Log para el valor X
    }

    // Método para gestionar la cola del acelerómetro
    private void manageAccelerometerQueue(Queue<String> queue) {
        if (queue.size() == MAX_SIZE) {
            String removedData = queue.poll();
            System.out.println("Se eliminó un dato del Acelerómetro: " + removedData);
            SensorDataWriter.writeDataToFile(context,"Se eliminó un dato del Acelerómetro: " + removedData);

        }
    }
    // Método para gestionar la cola del acelerómetro
    private void manageValueX(Queue<Double> queue,double deltaTime) {
        if (queue.size() == MAX_SIZE) {
            List<Double> dataList = new LinkedList<>(queue);
            double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);

            Double removedData = queue.poll();
            System.out.println( "Se eliminó valor X del aclerometro: " + removedData); // Log cuando se elimina valor X


        }
    }
    private void manageValueY(Queue<Double> queue,double deltaTime) {
        if (queue.size() == MAX_SIZE) {
            List<Double> dataList = new LinkedList<>(queue);

            double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);

            Double removedData = queue.poll();
            System.out.println( "Se eliminó valor Y del acelerometro: " + removedData); // Log cuando se elimina valor X


        }
    }
    private void manageValueZ(Queue<Double> queue,double deltaTime) {
        if (queue.size() == MAX_SIZE) {
            List<Double> dataList = new LinkedList<>(queue);

            double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);

            Double removedData = queue.poll();
            System.out.println( "Se eliminó valor Z del acelerometro: " + removedData); // Log cuando se elimina valor X


        }
    }

    public Queue<String> getAllAccelerometerData() {
        return new LinkedList<>(accelerometerDataQueue);
    }

    public Queue<Double> getAllValueX() {
        return new LinkedList<>(xValueQueue);
    }
    public Queue<Double> getAllValueY() {
        return new LinkedList<>(yValueQueue);
    }
    public Queue<Double> getAllValueZ() {
        return new LinkedList<>(zValueQueue);
    }

    // Limpiar todas las colas
    public void clearAllData() {
        accelerometerDataQueue.clear();
        xValueQueue.clear();
        yValueQueue.clear();
        zValueQueue.clear();

    }
}
