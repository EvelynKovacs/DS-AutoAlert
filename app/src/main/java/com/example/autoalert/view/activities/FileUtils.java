package com.example.autoalert.view.activities;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileUtils {

    private Context context;

    public FileUtils(Context context){
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

    public void addAndRefreshMap(String fileName, String ip, String message){
        HashMap<String, String> resultMap = readMapfromFile(fileName);

        resultMap.put(ip, message);

        saveMapInFile(fileName, resultMap);
    }

    public HashMap<String, String> readMapfromFile(String fileName) {
        HashMap<String, String> resultMap = new HashMap<>();
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] readLine = linea.split("-");
                resultMap.put(readLine[0], readLine[1]); // Añadir cada línea a la lista
                Log.i("Archivo", "Se añadio a la lista: " + readLine[0]  + " con " + readLine[1]);
            }
            Log.i("Archivo", "Mapa de " + fileName + " leída exitosamente.");
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
        return resultMap;
    }

    public void saveMapInFile(String fileName, HashMap<String, String> mapToSave) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE); // Sobrescribir el archivo
            for (Map.Entry<String, String> line : mapToSave.entrySet()) {
                fos.write((line.getKey() + "-" + line.getValue() + "\n").getBytes()); // Escribir cada nombre en una nueva línea
            }
            Log.i("Archivo", "Map "+ fileName + " guardada exitosamente.");
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

    public String readState(){
        String state = "";
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("state");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String linea;
            while ((linea = br.readLine()) != null) {
                state = linea;
                Log.i("Archivo", "Se obtuvo el estado: " + state);
            }
            Log.i("Archivo", "Archivo de Estado leída exitosamente.");
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
        return state;
    }

    public void saveStateInFile(String state) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("state", Context.MODE_PRIVATE); // Sobrescribir el archivo
            fos.write((state.getBytes())); // Escribir cada nombre en una nueva línea
            Log.i("Archivo", "Archivo Estado guardado exitosamente con: " + state);
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

    public void clearAppFilesContent() {
        File dir = context.getFilesDir();
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        // Escribir un contenido vacío para vaciar el archivo
                        fos.write(new byte[0]);
                        Log.i("FileClear", "Archivo " + file.getName() + " vaciado");
                    } catch (IOException e) {
                        Log.e("FileClear", "Error al vaciar el archivo " + file.getName(), e);
                    }
                }
            }
        }
    }

    public void deleteIpFromListAndMap(String ip) {
        Set<String> ipList = leerListaIpsEnArchivo();
        ipList.remove(ip);
        guardarListaEnArchivo(ipList);

        HashMap<String, String> votosMap = readMapfromFile("map-ip-vote");
        votosMap.remove(ip);
        saveMapInFile("map-ip-vote", votosMap);

        Log.i("Archivo", "Se elimino la ip " + ip + " de las listas");
    }





}
