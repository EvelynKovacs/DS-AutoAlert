package com.example.autoalert.repository;

import android.content.Context;

import com.example.autoalert.utils.SlopeComparator;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AccelerationQueueRepository {


    private static final int MAX_SIZE = 10;
    private static final int MAX_SIZE_ACCEL = 2;


    // Colas para diferentes sensores
    private Queue<Float> accelerationDataQueue;

    private Context context; // Agregar contexto para el archivo

    public AccelerationQueueRepository(Context context) {
        this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas
        accelerationDataQueue = new LinkedList<>();


    }

    // Métodos para el Acelerómetro
    public void addAccelerationData(float data) {
        manageAccelerationQueue(accelerationDataQueue);
        accelerationDataQueue.add(data);
        System.out.println("Se agregó un dato a la aceleracion: " + data);
        AccelerationDataWriter.writeAcceleration(context,"Aceleracion: " + data);


    }

    public Queue<Float> getAllSpeedData() {
        return new LinkedList<>(accelerationDataQueue);
    }


    private void manageAccelerationQueue(Queue<Float> queue) {
        if (queue.size() == MAX_SIZE) {
            Float removedData = queue.poll();
            System.out.println("Se eliminó un dato de la Aceleracion: " + removedData);
            AccelerationDataWriter.writeAcceleration(context,"Se eliminó un dato de la Aceleracion: " + removedData);

        }
    }





    // Limpiar todas las colas
    public void clearAllData() {
        accelerationDataQueue.clear();
    }

}

