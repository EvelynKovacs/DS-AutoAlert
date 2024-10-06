package com.example.autoalert.utils;

import static com.example.autoalert.utils.CalcularAngulo.calcularAngulo;
import static com.example.autoalert.utils.CambioBrusco.esCambioBrusco;
import static com.example.autoalert.utils.DesaceleracionBrusca.esDesaceleracionBrusca;

import android.content.Context;
import android.util.Log;
import com.example.autoalert.model.entities.DatosMovimiento;
import com.example.autoalert.repository.DetectorAccidenteDataWriter;

import java.util.LinkedList;

public class DetectorAccidenteVuelco {

    private static final double UMBRAL_CAMBIO_ORIENTACION = 60.0; // Grados (puede ajustarse)
    private static final double UMBRAL_DESACELERACION = 5.0;  // metros/seg^2
    private static final double UMBRAL_TIEMPO_ORIENTACION = 3; // Segundos de cambio mantenido
    private LinkedList<DatosMovimiento> historialDatos = new LinkedList<>();
    private boolean cambioBruscoDetectado = false;
    private Context context;

    public DetectorAccidenteVuelco(Context context) {
        this.context = context.getApplicationContext();
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
                Log.i("ACCIDENTE_VUELCO", "Condiciones previas de accidente por vuelco cumplidas.");
                return false;  // Aún no se ha confirmado el accidente, pero las condiciones previas se cumplieron
            }
        }

        if (cambioBruscoDetectado && historialDatos.size() == 3) {
            if (analizarMovimientoPosterior()) {
                Log.i("ACCIDENTE_VUELCO", "Posible accidente de vuelco detectado.");
                DetectorAccidenteDataWriter.writeAccidentDataToFile(context, "ACCIDENTE POR VUELCO DETECTADO.");
                cambioBruscoDetectado = false;
                return true;  // Se detectó un accidente de vuelco
            } else {
                Log.i("ACCIDENTE_VUELCO", "No se detecta accidente por vuelco.");
                cambioBruscoDetectado = false;
            }
        }

        return false;  // No se ha detectado un accidente de vuelco
    }

    private boolean analizarCondicionesPrevias() {
        DatosMovimiento punto1 = historialDatos.get(0);
        DatosMovimiento punto2 = historialDatos.get(1);
        DatosMovimiento punto3 = historialDatos.get(2);

        // Calcula el cambio en la orientación
        double cambioOrientacion = calcularAngulo(punto1, punto2, punto3);
        boolean cambioBrusco = esCambioBrusco(cambioOrientacion, UMBRAL_CAMBIO_ORIENTACION);
        Log.i("ACCIDENTE_VUELCO", "Cambio brusco de orientación: " + cambioBrusco);

        boolean desaceleracionBrusca = esDesaceleracionBrusca(punto2, punto3, UMBRAL_DESACELERACION);
        Log.i("ACCIDENTE_VUELCO", "Desaceleración brusca: " + desaceleracionBrusca);

        if (cambioBrusco && desaceleracionBrusca) {
            Log.i("ACCIDENTE_VUELCO", "Condiciones previas de vuelco detectadas.");
            return true;  // Las condiciones de un vuelco potencial están presentes
        }

        return false;
    }

    private boolean analizarMovimientoPosterior() {
        DatosMovimiento punto4 = historialDatos.get(0);
        DatosMovimiento punto5 = historialDatos.get(1);
        DatosMovimiento punto6 = historialDatos.get(2);

        // Analiza si el cambio de orientación se mantuvo por el tiempo requerido
        boolean cambioSostenido = verificarCambioSostenido(historialDatos);
        Log.i("ACCIDENTE_VUELCO", "Cambio de orientación sostenido: " + cambioSostenido);

        boolean desaceleracionBrusca = esDesaceleracionBrusca(punto4, punto5, UMBRAL_DESACELERACION);

        return cambioSostenido && desaceleracionBrusca;
    }

    private boolean verificarCambioSostenido(LinkedList<DatosMovimiento> historial) {
        // Verifica si el cambio de orientación se mantuvo durante los 3 puntos (tiempo simulado en segundos)
        if (historial.size() < 3) return false;

        DatosMovimiento punto1 = historial.get(0);
        DatosMovimiento punto2 = historial.get(1);
        DatosMovimiento punto3 = historial.get(2);

        double cambioOrientacion1 = calcularAngulo(punto1, punto2, punto3);
        return esCambioBrusco(cambioOrientacion1, UMBRAL_CAMBIO_ORIENTACION);
    }
}
