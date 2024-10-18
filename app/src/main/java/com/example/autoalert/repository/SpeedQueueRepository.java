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

public class SpeedQueueRepository {


        private static final int MAX_SIZE = 2;
        private static final int MAX_SIZE_SLOPE = 2;


    // Colas para diferentes sensores
        private Queue<Double> speedDataQueue;
        private  Queue<Double> speedSlopeQueue;

        private Context context; // Agregar contexto para el archivo

        public SpeedQueueRepository(Context context) {
            this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas
            speedDataQueue = new LinkedList<>();
            speedSlopeQueue = new ConcurrentLinkedQueue<>();


        }

        // Métodos para el Acelerómetro
    public void addSpeedData(Double data,double deltaTime) {
        manageSpeedQueue(speedDataQueue,speedSlopeQueue,deltaTime);
        speedDataQueue.add(data);
        System.out.println("Se agregó un dato a la velocidad: " + data);
        //SensorDataWriter.writeDataToFile(context,"Velocidad: " + data);


    }

    public Queue<Double> getAllSpeedData() {
        return new LinkedList<>(speedDataQueue);
    }


    private void manageSpeedQueue(Queue<Double> queue,Queue<Double> slopeQueue,double deltaTime) {
        if (queue.size() == MAX_SIZE) {
            List<Double> dataList = new LinkedList<>(queue);

           if (SlopeComparator.isAccidentDetectedSpeed(dataList.get(1), dataList.get(0))){
               System.out.println("POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en Velocidad ");
          //      SensorDataWriter.writeDataToFile(context,"POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en Velocidad"+" VA: "+ dataList.get(0)+ " VI: "+dataList.get(1));

           }

            // Calcular la nueva pendiente
           // double newSlope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);

            //Double previousSlope = slopeQueue.isEmpty() ? 0.0 : slopeQueue.peek();

//            if (SlopeComparator.isAccidentDetectedSpeed(previousSlope, newSlope)) {
//                System.out.println("POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en Velocidad ");
//                SensorDataWriter.writeDataToFile(context,"POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en Velocidad"+" V: "+ previousSlope+ " N: "+newSlope+ " DIF="+  Math.floor(Math.abs(newSlope - previousSlope)* 100000) / 100000);
//            }

            //manageSlopeQueue(slopeQueue, newSlope);
            //System.out.println("pendiente de la velocidad: "+newSlope);
           // SlopeDataWriter.writeSlopeToFile(context,"v","pendiente de la velocidad: "+newSlope);
            Double removedData = queue.poll();
            System.out.println("Se eliminó un dato de la Velocidad: " + removedData);
        //    SensorDataWriter.writeDataToFile(context,"Se eliminó un dato de la Velocidad: " + removedData);

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


    // Limpiar todas las colas
    public void clearAllData() {
        speedDataQueue.clear();
    }

    }


