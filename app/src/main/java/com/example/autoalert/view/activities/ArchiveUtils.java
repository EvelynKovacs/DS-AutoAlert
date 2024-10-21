package com.example.autoalert.view.activities;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class ArchiveUtils {

    private Context context;

    public ArchiveUtils(Context context){
        this.context = context;
    }

    public void crearOReiniciarArchivo(String nombreArchivo){
        FileOutputStream fos = null;
        try {
            // Sobrescribir o crear el archivo con el nombre recibido como parámetro
            fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);
            // Al usar Context.MODE_PRIVATE, el archivo se sobrescribe (se limpia)
            Log.i("Archivo", "El archivo '" + nombreArchivo + "' ha sido creado o limpiado exitosamente.");
        } catch (IOException e) {
            Log.e("Archivo", "Error al crear o limpiar el archivo '" + nombreArchivo + "': " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void crearOReiniciarArchivoIps() {
        FileOutputStream fos = null;
        try {
            // Sobrescribir o crear el archivo 'nombres'
            fos = context.openFileOutput("lista-ips", Context.MODE_PRIVATE);
            // Al usar Context.MODE_PRIVATE, el archivo se sobrescribe (se limpia)
            Log.i("Archivo", "El archivo 'lista-ips' ha sido creado o limpiado exitosamente.");
        } catch (IOException e) {
            Log.e("Archivo", "Error al crear o limpiar el archivo 'lista-ips': " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void guardarListaEnArchivo(Set<String> ipList) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("lista-ips", Context.MODE_PRIVATE); // Sobrescribir el archivo
            for (String nombre : ipList) {
                fos.write((nombre + "\n").getBytes()); // Escribir cada nombre en una nueva línea
            }
            Log.i("Archivo", "Lista de ips guardada exitosamente.");
        } catch (IOException e) {
            Log.e("Archivo", "Error al guardar la lista en el archivo: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Set<String> leerListaIpsEnArchivo() {
        Set<String> listaIps = new HashSet<>();
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("lista-ips");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String linea;
            while ((linea = br.readLine()) != null) {
                listaIps.add(linea); // Añadir cada línea a la lista
                Log.i("Archivo", "Se añadio a la lista: " + linea);
            }
            Log.i("Archivo", "Lista de nombres leída exitosamente.");
        } catch (FileNotFoundException e) {
            Log.e("Archivo", "El archivo no existe: " + e.getMessage());
        } catch (IOException e) {
            Log.e("Archivo", "Error al leer el archivo: " + e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return listaIps;
    }

    public void agregarIpYActualizarArchivo(String nuevaIp) {
        // Leer los nombres actuales del archivo
        Set<String> listaIps = leerListaIpsEnArchivo();

        // Añadir el nuevo nombre a la lista
        listaIps.add(nuevaIp);

        // Guardar la lista actualizada en el archivo
        guardarListaEnArchivo(listaIps);
    }


}
