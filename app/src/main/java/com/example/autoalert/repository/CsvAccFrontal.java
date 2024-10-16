package com.example.autoalert.repository;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CsvAccFrontal {
    private File csvFile;

    // Constructor
    public CsvAccFrontal(Context context) {
        createCsvFile(context);
    }

    private void createCsvFile(Context context) {
        try {
            // Verificar si el almacenamiento externo está disponible
            if (isExternalStorageWritable()) {
                // Crear el archivo .csv
                File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                csvFile = new File(storageDir, "datos_acc_frontal.csv");
                FileWriter writer = new FileWriter(csvFile);

                // Escribir encabezados
                writer.append("Time,Velocidad (km/h), Latitud,Longitud,Umbral,Dif_Vel,Dif_Brusca,Accidente\n");
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

    public void saveDataToCsv(double speedKmh, double latitude, double longitude,double umbral,double dv,boolean db,boolean accidentFlag ) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date()); // Obtiene la fecha y hora actual
            FileWriter writer = new FileWriter(csvFile, true);

            writer.append(String.format("\"%s\",\"%.2f\",\"%f\",\"%f\",\"%f\",\"%f\",\"%b\",\"%b\"\n",
                    currentTime, speedKmh, latitude, longitude,umbral,dv,db, accidentFlag));
            // writer.append(String.format("\"%.2f\",\"%f\",\"%f\",\"%s\"\n", speedKmh, latitude, longitude, address));
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
