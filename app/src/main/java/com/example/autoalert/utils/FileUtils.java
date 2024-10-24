package com.example.autoalert.utils;

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
            fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);
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

    public void guardarListaEnArchivo(Set<String> ipList) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("lista-ips", Context.MODE_PRIVATE); // Sobrescribir el archivo
            for (String nombre : ipList) {
                fos.write((nombre + "\n").getBytes()); // Escribir cada nombre en una nueva línea
            }

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

            }

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
        Set<String> listaIps = leerListaIpsEnArchivo();
        listaIps.add(nuevaIp);
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

            }

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
            }
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
                    // Verificar si el archivo no es uno de los que se deben conservar
                    if (!file.getName().equals("ubicaciones_periodicas.json") &&
                            !file.getName().equals("ultima_ubicacion.json") &&
                            !file.getName().equals("user_data.json")) {

                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            // Escribir un contenido vacío para vaciar el archivo
                            fos.write(new byte[0]);
                            Log.i("FileClear", "Archivo " + file.getName() + " vaciado");
                        } catch (IOException e) {
                            Log.e("FileClear", "Error al vaciar el archivo " + file.getName(), e);
                        }
                    } else {
                        Log.i("FileClear", "Archivo " + file.getName() + " no se vació");
                    }
                }
            }
        }
    }


    public void clearVotoFileContent(){
        File file = new File(context.getFilesDir(), "map-ip-voto");
        if (file.exists()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                // Escribir un contenido vacío para vaciar el archivo
                fos.write(new byte[0]);
                Log.i("FileClear", "Archivo 'votos' vaciado");
            } catch (IOException e) {
                Log.e("FileClear", "Error al vaciar el archivo 'votos'", e);
            }
        } else {
            Log.e("FileClear", "El archivo 'votos' no existe");
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
