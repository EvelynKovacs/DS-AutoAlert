package com.example.autoalert.repository;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.autoalert.R;
import com.example.autoalert.utils.AccidentDetector;
import com.example.autoalert.utils.SlopeComparator;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AccelerometerQueueRepository {

    private static final int MAX_SIZE = 10;
    private static final int MAX_SIZE_SLOPE = 2;


    // Colas para diferentes sensores
    private Queue<String> accelerometerDataQueue;
    private Queue<Double> xValueQueue;
    private Queue<Double> yValueQueue;
    private Queue<Double> zValueQueue;
    private Queue<Double> xSlopeQueue;
    private Queue<Double> ySlopeQueue;
    private Queue<Double> zSlopeQueue;
    private AccidentDetector accidentDetector;




    private Context context; // Agregar contexto para el archivo

    public AccelerometerQueueRepository(Context context) {
        this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas
        accelerometerDataQueue = new LinkedList<>();
        xValueQueue = new ConcurrentLinkedQueue<>();
        yValueQueue = new ConcurrentLinkedQueue<>();
        zValueQueue = new ConcurrentLinkedQueue<>();
        xSlopeQueue = new ConcurrentLinkedQueue<>();
        ySlopeQueue = new ConcurrentLinkedQueue<>();
        zSlopeQueue = new ConcurrentLinkedQueue<>();

        //this.accidentDetector= AccidentDetector.getInstance();


    }
    // Método para recibir la instancia de AccidentDetector
    public void setAccidentDetector(AccidentDetector accidentDetector) {
        this.accidentDetector = accidentDetector;
    }
    // Métodos para el Acelerómetro
    public void addAccelerometerData(String data) {

        manageAccelerometerQueue(accelerometerDataQueue);
        accelerometerDataQueue.add(data);
        System.out.println("Se agregó un dato al Acelerómetro: " + data);
        SensorDataWriter.writeDataToFile(context,"Acelerómetro: " + data);

    }
    public void xValueAdd(Double x,double deltaTime){
       // Double x3 = Double.parseDouble(String.format(Locale.US, "%.3f", x));

        //manageValueX(xValueQueue,deltaTime);
        manageValue(xValueQueue,xSlopeQueue,"ax",deltaTime);

        xValueQueue.add(x);

        System.out.println( "Se agregó valor X del acelerometro: " + x); // Log para el valor X
        SensorDataWriter.writeDataToFile(context,"Se agregó valor X del acelerometro: " + x);
    }
    public void yValueAdd(Double y, double deltaTime) {

        manageValue(yValueQueue,ySlopeQueue,"ay",deltaTime);
        yValueQueue.add(y);
        System.out.println("Se agregó valor Y del acelerometro: " + y); // Log para el valor X    }
        SensorDataWriter.writeDataToFile(context,"Se agregó valor Y del acelerometro: " + y);

    }
    public void zValueAdd(Double z,double deltaTime){
        manageValue(zValueQueue,zSlopeQueue,"az",deltaTime);
        zValueQueue.add(z);
        System.out.println( "Se agregó valor Z del acelerometro: " + z); // Log para el valor X
        SensorDataWriter.writeDataToFile(context,"Se agregó valor Z del acelerometro: " + z);

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
//    private void manageValueX(Queue<Double> queue,double deltaTime) {
//        if (queue.size() == MAX_SIZE) {
//            List<Double> dataList = new LinkedList<>(queue);
//
//            double newSlope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//
//            // Obtener la pendiente anterior de la cola (la primera pendiente)
//            Double previousSlope = xSlopeQueue.isEmpty() ? 0.0 : xSlopeQueue.peek();
//
//            // Comparar las pendientes
//            if (SlopeComparator.isAccidentDetected(previousSlope, newSlope)) {
//                System.out.println("POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en el eje X.");
//            }
//
//            // Manejar la cola de pendientes (solo 2 pendientes)
//            manageSlopeQueue(xSlopeQueue, newSlope);
//            //double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//            System.out.println("pendiente del acelerometro en X: "+newSlope);
//            SlopeDataWriter.writeSlopeToFile(context,"ax","pendiente del acelerometro en X: "+newSlope);
//            Double removedData = queue.poll();
//            System.out.println( "Se eliminó valor X del aclerometro: " + removedData); // Log cuando se elimina valor X
//            SensorDataWriter.writeDataToFile(context,"Se eliminó valor X del aclerometro: " + removedData);
//
//
//
//        }
//    }
//    private void manageValueY(Queue<Double> queue,double deltaTime) {
//        if (queue.size() == MAX_SIZE) {
//            List<Double> dataList = new LinkedList<>(queue);
//
//
//            double newSlope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//
//            Double previousSlope = ySlopeQueue.isEmpty() ? 0.0 : ySlopeQueue.peek();
//
//            if (SlopeComparator.isAccidentDetected(previousSlope, newSlope)) {
//                System.out.println("POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en el eje Y.");
//            }
//
//            manageSlopeQueue(ySlopeQueue, newSlope);
//
//            //double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//            System.out.println("pendiente del acelerometro en Y: "+newSlope);
//            SlopeDataWriter.writeSlopeToFile(context,"ay","pendiente del acelerometro en Y: "+newSlope);
//
//
//            Double removedData = queue.poll();
//            System.out.println( "Se eliminó valor Y del acelerometro: " + removedData); // Log cuando se elimina valor X
//            SensorDataWriter.writeDataToFile(context,"Se eliminó valor Y del aclerometro: " + removedData);
//
//
//
//        }
//    }
//    private void manageValueZ(Queue<Double> queue,double deltaTime) {
//        if (queue.size() == MAX_SIZE) {
//            List<Double> dataList = new LinkedList<>(queue);
//
//            double newSlope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//
//            Double previousSlope = zSlopeQueue.isEmpty() ? 0.0 : zSlopeQueue.peek();
//
//            if (SlopeComparator.isAccidentDetected(previousSlope, newSlope)) {
//                System.out.println("POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en el eje Z.");
//            }
//
//            manageSlopeQueue(zSlopeQueue, newSlope);
//
//            //double slope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);
//            System.out.println("pendiente del acelerometro en Z: "+newSlope);
//            SlopeDataWriter.writeSlopeToFile(context,"az","pendiente del acelerometro en Z: "+newSlope);
//
//            Double removedData = queue.poll();
//            System.out.println( "Se eliminó valor Z del acelerometro: " + removedData); // Log cuando se elimina valor X
//            SensorDataWriter.writeDataToFile(context,"Se eliminó valor Z del aclerometro: " + removedData);
//
//
//
//        }
  //  }

    private synchronized void manageValue(Queue<Double> valueQueue, Queue<Double> slopeQueue, String axisLabel, double deltaTime) {
        if (valueQueue.size() == MAX_SIZE) {
            List<Double> dataList = new LinkedList<>(valueQueue);

            // Calcular la nueva pendiente
            //double newSlope = LinearRegression.calculateLinearRegressionSlope(dataList, deltaTime);

            Double previousSlope = slopeQueue.isEmpty() ? 0.0 : slopeQueue.peek();

//            if (SlopeComparator.isAccidentDetectedAccelerometer(previousSlope, newSlope)) {
//                System.out.println("POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en el eje " + axisLabel);
//                SensorDataWriter.writeDataToFile(context,"POSIBLE ACCIDENTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE en el eje " + axisLabel + " V: "+ previousSlope+ " N: "+newSlope+" DIF="+Math.floor(Math.abs(newSlope - previousSlope)* 100000) / 100000);
//
//            }
            // Dentro de `manageValue`, después de detectar un posible accidente en un eje
//           boolean isAccidentDetected = SlopeComparator.isAccidentDetected(previousSlope, newSlope);
//           if(isAccidentDetected){}
    //AccidentDetector.shouldTriggerAccidentAlert();

            //accidentDetector.evaluateAccident(isAccidentDetected);


    // Después de procesar los tres ejes (X, Y, Z)
//            if (accidentDetector.shouldTriggerAccidentAlert()) {
//                System.out.println("ALERTAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: Se ha detectado un accidente.");
//                // Aquí puedes disparar la alerta real o llamar a otro método
//                accidentDetector.reset();
//
//            }

// Reiniciar el detector después de la evaluación

            //manageSlopeQueue(slopeQueue, newSlope);

            //System.out.println("pendiente del acelerometro en " + axisLabel + ": " + newSlope);
           //SlopeDataWriter.writeSlopeToFile(context, axisLabel, "pendiente del acelerometro en " + axisLabel + ": " + newSlope);

            Double removedData = valueQueue.poll();
            System.out.println("Se eliminó valor " + axisLabel + " del acelerómetro: " + removedData);
            SensorDataWriter.writeDataToFile(context, "Se eliminó valor " + axisLabel + " del acelerómetro: " + removedData);
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
