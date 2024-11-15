package com.example.autoalert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;

import com.example.autoalert.model.entities.DatosMovimiento;
import com.example.autoalert.utils.DetectorAccidenteTrasero;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DetectorAccidenteTraseroTest {
    private DetectorAccidenteTrasero detectorAccidente;


    @Before
    public void setup() {
//        Context context = ApplicationProvider.getApplicationContext();
//        detectorAccidente = new DetectorAccidenteLateral(context);

        Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(context); // Mockear el contexto
//
        detectorAccidente = new DetectorAccidenteTrasero(context);

        //detectorAccidente = new DetectorAccidente(getApplicationContext());
        //originalOut = System.out;
        //System.setOut(new PrintStream(outputStreamCaptor));


    }

    @Test
    public void testAceleracionNoAccidente() {
        long tiempoActual = System.currentTimeMillis();



        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 50, tiempoActual);       // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722,40, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 60, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s
        DatosMovimiento dato4 = new DatosMovimiento(-42.789100, -65.018790, 20, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.789050, -65.018810, 10, tiempoActual + 4000); // Continúa movimiento a 12 m/s
        DatosMovimiento dato6 = new DatosMovimiento(-42.789000, -65.018830, 8, tiempoActual + 5000); // Aumenta a 15 m/s
        DatosMovimiento dato7 = new DatosMovimiento(-42.788950, -65.018850, 8, tiempoActual + 6000); // Aumenta a 18 m/s
        DatosMovimiento dato8 = new DatosMovimiento(-42.788900, -65.018870, 8, tiempoActual + 7000); // Vuelve a 20 m/s
        DatosMovimiento dato9 = new DatosMovimiento(-42.788850, -65.018890, 5, tiempoActual + 8000); // Aumenta a 22 m/s
        DatosMovimiento dato10 = new DatosMovimiento(-42.788800, -65.018910, 6, tiempoActual + 9000); // Velocidad máxima de 25 m/s
        DatosMovimiento dato11 = new DatosMovimiento(-42.788750, -65.018930, 2, tiempoActual + 10000); // Reducción brusca a 15 m/s
        DatosMovimiento dato12 = new DatosMovimiento(-42.788700, -65.018950, 0, tiempoActual + 11000); // Baja a 10 m/s
        DatosMovimiento dato13 = new DatosMovimiento(-42.788650, -65.018970, 0, tiempoActual + 12000);  // Baja a 5 m/s
        //DatosMovimiento dato14 = new DatosMovimiento(-42.788600, -65.018990, 0, tiempoActual + 13000);  // Sube ligeramente a 8 m/s
//        DatosMovimiento dato15 = new DatosMovimiento(-42.788550, -65.019010, 5, tiempoActual + 14000); // Retoma velocidad constante a 12 m/s
//        DatosMovimiento dato16 = new DatosMovimiento(-42.788500, -65.019030, 0, tiempoActual + 15000);  // Baja velocidad a 7 m/s
//        DatosMovimiento dato17 = new DatosMovimiento(-42.788450, -65.019050, 0, tiempoActual + 16000);  // Disminuye a 3 m/s
//        DatosMovimiento dato18 = new DatosMovimiento(-42.788400, -65.019070, 0, tiempoActual + 17000);  // Llega a detenerse a 0 m/s


        // Registra los datos
        detectorAccidente.registrarNuevoDato(dato1);
        detectorAccidente.registrarNuevoDato(dato2);
        detectorAccidente.registrarNuevoDato(dato3);
        detectorAccidente.registrarNuevoDato(dato4);
        detectorAccidente.registrarNuevoDato(dato5);
        detectorAccidente.registrarNuevoDato(dato6);
        detectorAccidente.registrarNuevoDato(dato7);
       assertFalse( detectorAccidente.registrarNuevoDato(dato8));
        assertFalse(detectorAccidente.registrarNuevoDato(dato9));
        assertFalse(detectorAccidente.registrarNuevoDato(dato10));
        assertFalse(detectorAccidente.registrarNuevoDato(dato11));
        assertFalse(detectorAccidente.registrarNuevoDato(dato12));
        assertFalse(detectorAccidente.registrarNuevoDato(dato13));
        //assertTrue(detectorAccidente.registrarNuevoDato(dato14));
//        detectorAccidente.registrarNuevoDato(dato15);
//        detectorAccidente.registrarNuevoDato(dato16);
//        assertFalse(detectorAccidente.registrarNuevoDato(dato17));
//        assertTrue(detectorAccidente.registrarNuevoDato(dato18));
    }
    @Test
    public void testAceleracionAccidente() {
        long tiempoActual = System.currentTimeMillis();



        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 50, tiempoActual);       // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722,40, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 60, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s
        DatosMovimiento dato4 = new DatosMovimiento(-42.789100, -65.018790, 7, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.789050, -65.018810, 0, tiempoActual + 4000); // Continúa movimiento a 12 m/s
        DatosMovimiento dato6 = new DatosMovimiento(-42.789000, -65.018830, 0, tiempoActual + 5000); // Aumenta a 15 m/s
        DatosMovimiento dato7 = new DatosMovimiento(-42.788950, -65.018850, 0, tiempoActual + 6000); // Aumenta a 18 m/s
        DatosMovimiento dato8 = new DatosMovimiento(-42.788900, -65.018870, 0, tiempoActual + 7000); // Vuelve a 20 m/s
        DatosMovimiento dato9 = new DatosMovimiento(-42.788850, -65.018890, 0, tiempoActual + 8000); // Aumenta a 22 m/s
        DatosMovimiento dato10 = new DatosMovimiento(-42.788800, -65.018910, 0, tiempoActual + 9000); // Velocidad máxima de 25 m/s
        DatosMovimiento dato11 = new DatosMovimiento(-42.788750, -65.018930, 0, tiempoActual + 10000); // Reducción brusca a 15 m/s
        DatosMovimiento dato12 = new DatosMovimiento(-42.788800, -65.018910, 0, tiempoActual + 11000); // Velocidad máxima de 25 m/s
        DatosMovimiento dato13 = new DatosMovimiento(-42.788750, -65.018930, 0, tiempoActual + 12000); // Reducción brusca a 15 m/s
        //DatosMovimiento dato14 = new DatosMovimiento(-42.788750, -65.018930, 0, tiempoActual + 13000); // Reducción brusca a 15 m/s


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
        assertTrue(detectorAccidente.registrarNuevoDato(dato13));
        //assertFalse(detectorAccidente.registrarNuevoDato(dato14));

