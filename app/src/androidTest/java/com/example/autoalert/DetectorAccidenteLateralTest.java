package com.example.autoalert;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;

import com.example.autoalert.model.entities.DatosMovimiento;
import com.example.autoalert.utils.DetectorAccidenteLateral;
//import com.example.autoalert.utils.DetectorAccidenteLateral;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

//@Config(sdk = {28}) // Usa la versión de SDK que necesites
//@RunWith(RobolectricTestRunner.class)

public class DetectorAccidenteLateralTest extends Application {






    private DetectorAccidenteLateral detectorAccidente;
    private  ByteArrayOutputStream outputStreamCaptor;
    private  PrintStream originalOut ;

    @Before
    public void setup() {
//        Context context = ApplicationProvider.getApplicationContext();
//        detectorAccidente = new DetectorAccidenteLateral(context);

       Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(context); // Mockear el contexto
//
        detectorAccidente = new DetectorAccidenteLateral(context);

        //detectorAccidente = new DetectorAccidente(getApplicationContext());
        originalOut = System.out;
        //System.setOut(new PrintStream(outputStreamCaptor));
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

    }

    @Test
    public void testDesaceleracionNoAccidente() {
        long tiempoActual = System.currentTimeMillis();



        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 20, tiempoActual);  // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 15, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 5, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s

         DatosMovimiento postDato1 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento postDato2 = new DatosMovimiento(-42.78905,-65.01881, 12, tiempoActual + 4000); // Continúa movimiento
        DatosMovimiento postDato3 = new DatosMovimiento(-42.789,-65.01883, 15, tiempoActual + 5000); // Retorna a la velocidad original


        // Registra los datos
        detectorAccidente.registrarNuevoDato(dato1);
        detectorAccidente.registrarNuevoDato(dato2);
        detectorAccidente.registrarNuevoDato(dato3);
        detectorAccidente.registrarNuevoDato(postDato1);
        detectorAccidente.registrarNuevoDato(postDato2);
        detectorAccidente.registrarNuevoDato(postDato3);



        String output = outputStreamCaptor.toString().trim();
        System.out.println(output);  // Ver qué se imprimió realmente
        assertTrue(output.contains("No se detecta accidente."));





       // assertTrue(outputStreamCaptor.toString().trim().contains("No se detecta accidente."));
    }


    @Test
    public void testParadoNoAccidente() {
        long tiempoActual = System.currentTimeMillis();


        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 0, tiempoActual);  // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 0, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 0, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s

        DatosMovimiento postDato1 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento postDato2 = new DatosMovimiento(-42.78905,-65.01881, 12, tiempoActual + 4000); // Continúa movimiento
        DatosMovimiento postDato3 = new DatosMovimiento(-42.789,-65.01883, 15, tiempoActual + 5000); // Retorna a la velocidad original


        // Registra los datos
        detectorAccidente.registrarNuevoDato(dato1);
        detectorAccidente.registrarNuevoDato(dato2);
        detectorAccidente.registrarNuevoDato(dato3);
        detectorAccidente.registrarNuevoDato(postDato1);
        detectorAccidente.registrarNuevoDato(postDato2);
        detectorAccidente.registrarNuevoDato(postDato3);



        String output = outputStreamCaptor.toString().trim();
        System.out.println(output);  // Ver qué se imprimió realmente
        assertTrue(output.contains("No se detecta accidente."));

    }


    @Test
    public void testDesaceleracionAccidenteDetectado() {
        // Crea datos de movimiento que deberían desencadenar un accidente
        long tiempoActual = System.currentTimeMillis(); // Simula el tiempo actual


        //para anulo de 90 grados:
        //A:(−42.789256,−65.019722)
        //B:(−42.789256,−65.018722)
        //C:(−42.788256,−65.018722)




        // Crea datos que simulen un accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 20, tiempoActual);  // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 15, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 5, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s

        // Movimientos posteriores que confirman el accidente
        DatosMovimiento postDato1 = new DatosMovimiento(-42.789100, -65.018700, 0, tiempoActual + 3000); // Detención completa
        DatosMovimiento postDato2 = new DatosMovimiento(-42.789050, -65.018650, 0, tiempoActual + 4000); // Sin movimiento
        DatosMovimiento postDato3 = new DatosMovimiento(-42.789000, -65.018600, 0, tiempoActual + 5000); // Sin movimiento

        // Registra los datos
        detectorAccidente.registrarNuevoDato(dato1);
        detectorAccidente.registrarNuevoDato(dato2);
        detectorAccidente.registrarNuevoDato(dato3);
        detectorAccidente.registrarNuevoDato(postDato1);
        detectorAccidente.registrarNuevoDato(postDato2);
        detectorAccidente.registrarNuevoDato(postDato3);



        // Captura la salida para verificar si "Posible accidente lateral detectado." se imprime.
        assertTrue(outputStreamCaptor.toString().trim().contains("Posible accidente lateral detectado."));
    }

    @Test
    public void testMasDatosAccidenteDetectado() {
        long tiempoActual = System.currentTimeMillis();

        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 5, tiempoActual);
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 0, tiempoActual + 1000);
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 5, tiempoActual + 2000);

        // Movimientos posteriores que confirman el accidente
        DatosMovimiento postDato1 = new DatosMovimiento(-42.789100, -65.018700, 10, tiempoActual + 3000);
        DatosMovimiento postDato2 = new DatosMovimiento(-42.789050, -65.018650, 15, tiempoActual + 4000);
        DatosMovimiento postDato3 = new DatosMovimiento(-42.789000, -65.018600, 20, tiempoActual + 5000);
