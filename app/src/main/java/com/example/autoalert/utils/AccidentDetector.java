package com.example.autoalert.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.autoalert.R;
import com.example.autoalert.repository.LinesAngleDataWriter;

public class AccidentDetector {
     private Context context; // Agregar contexto para el archivo

     public   AccidentDetector(Context context){
          this.context=context;
     }

     // Método para detectar si el ángulo entre dos líneas formadas por tres coordenadas indica un accidente
     public boolean detectAccident(double coordX1, double coordY1,
                                   double coordX2, double coordY2,
                                   double coordX3, double coordY3) {

          // Calcular la pendiente de la primera línea (entre el primer y segundo punto)
          double firstSlope = calculateSlope(coordX1, coordY1, coordX2, coordY2);

          // Calcular la pendiente de la segunda línea (entre el segundo y tercer punto)
          double secondSlope = calculateSlope(coordX2, coordY2, coordX3, coordY3);

          // Calcular el ángulo entre las dos líneas
          double angle = calculateAngleBetweenLines(firstSlope, secondSlope);

          LinesAngleDataWriter.writeLinesDataToFile(context,
                  "P1=(" + coordX1 + "," + coordY1 + ") " +
                          "P2=(" + coordX2 + "," + coordY2 + ") " +
                          "P3=(" + coordX3 + "," + coordY3 + ")\n" +
                          "Pendiente1=" + firstSlope + " Pendiente2=" + secondSlope +
                          " Ángulo=" + angle);

          // Verificar si el ángulo entre las dos líneas es pronunciado (entre 70 y 90 grados)
          if (Math.abs(angle) >= 70) {
               // Accident detected
               LinesAngleDataWriter.writeLinesDataToFile(context, "ACCIDENTE DETECTADO ÁNGULO=" + angle);
               System.out.println("Accident detected: angle = " + angle);
               return true;
          }

          // No accident detected
          System.out.println("No accident: angle = " + angle);
          return false;
     }
     // Método para calcular la pendiente de una línea entre dos puntos (x1, y1) y (x2, y2)
     public static double calculateSlope(double x1, double y1, double x2, double y2) {
          // Evitar la división por cero
          if (x2 - x1 == 0) {
               return Double.POSITIVE_INFINITY; // Pendiente infinita (línea vertical)
          }
          return (y2 - y1) / (x2 - x1); // m = (y2 - y1) / (x2 - x1)
     }
     // Method to calculate the angle between two lines with slopes m1 and m2
     public static double calculateAngleBetweenLines(double slope1, double slope2) {
          System.out.println("PEND1="+slope1+ " PEND2="+slope2);
          if (Double.isInfinite(slope1) && slope2 == 0.0) {
               return 90.0; // A vertical and a horizontal line always form a 90-degree angle
          } else if (Double.isInfinite(slope2) && slope1 == 0.0) {
               return 90.0; // Same case but reversed
          } else if (Double.isInfinite(slope1) || Double.isInfinite(slope2)) {
               return 90.0; // General case for any line with an infinite slope
          }
          // Formula for the angle between two lines: arctan((m1 - m2) / (1 + m1 * m2))
          double tanAlpha = Math.abs((slope1 - slope2) / (1 + slope1 * slope2));

          // Calculate the angle in radians
          double angleRadians = Math.atan(tanAlpha);

          // Convert the result to degrees
          return Math.toDegrees(angleRadians);
     }





//     // Method to detect if the angle between two coordinates indicates an accident
//     public  boolean detectAccident(double previousCoordinateX, double previousCoordinateY,
//                                    double currentCoordinateX, double currentCoordinateY) {
//
//          // Calculate the slope of the first line (between (0,0) and the previous coordinate)
//          double previousSlope = calculateSlope(previousCoordinateX, previousCoordinateY);
//
//          // Calculate the slope of the second line (between (0,0) and the current coordinate)
//          double currentSlope = calculateSlope(currentCoordinateX, currentCoordinateY);
//
//          // Calculate the angle between the two lines
//          double angle = calculateAngleBetweenLines(previousSlope, currentSlope);
//
//          LinesAngleDataWriter.writeLinesDataToFile(context,"CX1="+previousCoordinateX+ " CY1="+previousCoordinateX+ " CX2A="+currentCoordinateX+ " CY2A="+currentCoordinateY+"\nPA="+previousSlope+" PN="+currentSlope+" ANG="+angle );
//
//          // Check if the angle between the lines is sharp (between 70 and 90 degrees)
//          if (Math.abs(angle) >= 70 ) {
//               // Accident detected
//               LinesAngleDataWriter.writeLinesDataToFile(context,"ACCIDENTE DETECTADO ANGULO="+angle);
//               System.out.println("Accident detected: angle = " + angle);
//               return true;
//          }
//
//          // No accident detected
//          System.out.println("No accident: angle = " + angle);
//          return false;
//     }
//
//     // Method to calculate the slope of a line between (0,0) and a point (x, y)
//     public static double calculateSlope(double x, double y) {
//          // Avoid division by zero
//          if (x == 0) {
//               return Double.POSITIVE_INFINITY; // Infinite slope (vertical line)
//          }
//          return y / x; // m = (y2 - y1) / (x2 - x1), but in this case y1 = 0 and x1 = 0
//     }
//
//     // Method to calculate the angle between two lines with slopes m1 and m2
//     public static double calculateAngleBetweenLines(double slope1, double slope2) {
//          // Formula for the angle between two lines: arctan((m1 - m2) / (1 + m1 * m2))
//          double angleRadians = Math.atan((slope1 - slope2) / (1 + slope1 * slope2));
//          return Math.toDegrees(angleRadians); // Convert the result to degrees
//     }

//     private int accidentCount = 0;
//
//     private LinearLayout redAlertLayout;
//     private TextView countdownTimer;
//     private static AccidentDetector instance;
//
//
//
//
//     public  AccidentDetector(){
//
//     }
//
//     // Constructor que recibe las vistas
//     public AccidentDetector(LinearLayout redAlertLayout, TextView countdownTimer) {
//          this.redAlertLayout = redAlertLayout;
//          this.countdownTimer = countdownTimer;
//          //og.d("AccidentDetector", "PASO POR VIEWWWWWWWWWWWWWWWWWWWWWWW: " + (redAlertLayout != null));
//     }
//     public static synchronized AccidentDetector getInstance(LinearLayout redAlertLayout, TextView countdownTimer) {
//          if (instance == null) {
//               instance = new AccidentDetector(redAlertLayout, countdownTimer);
//          }
//          return instance;
//     }
//     // Método para obtener la instancia sin parámetros
//     public static synchronized AccidentDetector getInstance() {
//          return instance;
//     }



//     public  void  evaluateAccident (boolean isDetected){
//          if(isDetected){
//               accidentCount ++;
//               Log.d("AccidentDetector", "PASO POR VIEWWWWWWWWWWWWWWWWWWWWWWW2222222: " + (redAlertLayout != null));
//
//               if(shouldTriggerAccidentAlert()){
//                    System.out.println("ALERTAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: Se ha detectado un accidente.");
//                    if (this.redAlertLayout != null) {
//                         this.redAlertLayout.setVisibility(View.VISIBLE);
//                         startCountdownTimer();
//                    }else{
//                         Log.e("AccidentDetector", "redAlertLayout is null!");
//
//                    }
//               }
//          }
//     }

//     public  static void shouldTriggerAccidentAlert(){
//          Log.d("AccidentDetector", "PASO POR VIEWWWWWWWWWWWWWWWWWWWWWWW333333333333: " + (redAlertLayout != null));
//          this.redAlertLayout.setVisibility(View.VISIBLE);
//          startCountdownTimer();
//     }

     // Reiniciar el contador después de la detección
//     public void reset() {
//          accidentCount = 0;
//     }
//     private void startCountdownTimer() {
//          //Log.d("AccidentDetector", "PASO POR VIEWWWWWWWWWWWWWWWWWWWWWWW4444444444444: " + (redAlertLayout != null));
//
//          CountDownTimer timer = new CountDownTimer(30000, 1000) {
//               @Override
//               public void onTick(long millisUntilFinished) {
//                    int secondsLeft = (int) (millisUntilFinished / 1000);
//                    countdownTimer.setText(String.valueOf(secondsLeft));
//               }
//
//               @Override
//               public void onFinish() {
//                    countdownTimer.setText("0");
//                    // Aquí puedes manejar lo que ocurre al finalizar el timer
//               }
//          };
//          timer.start();
//     }
}
