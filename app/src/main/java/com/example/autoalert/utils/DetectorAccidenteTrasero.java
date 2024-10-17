//package com.example.autoalert.utils;
//
//import static com.example.autoalert.utils.AceleracionBrusca.esAceleracionBrusca;
//import static com.example.autoalert.utils.AutoParado.elAutoEstaParado;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.example.autoalert.model.entities.DatosMovimiento;
//import com.example.autoalert.repository.DetectorAccidenteDataWriter;
//
//import java.util.LinkedList;
//
//public class DetectorAccidenteTrasero {
//
//    private static final double UMBRAL_ACELERACION = 20.0;  // metros/seg^2 (ajustar según necesidad)
//    private LinkedList<DatosMovimiento> historialDatos = new LinkedList<>();
//    private boolean aceleracionBruscaDetectada = false;
//    private Context context;
//
//    public DetectorAccidenteTrasero(Context context) {
//        this.context = context.getApplicationContext();
//    }
//
//    public boolean registrarNuevoDato(DatosMovimiento nuevoDato) {
//        historialDatos.add(nuevoDato);
//        if (historialDatos.size() > 3) {
//            historialDatos.removeFirst();  // Mantén solo los últimos 3 datos
//        }
//
//        if (!aceleracionBruscaDetectada && historialDatos.size() == 3) {
//            if (analizarCondicionesPrevias()) {
//                aceleracionBruscaDetectada = true;
//                historialDatos.clear();
//                Log.i("ACCIDENTE_TRASERO", "Condiciones previas de accidente trasero cumplidas.");
//                return true;  // Se detectó un posible accidente
//            }
//            return false;  // No se cumplen las condiciones previas
//        }
//
//        if (aceleracionBruscaDetectada && historialDatos.size() == 3) {
//            if (analizarMovimientoPosterior()) {
//                Log.i("ACCIDENTE_TRASERO", "Posible accidente trasero detectado.");
//                DetectorAccidenteDataWriter.writeAccidentDataToFile(context, "ACCIDENTE TRASERO DETECTADO.");
//                aceleracionBruscaDetectada = false;
//                return true;  // Se detectó un accidente
//            } else {
//                Log.i("ACCIDENTE_TRASERO", "No se detecta accidente trasero.");
//                aceleracionBruscaDetectada = false;
//                return false;  // No se detectó un accidente
//            }
//        }
//
//        return false;  // Por defecto, si no se cumplen las condiciones de accidente
//    }
//
//    private boolean analizarCondicionesPrevias() {
//        DatosMovimiento punto1 = historialDatos.get(0);
//        DatosMovimiento punto2 = historialDatos.get(1);
//        DatosMovimiento punto3 = historialDatos.get(2);
//
//        boolean aceleracionBrusca = esAceleracionBrusca(punto1, punto2, UMBRAL_ACELERACION);
//
//        if (aceleracionBrusca) {
//            Log.i("ACCIDENTE_TRASERO", "Aceleración brusca detectada.");
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
//        return esAceleracionBrusca(punto4, punto5, UMBRAL_ACELERACION) || elAutoEstaParado(punto4, punto5, punto6, 1);
//    }
//
//
//}

package com.example.autoalert.utils;

import static com.example.autoalert.utils.AceleracionBrusca.esAceleracionBrusca;

import android.content.Context;
import android.util.Log;

import com.example.autoalert.model.entities.DatosMovimiento;
import com.example.autoalert.repository.DetectorAccidenteDataWriter;

import java.util.LinkedList;

public class DetectorAccidenteTrasero {

    private static final double UMBRAL_ACELERACION = 10.0;  // Diferencia en km/h para considerar un aumento brusco
    private LinkedList<DatosMovimiento> historialDatos = new LinkedList<>();
    private boolean aceleracionBruscaDetectada = false;
    private Context context;

     private boolean aceleracionBruscaConfirmada;

    public DetectorAccidenteTrasero(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean registrarNuevoDato(DatosMovimiento nuevoDato) {
        historialDatos.add(nuevoDato);
        if (historialDatos.size() > 10) {  // Mantén un historial de los últimos 10 segundos
            historialDatos.removeFirst();
        }

        // Detectar aceleración brusca
        if (!aceleracionBruscaDetectada && historialDatos.size() == 2) {
            if (analizarCondicionesPrevias()) {
                aceleracionBruscaDetectada = true;
                historialDatos.clear();

                Log.i("ACCIDENTE_TRASERO", "Aceleración brusca detectada, evaluando comportamiento posterior.");
                return false;  // Aceleración detectada, pero aún no se confirma el accidente
            }
            historialDatos.removeFirst();

            return false;  // No se cumplen las condiciones// previas
        }

        // Evaluar si, tras la aceleración brusca, las últimas 3 velocidades son 0
        if (aceleracionBruscaDetectada && historialDatos.size() == 10) {
            if (evaluarAutoDetenidoUltimos3Segundos()) {

                Log.i("ACCIDENTE_TRASERO", "Accidente trasero detectado: las últimas 3 velocidades fueron 0.");
                DetectorAccidenteDataWriter.writeAccidentDataToFile(context, "ACCIDENTE TRASERO DETECTADO.");
                aceleracionBruscaDetectada = false;
                historialDatos.clear();  // Limpiar historial después de detectar el accidente
                return true;  // Se detectó un accidente
            } else {

                Log.i("ACCIDENTE_TRASERO", "No se detecta accidente trasero.");
                aceleracionBruscaDetectada = false;

                historialDatos.clear();
                historialDatos.add(nuevoDato);  // Guardar el último dato y continuar la evaluación
                return false;  // No se detectó un accidente
            }


        }

        return false;  // Por defecto, si no se cumplen las condiciones de accidente
    }

    private boolean analizarCondicionesPrevias() {
        DatosMovimiento punto1 = historialDatos.get(historialDatos.size() - 2);
        DatosMovimiento punto2 = historialDatos.get(historialDatos.size() - 1);

        aceleracionBruscaConfirmada =esAceleracionBrusca(punto1, punto2, UMBRAL_ACELERACION);

        // Detectar aumento brusco de velocidad
        return aceleracionBruscaConfirmada;
    }

    private boolean evaluarAutoDetenidoUltimos3Segundos() {
        // Verificar si las últimas 3 velocidades fueron 0
        int size = historialDatos.size();
        return historialDatos.get(size - 1).getVelocidad() == 0 &&
                historialDatos.get(size - 2).getVelocidad() == 0 &&
                historialDatos.get(size - 3).getVelocidad() == 0;
    }


}

