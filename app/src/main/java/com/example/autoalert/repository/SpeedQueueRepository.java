package com.example.autoalert.repository;

import android.content.Context;

import java.util.LinkedList;
import java.util.Queue;
import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class SpeedQueueRepository {


        private static final int MAX_SIZE = 10;

        // Colas para diferentes sensores
        private Queue<Double> speedDataQueue;

        private Context context; // Agregar contexto para el archivo

        public SpeedQueueRepository(Context context) {
            this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas
            speedDataQueue = new LinkedList<>();

        }

        // Métodos para el Acelerómetro
    public void addSpeedData(Double data) {
        manageSpeedQueue(speedDataQueue);
        speedDataQueue.add(data);
        System.out.println("Se agregó un dato a la velocidad: " + data);
        SensorDataWriter.writeDataToFile(context,"Velocidad: " + data);


    }

    public Queue<Double> getAllSpeedData() {
        return new LinkedList<>(speedDataQueue);
    }
    private void manageSpeedQueue(Queue<Double> queue) {
        if (queue.size() == MAX_SIZE) {
            Double removedData = queue.poll();
            System.out.println("Se eliminó un dato de la Velocidad: " + removedData);
            SensorDataWriter.writeDataToFile(context,"Se eliminó un dato de la Velocidad: " + removedData);

        }
    }

    // Limpiar todas las colas
    public void clearAllData() {
        speedDataQueue.clear();
    }

    }


