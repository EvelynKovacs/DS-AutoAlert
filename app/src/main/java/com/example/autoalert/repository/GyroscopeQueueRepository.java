package com.example.autoalert.repository;

import android.content.Context;

import java.util.LinkedList;
import java.util.Queue;
import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class GyroscopeQueueRepository {


        private static final int MAX_SIZE = 10;

        // Colas para diferentes sensores
        private Queue<String> gyroscopeDataQueue;
        private Queue<Double> xValueQueue;
        private Queue<Double> yValueQueue;
        private Queue<Double> zValueQueue;
        private Context context; // Agregar contexto para el archivo

        public GyroscopeQueueRepository(Context context) {
            this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas
            gyroscopeDataQueue = new LinkedList<>();
            xValueQueue = new LinkedList<>();
            yValueQueue = new LinkedList<>();
            zValueQueue = new LinkedList<>();
        }

        // Métodos para el Giroscopio
        public void addGyroscopeData(String data) {

            manageGyroscopeQueue(gyroscopeDataQueue);
            gyroscopeDataQueue.add(data);
            System.out.println("Se agregó un dato al Giroscopio: " + data);
            SensorDataWriter.writeDataToFile(context,"Giroscopio: " + data);

        }
        public void xValueAdd(Double x){
            manageValueX(xValueQueue);
            xValueQueue.add(x);
            System.out.println( "Se agregó valor X del giroscopio: " + x); // Log para el valor X
        }
        public void yValueAdd(Double y) {
            manageValueY(yValueQueue);
            yValueQueue.add(y);
            System.out.println("Se agregó valor Y del giroscopio: " + y); // Log para el valor X    }
        }
        public void zValueAdd(Double z){
            manageValueZ(zValueQueue);
            zValueQueue.add(z);
            System.out.println( "Se agregó valor Z del giroscopio: " + z); // Log para el valor X
        }

        // Método para gestionar la cola del giroscopio
        private void manageGyroscopeQueue(Queue<String> queue) {
            if (queue.size() == MAX_SIZE) {
                String removedData = queue.poll();
                System.out.println("Se eliminó un dato del Giroscopio: " + removedData);
                SensorDataWriter.writeDataToFile(context,"Se eliminó un dato del Giroscopio: " + removedData);

            }
        }

        private void manageValueX(Queue<Double> queue) {
            if (queue.size() == MAX_SIZE) {
                Double removedData = queue.poll();
                System.out.println( "Se eliminó valor X del giroscopio: " + removedData); // Log cuando se elimina valor X


            }
        }
        private void manageValueY(Queue<Double> queue) {
            if (queue.size() == MAX_SIZE) {
                Double removedData = queue.poll();
                System.out.println( "Se eliminó valor Y del giroscopio: " + removedData); // Log cuando se elimina valor X


            }
        }
        private void manageValueZ(Queue<Double> queue) {
            if (queue.size() == MAX_SIZE) {
                Double removedData = queue.poll();
                System.out.println( "Se eliminó valor Z del giroscopio: " + removedData); // Log cuando se elimina valor X


            }
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


