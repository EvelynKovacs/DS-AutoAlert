package com.example.autoalert.repository;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SlopeDataWriter {
    // Nombres de archivo para cada tipo de dato
    private static final String FILE_NAME_AX = "slope_data_ax.txt";
    private static final String FILE_NAME_AY = "slope_data_ay.txt";
    private static final String FILE_NAME_AZ = "slope_data_az.txt";
    private static final String FILE_NAME_GX = "slope_data_gx.txt";
    private static final String FILE_NAME_GY = "slope_data_gy.txt";
    private static final String FILE_NAME_GZ = "slope_data_gz.txt";
    private static final String FILE_NAME_V = "slope_data_v.txt";

    // Método para obtener el nombre de archivo según el tipo de dato
    private static String getFileName(String dataType) {
        switch (dataType) {
            case "ax":
                return FILE_NAME_AX;
            case "ay":
                return FILE_NAME_AY;
            case "az":
                return FILE_NAME_AZ;
            case "gx":
                return FILE_NAME_GX;
            case "gy":
                return FILE_NAME_GY;
            case "gz":
                return FILE_NAME_GZ;
            case "v":
                return FILE_NAME_V;
            default:
                throw new IllegalArgumentException("Tipo de dato no válido: " + dataType);
        }
    }

    // Método para escribir los datos con su timestamp en el archivo adecuado
    public static void writeSlopeToFile(Context context, String dataType, String data) {
        try {
            // Obtener la fecha y hora actual
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());

            // Crear el contenido con la fecha y hora
            String dataWithTimestamp = currentTime + " - " + data;

            // Obtener el nombre de archivo adecuado según el tipo de dato
            String fileName = getFileName(dataType);

            // Obtener el archivo en el directorio de archivos internos
            File file = new File(context.getFilesDir(), fileName);

            // Verificar si el archivo existe, si no, crearlo
            if (!file.exists()) {
                file.createNewFile();  // Crear archivo si no existe
            }

            // Escribir datos en el archivo
            try (FileWriter writer = new FileWriter(file, true)) { // `true` para agregar datos sin sobrescribir
                writer.write(dataWithTimestamp + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
