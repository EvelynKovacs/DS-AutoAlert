package com.example.autoalert.utils;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.autoalert.R;

public class AccidentDetector {
     private int accidentCount = 0;

     private LinearLayout redAlertLayout;
     private TextView countdownTimer;
     private static AccidentDetector instance;




     public  AccidentDetector(){

     }

     // Constructor que recibe las vistas
     public AccidentDetector(LinearLayout redAlertLayout, TextView countdownTimer) {
          this.redAlertLayout = redAlertLayout;
          this.countdownTimer = countdownTimer;
          //og.d("AccidentDetector", "PASO POR VIEWWWWWWWWWWWWWWWWWWWWWWW: " + (redAlertLayout != null));
     }
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
     public void reset() {
          accidentCount = 0;
     }
     private void startCountdownTimer() {
          //Log.d("AccidentDetector", "PASO POR VIEWWWWWWWWWWWWWWWWWWWWWWW4444444444444: " + (redAlertLayout != null));

          CountDownTimer timer = new CountDownTimer(30000, 1000) {
               @Override
               public void onTick(long millisUntilFinished) {
                    int secondsLeft = (int) (millisUntilFinished / 1000);
                    countdownTimer.setText(String.valueOf(secondsLeft));
               }

               @Override
               public void onFinish() {
                    countdownTimer.setText("0");
                    // Aquí puedes manejar lo que ocurre al finalizar el timer
               }
          };
          timer.start();
     }
}
