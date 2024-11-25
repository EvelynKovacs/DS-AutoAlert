//package com.example.autoalert.utils;
//
//import static com.example.autoalert.utils.AutoParado.elAutoEstaParado;
//import static com.example.autoalert.utils.DesaceleracionBrusca.esDesaceleracionBrusca;
//
//import android.content.Context;
//import android.util.Log;
//import com.example.autoalert.model.entities.DatosMovimiento;
//import com.example.autoalert.repository.DetectorAccidenteDataWriter;
//import java.util.LinkedList;
//
//public class DetectorAccidenteFrontal {
//
//    private static final double UMBRAL_DESACELERACION = 5.0;  // metros/seg^2 (ajustar según necesidad)
//    private LinkedList<DatosMovimiento> historialDatos = new LinkedList<>();
//    private boolean cambioBruscoDetectado = false;
//    private Context context;
//
//    public DetectorAccidenteFrontal(Context context) {
//        this.context = context.getApplicationContext();
//    }
//
//    public boolean registrarNuevoDato(DatosMovimiento nuevoDato) {
//        historialDatos.add(nuevoDato);
//        if (historialDatos.size() > 3) {
//            historialDatos.removeFirst();  // Mantén solo los últimos 3 datos
//        }
//
//        if (!cambioBruscoDetectado && historialDatos.size() == 3) {
//            if (analizarCondicionesPrevias()) {
//                cambioBruscoDetectado = true;
//                historialDatos.clear();
//                Log.i("ACCIDENTE_FRONTAL", "Condiciones previas de accidente frontal cumplidas.");
//                return true;  // Se detectó un posible accidente en condiciones previas
//            }
//            return false;  // No se cumplen las condiciones previas
//        }
//
//        if (cambioBruscoDetectado && historialDatos.size() == 3) {
//            if (analizarMovimientoPosterior() ) {
//                Log.i("ACCIDENTE_FRONTAL", "Posible accidente frontal detectado.");
//                DetectorAccidenteDataWriter.writeAccidentDataToFile(context, "ACCIDENTE FRONTAL DETECTADO.");
//                cambioBruscoDetectado = false;
//
//                return true;  // Se detectó un accidente
//            } else {
//                Log.i("ACCIDENTE_FRONTAL", "No se detecta accidente frontal.");
//                cambioBruscoDetectado = false;
//                return false;  // No se detectó un accidente
//            }
//        }
//
//        return false;  // Por defecto, si no se cumplen las condiciones de accidente
//    }
//
//
//    private boolean analizarCondicionesPrevias() {
//        DatosMovimiento punto1 = historialDatos.get(0);
//        DatosMovimiento punto2 = historialDatos.get(1);
//        DatosMovimiento punto3 = historialDatos.get(2);
//
//        boolean desaceleracionBrusca = esDesaceleracionBrusca(punto1, punto2, UMBRAL_DESACELERACION);
//
//        if (desaceleracionBrusca) {
//            Log.i("ACCIDENTE_FRONTAL", "Desaceleración brusca detectada.");
//            return true;
//        }
//
//        return false;
//    }
//
//    private boolean analizarMovimientoPosterior() {
//        DatosMovimiento punto4 = historialDatos.get(0);
//        DatosMovimiento punto5 = historialDatos.get(1);
//        DatosMovimiento punto6 = historialDatos.get(2);
//
//        return esDesaceleracionBrusca(punto4, punto5, UMBRAL_DESACELERACION) || elAutoEstaParado(punto4, punto5, punto6, 1);
//    }
//
//
//}
//
package com.example.autoalert.utils;

import static android.content.ContentValues.TAG;
import static com.example.autoalert.utils.AutoParado.elAutoEstaParado;

import android.content.Context;
import android.util.Log;
import com.example.autoalert.model.entities.DatosMovimiento;
import java.util.LinkedList;

public class DetectorAccidenteFrontal {

    private static final double VELOCIDAD_MINIMA = 25.0;  // Velocidad mínima para considerar desaceleración brusca
    private LinkedList<DatosMovimiento> historialDatos = new LinkedList<>();
    private Context context;
    private boolean desaceleracionBruscaConfirmada = false;
    private long tiempoInicioEvaluacion = 0;
    private static final int CEROS_CONSECUTIVOS_NECESARIOS= 23;



    private int contadorCero=0;

    private double diferenciaVelocidad;
    private double umbralVariable;

