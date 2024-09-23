package com.example.autoalert.repository;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import com.example.autoalert.utils.SlopeComparator;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GyroscopeQueueRepository {

    private static final int MAX_SIZE_SLOPE = 2;
        private static final int MAX_SIZE = 10;

        // Colas para diferentes sensores
        private Queue<String> gyroscopeDataQueue;
        private Queue<Double> xValueQueue;
        private Queue<Double> yValueQueue;
        private Queue<Double> zValueQueue;
        private Queue<Double> xSlopeQueue;
        private Queue<Double> ySlopeQueue;
        private Queue<Double> zSlopeQueue;
        private Context context; // Agregar contexto para el archivo

        public GyroscopeQueueRepository(Context context) {
            this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas
            gyroscopeDataQueue = new LinkedList<>();
            xValueQueue = new ConcurrentLinkedQueue<>();
            yValueQueue = new ConcurrentLinkedQueue<>();
            zValueQueue = new ConcurrentLinkedQueue<>();
            xSlopeQueue =  new ConcurrentLinkedQueue<>();
            ySlopeQueue = new ConcurrentLinkedQueue<>();
            zSlopeQueue = new ConcurrentLinkedQueue<>();
        }

        // Métodos para el Giroscopio
        public void addGyroscopeData(String data) {

            manageGyroscopeQueue(gyroscopeDataQueue);
            gyroscopeDataQueue.add(data);
            System.out.println("Se agregó un dato al Giroscopio: " + data);
            SensorDataWriter.writeDataToFile(context,"Giroscopio: " + data);

        }
        public void xValueAdd(Double x,double deltaTime){
            manageValue(xValueQueue,xSlopeQueue,"gx",deltaTime);
            xValueQueue.add(x);
            System.out.println( "Se agregó valor X del giroscopio: " + x); // Log para el valor X
            SensorDataWriter.writeDataToFile(context,"Se agregó valor X del giroscopio: " + x);

        }
        public void yValueAdd(Double y,double deltaTime) {
            manageValue(yValueQueue,ySlopeQueue,"gy",deltaTime);
            yValueQueue.add(y);
            System.out.println("Se agregó valor Y del giroscopio: " + y); // Log para el valor // }
            SensorDataWriter.writeDataToFile(context,"Se agregó valor Y del giroscopio: " + y);

        }
        public void zValueAdd(Double z,double deltaTime){
            manageValue(zValueQueue,zSlopeQueue,"gz",deltaTime);
            zValueQueue.add(z);
            System.out.println( "Se agregó valor Z del giroscopio: " + z); // Log para el valor X
            SensorDataWriter.writeDataToFile(context,"Se agregó valor Z del giroscopio: " + z);

        }

        // Método para gestionar la cola del giroscopio
        private void manageGyroscopeQueue(Queue<String> queue) {
            if (queue.size() == MAX_SIZE) {
                String removedData = queue.poll();
                System.out.println("Se eliminó un dato del Giroscopio: " + removedData);
                SensorDataWriter.writeDataToFile(context,"Se eliminó un dato del Giroscopio: " + removedData);

            }
        }

//        private void manageValueX(Queue<Double> queue,double deltaTime) {
//            if (queue.size() == MAX_SIZE) {
//                List<Double> dataList = new LinkedList<>(queue);
//                double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//                System.out.println("pendiente del giroscopio en X: "+slope);
//                SlopeDataWriter.writeSlopeToFile(context,"gx","pendiente del giroscopio en X: "+slope);
//                Double removedData = queue.poll();
//                System.out.println( "Se eliminó valor X del giroscopio: " + removedData); // Log cuando se elimina valor X
//                SensorDataWriter.writeDataToFile(context,"Se eliminó valor X del giroscopio: " + removedData);
//
//
//
//            }
//        }
//        private void manageValueY(Queue<Double> queue,double deltaTime) {
//            if (queue.size() == MAX_SIZE) {
//                List<Double> dataList = new LinkedList<>(queue);
//                double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//                System.out.println("pendiente del giroscopio en Y: "+slope);
//                SlopeDataWriter.writeSlopeToFile(context,"gy","pendiente del giroscopio en Y: "+slope);
//                Double removedData = queue.poll();
//                System.out.println( "Se eliminó valor Y del giroscopio: " + removedData); // Log cuando se elimina valor X
//                SensorDataWriter.writeDataToFile(context,"Se eliminó valor Y del giroscopio: " + removedData);
//
//
//
//            }
//        }
//        private void manageValueZ(Queue<Double> queue,double deltaTime) {
//            if (queue.size() == MAX_SIZE) {
//                List<Double> dataList = new LinkedList<>(queue);
//                double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//                System.out.println("pendiente del girocopio en Z: "+slope);
//                SlopeDataWriter.writeSlopeToFile(context,"gz","pendiente del giroscopio en Z: "+slope);
//                Double removedData = queue.poll();
//                System.out.println( "Se eliminó valor Z del giroscopio: " + removedData); // Log cuando se elimina valor X
//                SensorDataWriter.writeDataToFile(context,"Se eliminó valor Y del giroscopio: " + removedData);
//
//
//
//            }
//        }

    private synchronized void manageValue(Queue<Double> valueQueue, Queue<Double> slopeQueue, String axisLabel, double deltaTime) {
        if (valueQueue.size() == MAX_SIZE) {
            List<Double> dataList = new LinkedList<>(valueQueue);

            // Calcular la nueva pendiente
            double newSlope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);

            Double previousSlope = slopeQueue.isEmpty() ? 0.0 : slopeQueue.peek();

            if (SlopeComparator.isAccidentDetectedGyroscope(previousSlope, newSlope)) {
                System.out.println("POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en el eje " + axisLabel);
                SensorDataWriter.writeDataToFile(context,"POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en el eje " + axisLabel);

            }

            manageSlopeQueue(slopeQueue, newSlope);

            System.out.println("pendiente del giroscopio en " + axisLabel + ": " + newSlope);
            SlopeDataWriter.writeSlopeToFile(context, axisLabel, "pendiente del giroscopio en " + axisLabel + ": " + newSlope);

            Double removedData = valueQueue.poll();
            System.out.println("Se eliminó valor " + axisLabel + " del giroscopio: " + removedData);
            SensorDataWriter.writeDataToFile(context, "Se eliminó valor " + axisLabel + " del giroscopio: " + removedData);
        }
    }


    private void manageSlopeQueue(Queue<Double> slopeQueue, double newSlope) {
        // Si la cola tiene más de 2 pendientes, eliminar la más antigua
        if (slopeQueue.size() == MAX_SIZE_SLOPE) {
            slopeQueue.poll(); // Eliminar la pendiente más antigua
        }
        // Agregar la nueva pendiente
        slopeQueue.add(newSlope);
    }

        public Queue<String> getAllGyroscopeData() {
            return new LinkedList<>(gyroscopeDataQueue);
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
            gyroscopeDataQueue.clear();
            xValueQueue.clear();
            yValueQueue.clear();
            zValueQueue.clear();

        }
    }


