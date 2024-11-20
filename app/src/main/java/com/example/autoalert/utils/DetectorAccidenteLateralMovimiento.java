package com.example.autoalert.utils;

import static android.content.ContentValues.TAG;
import static com.example.autoalert.utils.AceleracionBrusca.esAceleracionBrusca;
import static com.example.autoalert.utils.AutoParado.elAutoEstaParado;
import static com.example.autoalert.utils.CalcularAngulo.calcularAngulo;
import static com.example.autoalert.utils.CambioBrusco.esCambioBrusco;
import static com.example.autoalert.utils.DesaceleracionBrusca.esDesaceleracionBrusca;

import android.content.Context;
import android.util.Log;

import com.example.autoalert.model.entities.DatosMovimiento;


import java.util.LinkedList;

public class DetectorAccidenteLateralMovimiento {

    private LinkedList<DatosMovimiento> historialDatos = new LinkedList<>();
    private static final double UMBRAL_CAMBIO_ANGULO = 70;
    private static final double UMBRAL_ACELERACION = 10.0;  // Diferencia en km/h para considerar un aumento brusco// Grados
    private boolean cambioBruscoDetectado=false;
    private double angulo;
    private boolean cambioBrusco=false;
    private boolean desaceleracionBruscaDetectada=false;
    private boolean aceleracionBruscaDetectada=false;

    private int contadorCero = 0;
    private static final int CEROS_CONSECUTIVOS_NECESARIOS= 18;
    private double diferenciaVelocidad;
    private double umbralVariable;

    private Context context; // Agregar contexto para el archivo

    public DetectorAccidenteLateralMovimiento(Context context) {
        this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas


    }


    public boolean registrarNuevoDato(DatosMovimiento nuevoDato) {
        historialDatos.add(nuevoDato);


        if (!cambioBruscoDetectado && historialDatos.size() == 3) {
            if (analizarCondicionesPrevias()) {
                cambioBruscoDetectado = true;
                historialDatos.clear();
                Log.i(TAG, "Condiciones previas cumplidas. Recolectando 3 nuevos datos...");

                return false; // Aún no es accidente, pero se cumple la primera condición
            }

            historialDatos.removeFirst();
            return false;



        }

        if (cambioBruscoDetectado) {
            Log.i(TAG, "PASO POR CAMBIO BRUSCO DETECTEDDDD.");

            if (analizarMovimientoPosterior()) {
                Log.i(TAG, "Posible accidente lateral detectado.");

                cambioBruscoDetectado = false;
                contadorCero=0;
                historialDatos.clear();
                historialDatos.addFirst(nuevoDato);

                return true;  // Accidente detectado
            } else {

                Log.i(TAG, "No se detecta accidente.");
            }

            //cambioBruscoDetectado = false;

        }
        if(historialDatos.size() < 3&& !cambioBruscoDetectado) {
        }

        return false;  // Por defecto, no se detecta accidente
    }



    private boolean analizarCondicionesPrevias() {
        DatosMovimiento punto1 = historialDatos.get(0);
        DatosMovimiento punto2 = historialDatos.get(1);
        DatosMovimiento punto3 = historialDatos.get(2);



        angulo = calcularAngulo(punto1, punto2, punto3);
        Log.i(TAG,"ANGULAAAA : "+ angulo+ " punto1="+ punto1.getLatitud()+","+punto1.getLongitud()+" punto2="+ punto2.getLatitud()+","+punto2.getLongitud()+" punto3="+ punto3.getLatitud()+","+punto3.getLongitud());

        cambioBrusco = esCambioBrusco(angulo, UMBRAL_CAMBIO_ANGULO);
        Log.i(TAG,"CAMBIO BRUSCO : "+ cambioBrusco);


        desaceleracionBruscaDetectada = esDesaceleracionVariable();
        Log.i(TAG,"DESACELERACION BRUSCA : "+ desaceleracionBruscaDetectada);

        aceleracionBruscaDetectada= esAceleracionBrusca(historialDatos.get(1),historialDatos.get(2),UMBRAL_ACELERACION);





        if (cambioBrusco && (desaceleracionBruscaDetectada || aceleracionBruscaDetectada) ) {
            System.out.println("Cambio brusco y desaceleración brusca o auto parado detectados.");
            Log.i(TAG,"Cambio brusco y desaceleración brusca o auto parado detectados ");

            return true;
        }

        return false;
    }

    private boolean esDesaceleracionVariable() {
        DatosMovimiento punto1 = historialDatos.get(1);
        DatosMovimiento punto2 = historialDatos.get(2);

        umbralVariable = punto1.getVelocidad() * 0.8;  // 80% de la velocidad del primer dato
        diferenciaVelocidad = punto1.getVelocidad() - punto2.getVelocidad();

        return diferenciaVelocidad > umbralVariable;  // Si la diferencia es mayor al umbral, hay desaceleración brusca
    }

    private boolean analizarMovimientoPosterior() {

        if(historialDatos.size()>10 && contadorCero==0){
            historialDatos.clear();
            cambioBruscoDetectado=false;
            return false;
        }

        if(historialDatos.getLast().getVelocidad()==0 ){
            contadorCero++;
            Log.i(TAG,"CERO EN LAT: "+ contadorCero);
            if (contadorCero >= CEROS_CONSECUTIVOS_NECESARIOS) {
                Log.i(TAG, "Accidente LATERAL confirmado tras 23 datos consecutivos en velocidad cero.");
                contadorCero = 0; // Reiniciar el contador para próximas detecciones
                return true;
            }

        }
        else{
            contadorCero=0;
        }


        return false;


    }




}