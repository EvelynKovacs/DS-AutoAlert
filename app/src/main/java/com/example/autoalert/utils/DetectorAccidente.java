package com.example.autoalert.utils;


import android.content.Context;
import com.example.autoalert.model.entities.DatosMovimiento;

    public class DetectorAccidente {

        private DetectorAccidenteLateral detectorLateral;
        private DetectorAccidenteFrontal detectorFrontal;
        private DetectorAccidenteTrasero detectorTrasero;
        private DetectorAccidenteVuelco detectorVuelco;

        public DetectorAccidente(Context context) {
            detectorLateral = new DetectorAccidenteLateral(context);
            detectorFrontal = new DetectorAccidenteFrontal(context);
            detectorTrasero = new DetectorAccidenteTrasero(context);
            detectorVuelco = new DetectorAccidenteVuelco(context);

        }

        public void registrarNuevoDato(DatosMovimiento nuevoDato) {
            // Verificar todos los tipos de accidentes y notificar si ocurre uno
            if (!detectorLateral.registrarNuevoDato(nuevoDato) ) {
                NotificadorAccidente.getInstancia().notificarAccidente("Lateral");
            }
            if (detectorFrontal.registrarNuevoDato(nuevoDato)) {
                NotificadorAccidente.getInstancia().notificarAccidente("Frontal");
            }
            if (detectorTrasero.registrarNuevoDato(nuevoDato)) {
                NotificadorAccidente.getInstancia().notificarAccidente("Trasero");
            }
            if (detectorVuelco.registrarNuevoDato(nuevoDato)) {
                NotificadorAccidente.getInstancia().notificarAccidente("Vuelco");
            }

    }

}
