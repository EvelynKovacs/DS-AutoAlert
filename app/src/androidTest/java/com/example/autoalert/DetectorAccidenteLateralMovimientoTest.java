package com.example.autoalert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;

import com.example.autoalert.model.entities.DatosMovimiento;
import com.example.autoalert.utils.DetectorAccidenteLateralMovimiento;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

//@Config(sdk = {28}) // Usa la versión de SDK que necesites
//@RunWith(RobolectricTestRunner.class)

public class DetectorAccidenteLateralMovimientoTest extends Application {






    private DetectorAccidenteLateralMovimiento detectorAccidente;
    private  ByteArrayOutputStream outputStreamCaptor;
    private  PrintStream originalOut ;

    @Before
    public void setup() {
//        Context context = ApplicationProvider.getApplicationContext();
//        detectorAccidente = new DetectorAccidenteLateral(context);

       Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(context); // Mockear el contexto
//
        detectorAccidente = new DetectorAccidenteLateralMovimiento(context);

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
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 50, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 5, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s

         DatosMovimiento dato4 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.78905,-65.01881, 12, tiempoActual + 4000); // Continúa movimiento
        DatosMovimiento dato6 = new DatosMovimiento(-42.789,-65.01883, 15, tiempoActual + 5000); // Retorna a la velocidad original
        DatosMovimiento dato7 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 6000); // Retoma la velocidad
        DatosMovimiento dato8 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 7000); // Continúa movimiento
        DatosMovimiento dato9 = new DatosMovimiento(-42.789,-65.01883, 15, tiempoActual + 8000); // Retorna a la velocidad original
        DatosMovimiento dato10 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 9000); // Retoma la velocidad
        DatosMovimiento dato11 = new DatosMovimiento(-42.78905,-65.01881, 12, tiempoActual + 10000); // Continúa movimiento
        DatosMovimiento dato12 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato13 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 12000); // Retoma la velocidad

        // Registra los datos
        assertFalse(detectorAccidente.registrarNuevoDato(dato1));
        assertFalse(detectorAccidente.registrarNuevoDato(dato2));
        assertFalse(detectorAccidente.registrarNuevoDato(dato3));
        assertFalse(detectorAccidente.registrarNuevoDato(dato4));
        assertFalse(detectorAccidente.registrarNuevoDato(dato5));
        assertFalse(detectorAccidente.registrarNuevoDato(dato6));
        assertFalse(detectorAccidente.registrarNuevoDato(dato7));
        assertFalse(detectorAccidente.registrarNuevoDato(dato8));
        assertFalse(detectorAccidente.registrarNuevoDato(dato9));
        assertFalse(detectorAccidente.registrarNuevoDato(dato10));
        assertFalse(detectorAccidente.registrarNuevoDato(dato11));
        assertFalse(detectorAccidente.registrarNuevoDato(dato12));
        assertFalse(detectorAccidente.registrarNuevoDato(dato13));


    }
    @Test
    public void testNoDesaceleracionNoAccidente() {
        long tiempoActual = System.currentTimeMillis();



        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 20, tiempoActual);  // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 18, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 5, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s

        DatosMovimiento dato4 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.78905,-65.01881, 12, tiempoActual + 4000); // Continúa movimiento
        DatosMovimiento dato6 = new DatosMovimiento(-42.789,-65.01883, 15, tiempoActual + 5000); // Retorna a la velocidad original
        DatosMovimiento dato7 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 6000); // Retoma la velocidad
        DatosMovimiento dato8 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 7000); // Continúa movimiento
        DatosMovimiento dato9 = new DatosMovimiento(-42.789,-65.01883, 15, tiempoActual + 8000); // Retorna a la velocidad original
        DatosMovimiento dato10 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 9000); // Retoma la velocidad
        DatosMovimiento dato11 = new DatosMovimiento(-42.78905,-65.01881, 12, tiempoActual + 10000); // Continúa movimiento
        DatosMovimiento dato12 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato13 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 12000); // Retoma la velocidad

        // Registra los datos
        assertFalse(detectorAccidente.registrarNuevoDato(dato1));
        assertFalse(detectorAccidente.registrarNuevoDato(dato2));
        assertFalse(detectorAccidente.registrarNuevoDato(dato3));
        assertFalse(detectorAccidente.registrarNuevoDato(dato4));
        assertFalse(detectorAccidente.registrarNuevoDato(dato5));
        assertFalse(detectorAccidente.registrarNuevoDato(dato6));
        assertFalse(detectorAccidente.registrarNuevoDato(dato7));
        assertFalse(detectorAccidente.registrarNuevoDato(dato8));
        assertFalse(detectorAccidente.registrarNuevoDato(dato9));
        assertFalse(detectorAccidente.registrarNuevoDato(dato10));
        assertFalse(detectorAccidente.registrarNuevoDato(dato11));
        assertFalse(detectorAccidente.registrarNuevoDato(dato12));
        assertFalse(detectorAccidente.registrarNuevoDato(dato13));


    }


    @Test
    public void testAccidente1() {
        long tiempoActual = System.currentTimeMillis();


        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 20, tiempoActual);  // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 50, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 5, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s

        DatosMovimiento dato4 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.78905,-65.01881, 12, tiempoActual + 4000); // Continúa movimiento
        DatosMovimiento dato6 = new DatosMovimiento(-42.789,-65.01883, 15, tiempoActual + 5000); // Retorna a la velocidad original
        DatosMovimiento dato7 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 6000); // Retoma la velocidad
        DatosMovimiento dato8 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 7000); // Continúa movimiento
        DatosMovimiento dato9 = new DatosMovimiento(-42.789,-65.01883, 15, tiempoActual + 8000); // Retorna a la velocidad original
        DatosMovimiento dato10 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 9000); // Retoma la velocidad
        DatosMovimiento dato11 = new DatosMovimiento(-42.78905,-65.01881, 12, tiempoActual + 10000); // Continúa movimiento
        DatosMovimiento dato12 = new DatosMovimiento(-42.789,-65.01883, 10, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato13 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato14 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 13000); // Retorna a la velocidad original
        DatosMovimiento dato15 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 14000); // Retoma la velocidad
        DatosMovimiento dato16 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 15000); // Retorna a la velocidad original
        DatosMovimiento dato17 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 16000); // Retoma la velocidad
        DatosMovimiento dato18 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 17000); // Retorna a la velocidad original
        DatosMovimiento dato19 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 18000); // Retoma la velocidad
        DatosMovimiento dato20 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 19000); // Retorna a la velocidad original
        DatosMovimiento dato21 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 20000); // Retoma la velocidad
        DatosMovimiento dato22 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 21000); // Retorna a la velocidad original
        DatosMovimiento dato23 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 22000); // Retoma la velocidad
        DatosMovimiento dato24 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 23000); // Retorna a la velocidad original
        DatosMovimiento dato25 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 24000); // Retoma la velocidad
        DatosMovimiento dato26 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 25000); // Retorna a la velocidad original
        DatosMovimiento dato27 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 26000); // Retoma la velocidad
        DatosMovimiento dato28 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 27000); // Retorna a la velocidad original
        DatosMovimiento dato29 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 28000); // Retoma la velocidad
        DatosMovimiento dato30 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 29000); // Retorna a la velocidad original
        DatosMovimiento dato31 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 30000); // Retoma la velocidad
        DatosMovimiento dato32 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 31000); // Retorna a la velocidad original
        DatosMovimiento dato33 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 32000); // Retoma la velocidad
        DatosMovimiento dato34 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 33000); // Retorna a la velocidad original
        DatosMovimiento dato35 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 34000); // Retoma la velocidad

        // Registra los datos
        assertFalse(detectorAccidente.registrarNuevoDato(dato1));
        assertFalse(detectorAccidente.registrarNuevoDato(dato2));
        assertFalse(detectorAccidente.registrarNuevoDato(dato3));
        assertFalse(detectorAccidente.registrarNuevoDato(dato4));
        assertFalse(detectorAccidente.registrarNuevoDato(dato5));
        assertFalse(detectorAccidente.registrarNuevoDato(dato6));
        assertFalse(detectorAccidente.registrarNuevoDato(dato7));
        assertFalse(detectorAccidente.registrarNuevoDato(dato8));
        assertFalse(detectorAccidente.registrarNuevoDato(dato9));
        assertFalse(detectorAccidente.registrarNuevoDato(dato10));
        assertFalse(detectorAccidente.registrarNuevoDato(dato11));
        assertFalse(detectorAccidente.registrarNuevoDato(dato12));
        assertFalse(detectorAccidente.registrarNuevoDato(dato13));
        assertFalse(detectorAccidente.registrarNuevoDato(dato14));
        assertFalse(detectorAccidente.registrarNuevoDato(dato15));
        assertFalse(detectorAccidente.registrarNuevoDato(dato16));
        assertFalse(detectorAccidente.registrarNuevoDato(dato17));
        assertFalse(detectorAccidente.registrarNuevoDato(dato18));
        assertFalse(detectorAccidente.registrarNuevoDato(dato19));
        assertFalse(detectorAccidente.registrarNuevoDato(dato20));
        assertFalse(detectorAccidente.registrarNuevoDato(dato21));
        assertFalse(detectorAccidente.registrarNuevoDato(dato22));
        assertFalse(detectorAccidente.registrarNuevoDato(dato23));
        assertFalse(detectorAccidente.registrarNuevoDato(dato24));
        assertFalse(detectorAccidente.registrarNuevoDato(dato25));
        assertFalse(detectorAccidente.registrarNuevoDato(dato26));
        assertFalse(detectorAccidente.registrarNuevoDato(dato27));
        assertFalse(detectorAccidente.registrarNuevoDato(dato28));
        assertFalse(detectorAccidente.registrarNuevoDato(dato29));
        assertFalse(detectorAccidente.registrarNuevoDato(dato30));
        assertFalse(detectorAccidente.registrarNuevoDato(dato31));
        assertFalse(detectorAccidente.registrarNuevoDato(dato32));
        assertFalse(detectorAccidente.registrarNuevoDato(dato33));
        assertFalse(detectorAccidente.registrarNuevoDato(dato34));
        assertTrue(detectorAccidente.registrarNuevoDato(dato35));


    }

    @Test
    public void testAccidente2() {
        long tiempoActual = System.currentTimeMillis();


        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 20, tiempoActual);  // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 50, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 5, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s

        DatosMovimiento dato4 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 4000); // Continúa movimiento
        DatosMovimiento dato6 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 5000); // Retorna a la velocidad original
        DatosMovimiento dato7 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 6000); // Retoma la velocidad
        DatosMovimiento dato8 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 7000); // Continúa movimiento
        DatosMovimiento dato9 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 8000); // Retorna a la velocidad original
        DatosMovimiento dato10 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 9000); // Retoma la velocidad
        DatosMovimiento dato11 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 10000); // Continúa movimiento
        DatosMovimiento dato12 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato13 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato14 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato15 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato16 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato17 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato18 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato19 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato20 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato21 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato22 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato23 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato24 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato25 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato26 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato27 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad

        // Registra los datos
        assertFalse(detectorAccidente.registrarNuevoDato(dato1));
        assertFalse(detectorAccidente.registrarNuevoDato(dato2));
        assertFalse(detectorAccidente.registrarNuevoDato(dato3));
        assertFalse(detectorAccidente.registrarNuevoDato(dato4));
        assertFalse(detectorAccidente.registrarNuevoDato(dato5));
        assertFalse(detectorAccidente.registrarNuevoDato(dato6));
        assertFalse(detectorAccidente.registrarNuevoDato(dato7));
        assertFalse(detectorAccidente.registrarNuevoDato(dato8));
        assertFalse(detectorAccidente.registrarNuevoDato(dato9));
        assertFalse(detectorAccidente.registrarNuevoDato(dato10));
        assertFalse(detectorAccidente.registrarNuevoDato(dato11));
        assertFalse(detectorAccidente.registrarNuevoDato(dato12));
        assertFalse(detectorAccidente.registrarNuevoDato(dato13));
        assertFalse(detectorAccidente.registrarNuevoDato(dato14));
        assertFalse(detectorAccidente.registrarNuevoDato(dato15));
        assertFalse(detectorAccidente.registrarNuevoDato(dato16));
        assertFalse(detectorAccidente.registrarNuevoDato(dato17));
        assertFalse(detectorAccidente.registrarNuevoDato(dato18));
        assertFalse(detectorAccidente.registrarNuevoDato(dato19));
        assertFalse(detectorAccidente.registrarNuevoDato(dato20));
        assertFalse(detectorAccidente.registrarNuevoDato(dato21));
        assertFalse(detectorAccidente.registrarNuevoDato(dato22));
        assertFalse(detectorAccidente.registrarNuevoDato(dato23));
        assertFalse(detectorAccidente.registrarNuevoDato(dato24));
        assertFalse(detectorAccidente.registrarNuevoDato(dato25));
        assertFalse(detectorAccidente.registrarNuevoDato(dato26));
        assertTrue(detectorAccidente.registrarNuevoDato(dato27));



    }


    @Test
    public void testAccidente3() {
        long tiempoActual = System.currentTimeMillis();


        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 20, tiempoActual);  // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722, 10, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 30, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s

        DatosMovimiento dato4 = new DatosMovimiento(-42.7891,-65.01879, 10, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 4000); // Continúa movimiento
        DatosMovimiento dato6 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 5000); // Retorna a la velocidad original
        DatosMovimiento dato7 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 6000); // Retoma la velocidad
        DatosMovimiento dato8 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 7000); // Continúa movimiento
        DatosMovimiento dato9 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 8000); // Retorna a la velocidad original
        DatosMovimiento dato10 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 9000); // Retoma la velocidad
        DatosMovimiento dato11 = new DatosMovimiento(-42.78905,-65.01881, 0, tiempoActual + 10000); // Continúa movimiento
        DatosMovimiento dato12 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato13 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato14 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato15 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato16 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato17 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato18 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato19 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato20 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato21 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato22 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato23 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato24 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato25 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad
        DatosMovimiento dato26 = new DatosMovimiento(-42.789,-65.01883, 0, tiempoActual + 11000); // Retorna a la velocidad original
        DatosMovimiento dato27 = new DatosMovimiento(-42.7891,-65.01879, 0, tiempoActual + 12000); // Retoma la velocidad

        // Registra los datos
        assertFalse(detectorAccidente.registrarNuevoDato(dato1));
        assertFalse(detectorAccidente.registrarNuevoDato(dato2));
        assertFalse(detectorAccidente.registrarNuevoDato(dato3));
        assertFalse(detectorAccidente.registrarNuevoDato(dato4));
        assertFalse(detectorAccidente.registrarNuevoDato(dato5));
        assertFalse(detectorAccidente.registrarNuevoDato(dato6));
        assertFalse(detectorAccidente.registrarNuevoDato(dato7));
        assertFalse(detectorAccidente.registrarNuevoDato(dato8));
        assertFalse(detectorAccidente.registrarNuevoDato(dato9));
        assertFalse(detectorAccidente.registrarNuevoDato(dato10));
        assertFalse(detectorAccidente.registrarNuevoDato(dato11));
        assertFalse(detectorAccidente.registrarNuevoDato(dato12));
        assertFalse(detectorAccidente.registrarNuevoDato(dato13));
        assertFalse(detectorAccidente.registrarNuevoDato(dato14));
        assertFalse(detectorAccidente.registrarNuevoDato(dato15));
        assertFalse(detectorAccidente.registrarNuevoDato(dato16));
        assertFalse(detectorAccidente.registrarNuevoDato(dato17));
        assertFalse(detectorAccidente.registrarNuevoDato(dato18));
        assertFalse(detectorAccidente.registrarNuevoDato(dato19));
        assertFalse(detectorAccidente.registrarNuevoDato(dato20));
        assertFalse(detectorAccidente.registrarNuevoDato(dato21));
        assertFalse(detectorAccidente.registrarNuevoDato(dato22));
        assertFalse(detectorAccidente.registrarNuevoDato(dato23));
        assertFalse(detectorAccidente.registrarNuevoDato(dato24));
        assertFalse(detectorAccidente.registrarNuevoDato(dato25));
        assertFalse(detectorAccidente.registrarNuevoDato(dato26));
        assertTrue(detectorAccidente.registrarNuevoDato(dato27));



    }




    @After
    public void tearDown() {
        // Restaura la salida estándar original
    }
}
