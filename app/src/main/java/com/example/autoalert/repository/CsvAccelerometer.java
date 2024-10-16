package com.example.autoalert.repository;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CsvAccelerometer {
    private File csvFile;

    // Constructor
    public CsvAccelerometer(Context context) {
        createCsvFile(context);
    }

    private void createCsvFile(Context context) {
        try {
            // Verificar si el almacenamiento externo está disponible
            if (isExternalStorageWritable()) {
                // Crear el archivo .csv
                File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                csvFile = new File(storageDir, "datos_acelerometro.csv");
                FileWriter writer = new FileWriter(csvFile);

                // Escribir encabezados
                writer.append("Time,X,Y,Z\n");
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para verificar si el almacenamiento externo está disponible
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public void saveDataToCsv(double x, double y, double z) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date()); // Obtiene la fecha y hora actual
            FileWriter writer = new FileWriter(csvFile, true);

            // Utilizamos Double.toString() para no limitar los decimales
            writer.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    currentTime, Double.toString(x), Double.toString(y), Double.toString(z)));

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // Método para guardar los datos sin parámetros explícitos
//    public void saveData(double speedKmh, double latitude, double longitude, String address) {
//        saveDataToCsv(speedKmh, latitude, longitude, address);
//    }

    public String getCsvFilePath() {
        if (csvFile != null) {
            return csvFile.getAbsolutePath();
        }
        return null;
    }

}