//        detectorAccidente.registrarNuevoDato(dato15);
//        detectorAccidente.registrarNuevoDato(dato16);
//        assertFalse(detectorAccidente.registrarNuevoDato(dato17));
//        assertTrue(detectorAccidente.registrarNuevoDato(dato18));











        // assertTrue(outputStreamCaptor.toString().trim().contains("No se detecta accidente."));
    }

    @Test
    public void testNoAccidenteRompePatron() {
        long tiempoActual = System.currentTimeMillis();



        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 50, tiempoActual);       // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722,40, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 60, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s
        DatosMovimiento dato4 = new DatosMovimiento(-42.789100, -65.018790, 7, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.789050, -65.018810, 28, tiempoActual + 4000); // Continúa movimiento a 12 m/s
        DatosMovimiento dato6 = new DatosMovimiento(-42.789000, -65.018830, 0, tiempoActual + 5000); // Aumenta a 15 m/s
        DatosMovimiento dato7 = new DatosMovimiento(-42.788950, -65.018850, 7, tiempoActual + 6000); // Aumenta a 18 m/s
        DatosMovimiento dato8 = new DatosMovimiento(-42.788900, -65.018870, 7, tiempoActual + 7000); // Vuelve a 20 m/s
        DatosMovimiento dato9 = new DatosMovimiento(-42.788850, -65.018890, 5, tiempoActual + 8000); // Aumenta a 22 m/s
        DatosMovimiento dato10 = new DatosMovimiento(-42.788800, -65.018910, 5, tiempoActual + 9000); // Velocidad máxima de 25 m/s
        DatosMovimiento dato11 = new DatosMovimiento(-42.788750, -65.018930, 0, tiempoActual + 10000); // Reducción brusca a 15 m/s
        DatosMovimiento dato12 = new DatosMovimiento(-42.788550, -65.019010, 0, tiempoActual + 11000); // Retoma velocidad constante a 12 m/s
       DatosMovimiento dato13 = new DatosMovimiento(-42.788500, -65.019030, 0, tiempoActual + 12000);  // Baja velocidad a 7 m/s
