package com.example.autoalert.utils;

import static android.content.ContentValues.TAG;
import static com.example.autoalert.utils.AutoParado.elAutoEstaParado;
import static com.example.autoalert.utils.CalcularAngulo.calcularAngulo;
import static com.example.autoalert.utils.CambioBrusco.esCambioBrusco;
import static com.example.autoalert.utils.DesaceleracionBrusca.esDesaceleracionBrusca;

import android.content.Context;
import android.util.Log;

import com.example.autoalert.model.entities.DatosMovimiento;
import com.example.autoalert.repository.DetectorAccidenteDataWriter;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;

public class DetectorAccidenteLateral {


    private static final double UMBRAL_AUTO_PARADO = 5;
    private LinkedList<DatosMovimiento> historialDatos = new LinkedList<>();
    private static final double UMBRAL_CAMBIO_ANGULO = 70;  // Grados
    private static final double UMBRAL_DESACELERACION = 5.0;  // metros/seg^2
    private boolean cambioBruscoDetectado=false;


    private Context context; // Agregar contexto para el archivo

    public DetectorAccidenteLateral(Context context) {
        this.context = context.getApplicationContext(); // Usar el contexto de aplicación para evitar fugas


    }


    public boolean registrarNuevoDato(DatosMovimiento nuevoDato) {
        historialDatos.add(nuevoDato);
        if (historialDatos.size() > 3) {
            historialDatos.removeFirst();  // Mantén solo los últimos 3 datos
        }

        if (!cambioBruscoDetectado && historialDatos.size() == 3) {
            if (analizarCondicionesPrevias()) {
                cambioBruscoDetectado = true;
                historialDatos.clear();
                Log.i(TAG, "Condiciones previas cumplidas. Recolectando 3 nuevos datos...");
                return false; // Aún no es accidente, pero se cumple la primera condición
            }
        }

        if (cambioBruscoDetectado && historialDatos.size() == 3) {
            if (analizarMovimientoPosterior()) {
                Log.i(TAG, "Posible accidente lateral detectado.");
                DetectorAccidenteDataWriter.writeAccidentDataToFile(context, "ACCIDENTEEEEEEEEEEEEEEEEEEEEE LATERALLLLLLLLLLLLLLLLLLLL detectado.");
                cambioBruscoDetectado = false;
                return true;  // Accidente detectado
            } else {
                Log.i(TAG, "No se detecta accidente.");
            }
            cambioBruscoDetectado = false;
        }

        return false;  // Por defecto, no se detecta accidente
    }



    private boolean analizarCondicionesPrevias() {
        DatosMovimiento punto1 = historialDatos.get(0);
        DatosMovimiento punto2 = historialDatos.get(1);
        DatosMovimiento punto3 = historialDatos.get(2);

        DetectorAccidenteDataWriter.writeAccidentDataToFile(context,"Punto 1: ("+punto1.getLatitud()+","+punto1.getLongitud()+")");
        DetectorAccidenteDataWriter.writeAccidentDataToFile(context,"Punto 2: ("+punto2.getLatitud()+","+punto2.getLongitud()+")");
        DetectorAccidenteDataWriter.writeAccidentDataToFile(context,"Punto 3: ("+punto3.getLatitud()+","+punto3.getLongitud()+")");


        double angulo = calcularAngulo(punto1, punto2, punto3);
        Log.i(TAG,"ANGULO : "+ angulo+ " punto1="+ punto1.getLatitud()+","+punto1.getLongitud()+" punto2="+ punto2.getLatitud()+","+punto2.getLongitud()+" punto3="+ punto3.getLatitud()+","+punto3.getLongitud());

        DetectorAccidenteDataWriter.writeAccidentDataToFile(context,"ANGULO: "+ angulo);
        boolean cambioBrusco = esCambioBrusco(angulo, UMBRAL_CAMBIO_ANGULO);
        Log.i(TAG,"CAMBIO BRUSCO : "+ cambioBrusco);

        DetectorAccidenteDataWriter.writeAccidentDataToFile(context,"CAMBIO BRUSCO: "+ cambioBrusco);

        boolean desaceleracionBrusca = esDesaceleracionBrusca(punto2, punto3, UMBRAL_DESACELERACION);
        Log.i(TAG,"DESACELERACION BRUSCA : "+ desaceleracionBrusca);

        DetectorAccidenteDataWriter.writeAccidentDataToFile(context,"DESACELERACION BRUSCA: "+ desaceleracionBrusca);

        boolean autoParado = elAutoEstaParado(punto1, punto2, punto3, UMBRAL_AUTO_PARADO);
        Log.i(TAG,"AUTO PARADO : "+ autoParado);

        DetectorAccidenteDataWriter.writeAccidentDataToFile(context,"PARADO: "+ desaceleracionBrusca);



        if (cambioBrusco && (desaceleracionBrusca || autoParado)) {
            System.out.println("Cambio brusco y desaceleración brusca o auto parado detectados.");
            Log.i(TAG,"Cambio brusco y desaceleración brusca o auto parado detectados ");

            DetectorAccidenteDataWriter.writeAccidentDataToFile(context, "Se cumplen todas las condiciones previas.");
            return true;
        }

        return false;
    }



    private boolean analizarMovimientoPosterior() {
        DatosMovimiento punto4 = historialDatos.get(0);
        DatosMovimiento punto5 = historialDatos.get(1);
        DatosMovimiento punto6 = historialDatos.get(2);

        boolean resultado = evaluarMovimientoPosterior(punto4, punto5, punto6, UMBRAL_DESACELERACION);

        if (resultado) {
            return true; // Se detectó accidente en los datos posteriores
        }

        return false; // No se detectó accidente, seguimos recolectando datos
    }

    private boolean evaluarMovimientoPosterior(DatosMovimiento punto4, DatosMovimiento punto5, DatosMovimiento punto6, double umbralDesaceleracion) {
//        if (punto6.getVelocidad() > punto5.getVelocidad() && punto5.getVelocidad() > punto4.getVelocidad()) {
//            return "El auto siguió en movimiento.";
//        }

//        if (punto4.getVelocidad() > punto5.getVelocidad() && punto5.getVelocidad() > punto6.getVelocidad()) {
//            double desaceleracionProgresiva = calcularDesaceleracion(punto4, punto5, punto6);
//            if (desaceleracionProgresiva < umbralDesaceleracion) {
//                return "El auto desaceleró de manera progresiva.";
//            }
//        }

        if (esDesaceleracionBrusca(punto4, punto5, umbralDesaceleracion) || elAutoEstaParado(punto4, punto5, punto6, UMBRAL_AUTO_PARADO)) {
            return true;
        }

        return false;
    }



}