//        DatosMovimiento postDato4 = new DatosMovimiento(-42.78895,-65.01876, 5, tiempoActual + 5000);
//        DatosMovimiento postDato5 = new DatosMovimiento(-42.78887,-65.0188, 0, tiempoActual + 5000);
//        DatosMovimiento postDato6 = new DatosMovimiento(-42.7888,-65.01884, 0, tiempoActual + 5000);
//        DatosMovimiento postDato7 = new DatosMovimiento(-42.78874,-65.01888, 0, tiempoActual + 5000);
        DatosMovimiento postDato4 = new DatosMovimiento(-42.78893,-65.01864, 5, tiempoActual + 5000);
        DatosMovimiento postDato5 = new DatosMovimiento(-42.78887,-65.01868, 0, tiempoActual + 5000);
        DatosMovimiento postDato6 = new DatosMovimiento(-42.7888,-65.0186, 0, tiempoActual + 5000);
        DatosMovimiento postDato7 = new DatosMovimiento(-42.78874,-65.01852, 0, tiempoActual + 5000);


        // Registra los datos
        detectorAccidente.registrarNuevoDato(dato1);
        detectorAccidente.registrarNuevoDato(dato2);
        detectorAccidente.registrarNuevoDato(dato3);
        detectorAccidente.registrarNuevoDato(postDato1);
        detectorAccidente.registrarNuevoDato(postDato2);
        detectorAccidente.registrarNuevoDato(postDato3);
        detectorAccidente.registrarNuevoDato(postDato4);
        detectorAccidente.registrarNuevoDato(postDato5);
        detectorAccidente.registrarNuevoDato(postDato6);
        detectorAccidente.registrarNuevoDato(postDato7);



        // Captura la salida para verificar si "Posible accidente lateral detectado." se imprime.
        assertTrue(outputStreamCaptor.toString().trim().contains("Posible accidente lateral detectado."));

    }


    @Test
    public void testMasCurvasNoAccidente() {
        long tiempoActual = System.currentTimeMillis();

        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 5, tiempoActual);
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 2, tiempoActual + 1000);
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 5, tiempoActual + 2000);

        // Movimientos posteriores que confirman el accidente
        DatosMovimiento postDato1 = new DatosMovimiento(-42.789100, -65.018700, 10, tiempoActual + 3000);
        DatosMovimiento postDato2 = new DatosMovimiento(-42.789050, -65.018650, 15, tiempoActual + 4000);
        DatosMovimiento postDato3 = new DatosMovimiento(-42.789000, -65.018600, 20, tiempoActual + 5000);
//        DatosMovimiento postDato4 = new DatosMovimiento(-42.78895,-65.01876, 5, tiempoActual + 5000);
//        DatosMovimiento postDato5 = new DatosMovimiento(-42.78887,-65.0188, 5, tiempoActual + 5000);
//        DatosMovimiento postDato6 = new DatosMovimiento(-42.7888,-65.01884, 10, tiempoActual + 5000);
//        DatosMovimiento postDato7 = new DatosMovimiento(-42.78874,-65.01888, 15, tiempoActual + 5000);
        DatosMovimiento postDato4 = new DatosMovimiento(-42.78893,-65.01864, 5, tiempoActual + 5000);
        DatosMovimiento postDato5 = new DatosMovimiento(-42.78887,-65.01868, 5, tiempoActual + 5000);
        DatosMovimiento postDato6 = new DatosMovimiento(-42.7888,-65.0186, 10, tiempoActual + 5000);
        DatosMovimiento postDato7 = new DatosMovimiento(-42.78874,-65.01852, 15, tiempoActual + 5000);


        // Registra los datos
        detectorAccidente.registrarNuevoDato(dato1);
        detectorAccidente.registrarNuevoDato(dato2);
        detectorAccidente.registrarNuevoDato(dato3);
        detectorAccidente.registrarNuevoDato(postDato1);
        detectorAccidente.registrarNuevoDato(postDato2);
        detectorAccidente.registrarNuevoDato(postDato3);
        detectorAccidente.registrarNuevoDato(postDato4);
        detectorAccidente.registrarNuevoDato(postDato5);
        detectorAccidente.registrarNuevoDato(postDato6);
        detectorAccidente.registrarNuevoDato(postDato7);



        // Captura la salida para verificar si "Posible accidente lateral detectado." se imprime.
        assertTrue(outputStreamCaptor.toString().trim().contains("No se detecta accidente."));

    }



    @After
    public void tearDown() {
        // Restaura la salida estándar original
       System.setOut(originalOut);
    }
}