//        DatosMovimiento dato17 = new DatosMovimiento(-42.788450, -65.019050, 0, tiempoActual + 16000);  // Disminuye a 3 m/s
//        DatosMovimiento dato18 = new DatosMovimiento(-42.788400, -65.019070, 0, tiempoActual + 17000);  // Llega a detenerse a 0 m/s


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

//        detectorAccidente.registrarNuevoDato(dato15);
//        detectorAccidente.registrarNuevoDato(dato16);
//        assertFalse(detectorAccidente.registrarNuevoDato(dato17));
//        assertTrue(detectorAccidente.registrarNuevoDato(dato18));











        // assertTrue(outputStreamCaptor.toString().trim().contains("No se detecta accidente."));
    }
    @Test
    public void testAceleracionSinAccidente() {
        long tiempoActual = System.currentTimeMillis();



        // Crea datos que simulen una desaceleración que no termina en accidente
        DatosMovimiento dato1 = new DatosMovimiento(-42.789351, -65.019023, 50, tiempoActual);       // Velocidad de 20 m/s
        DatosMovimiento dato2 = new DatosMovimiento(-42.789256, -65.018722,40, tiempoActual + 1000); // 15 m/s después de 1 segundo
        DatosMovimiento dato3 = new DatosMovimiento(-42.789162, -65.018765, 60, tiempoActual + 2000);  // Desaceleración brusca a 5 m/s
        DatosMovimiento dato4 = new DatosMovimiento(-42.789100, -65.018790, 7, tiempoActual + 3000); // Retoma la velocidad
        DatosMovimiento dato5 = new DatosMovimiento(-42.789050, -65.018810, 28, tiempoActual + 4000); // Continúa movimiento a 12 m/s
        DatosMovimiento dato6 = new DatosMovimiento(-42.789000, -65.018830, 0, tiempoActual + 5000); // Aumenta a 15 m/s
        DatosMovimiento dato7 = new DatosMovimiento(-42.788950, -65.018850, 7, tiempoActual + 6000); // Aumenta a 18 m/s
        DatosMovimiento dato8 = new DatosMovimiento(-42.788900, -65.018870, 7, tiempoActual + 7000); // Vuelve a 20 m/s
        DatosMovimiento dato9 = new DatosMovimiento(-42.788850, -65.018890, 5, tiempoActual + 8000); // Aumenta a 22 m/s
        DatosMovimiento dato10 = new DatosMovimiento(-42.788800, -65.018910, 5, tiempoActual + 9000); // Velocidad máxima de 25 m/s
        DatosMovimiento dato11 = new DatosMovimiento(-42.788750, -65.018930, 12, tiempoActual + 10000); // Reducción brusca a 15 m/s
        DatosMovimiento dato12 = new DatosMovimiento(-42.788550, -65.019010, 0, tiempoActual + 11000); // Retoma velocidad constante a 12 m/s
        DatosMovimiento dato13 = new DatosMovimiento(-42.788500, -65.019030, 0, tiempoActual + 12000);  // Baja velocidad a 7 m/s
       DatosMovimiento dato14 = new DatosMovimiento(-42.788450, -65.019050, 0, tiempoActual + 13000);  // Disminuye a 3 m/s
//        DatosMovimiento dato18 = new DatosMovimiento(-42.788400, -65.019070, 0, tiempoActual + 17000);  // Llega a detenerse a 0 m/s


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

    }
    @After
    public void tearDown() {
        // Restaura la salida estándar original
    }
}