    public DetectorAccidenteFrontal(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean registrarNuevoDato(DatosMovimiento nuevoDato) {
        historialDatos.add(nuevoDato);
        if (historialDatos.size() > 12) {
            historialDatos.removeFirst();  // Mantén solo los últimos 12 datos como máximo
        }

        // Evaluar desaceleración brusca si hay al menos dos datos en la lista
        if (!desaceleracionBruscaConfirmada && historialDatos.size() >= 2) {
            if (evaluarDesaceleracionVariable()) {
                desaceleracionBruscaConfirmada = true;
                //tiempoInicioEvaluacion = nuevoDato.getTiempo();
                Log.i("ACCIDENTE_FRONTAL", "Desaceleración brusca confirmada. Iniciando evaluación de patrón.");

            } else {
                historialDatos.removeFirst();  // No hay desaceleración, eliminar el primer dato y continuar

                return false;
            }

        }

        // Si ya se confirmó la desaceleración brusca, evaluar el patrón de los datos posteriores
        if (desaceleracionBruscaConfirmada) {
            if (evaluarMovimientoPosterior()) {

                Log.i("ACCIDENTE_FRONTAL", "Accidente frontal detectado.");
                desaceleracionBruscaConfirmada = false;
                contadorCero=0;
                historialDatos.clear();  // Limpiar el historial tras detectar el accidente

                return true;  // Accidente detectado
            } /*else if (romperPatron()) {

                // Si se rompe el patrón (por ejemplo, el vehículo acelera), reiniciar la evaluación
                desaceleracionBruscaConfirmada = false;
                historialDatos.clear();
                historialDatos.add(nuevoDato);  // Guardar el último dato y continuar la evaluación
            }*/
        }

        return false;

    }

    // Evaluar si hay una desaceleración brusca entre los primeros dos datos
    private boolean evaluarDesaceleracionVariable() {
        DatosMovimiento punto1 = historialDatos.get(0);
        DatosMovimiento punto2 = historialDatos.get(1);

        // Ignorar evaluaciones si la velocidad del primer dato es menor a 12 km/h
        if (punto1.getVelocidad() < VELOCIDAD_MINIMA) {
            Log.i("ACCIDENTE_FRONTAL", "Velocidad inicial menor a 20 km/h: no se evalúa la desaceleración.");
            umbralVariable = 0;
            diferenciaVelocidad =0;
            return false;  // No evaluar, simplemente seguir acumulando datos
        }

         umbralVariable = punto1.getVelocidad() * 0.8;  // 80% de la velocidad del primer dato
         diferenciaVelocidad = punto1.getVelocidad() - punto2.getVelocidad();
        Log.i("ACCIDENTE_FRONTAL", "Diferencia de velocidad: " + diferenciaVelocidad + ", Umbral variable: " + umbralVariable);

        return diferenciaVelocidad > umbralVariable;  // Si la diferencia es mayor al umbral, hay desaceleración brusca
    }

    // Evaluar el patrón de desaceleración después de haber confirmado la desaceleración brusca
    private boolean evaluarMovimientoPosterior() {
        if (historialDatos.size() < 3) {
            return false;  // No hay suficientes datos para evaluar
        }
        if(historialDatos.size()==12& contadorCero==0){
            historialDatos.clear();

            desaceleracionBruscaConfirmada=false;
           // historialDatos.subList(0, historialDatos.size() - 1).clear();
            return false;
        }

        if(historialDatos.getLast().getVelocidad()<=3 ){
            contadorCero++;
            Log.i(TAG,"CERO: "+ contadorCero);
            if (contadorCero >= CEROS_CONSECUTIVOS_NECESARIOS) {
                Log.i(TAG, "Accidente FRONTAL confirmado tras 23 datos consecutivos en velocidad cero.");
                contadorCero = 0; // Reiniciar el contador para próximas detecciones
                return true;
            }

        }
        else{
            contadorCero=0;
        }

        // Si el vehículo llega a velocidad 0 y se mantiene por al menos 3 datos consecutivos, se confirma el accidente

        return false;  // No se ha detectado accidente todavía
    }

    // Evaluar si el auto se mantiene en velocidad 0 durante al menos 3 datos consecutivos
//    private boolean evaluarPatronDeVelocidadCero() {
//        int contadorCero = 0;
//        for (DatosMovimiento dato : historialDatos) {
//            if (dato.getVelocidad() == 0) {
//                contadorCero++;
//            } else {
//                contadorCero = 0;  // Reiniciar si no está en 0
//            }
//
//            if (contadorCero >= 3) {
//                Log.i("ACCIDENTE_FRONTAL", "El auto ha estado parado durante al menos 3 datos consecutivos.");
//                return true;  // Accidente confirmado
//            }
//        }
//        return false;
//    }

    // Romper el patrón de desaceleración si hay un aumento de velocidad
    /*private boolean romperPatron() {
        for (int i = 1; i < historialDatos.size(); i++) {
            DatosMovimiento anterior = historialDatos.get(i - 1);
            DatosMovimiento actual = historialDatos.get(i);

            if (actual.getVelocidad() > anterior.getVelocidad()) {
                Log.i("ACCIDENTE_FRONTAL", "El vehículo ha acelerado. Patrón roto.");
                return true;  // Si se detecta aceleración, romper el patrón
            }
        }
        return false;
    }*/
}

