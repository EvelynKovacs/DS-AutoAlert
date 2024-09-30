package com.example.autoalert.repository;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccelerationDataWriter {

        private static final String FILE_NAME = "acceleration_data.txt";

        public static void writeAcceleration(Context context, String data) {
            try {
                // Obtener la fecha y hora actual
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentTime = sdf.format(new Date());

                // Crear el contenido con la fecha y hora
                String dataWithTimestamp = currentTime + " - " + data;

                // Obtener el archivo en el directorio de archivos internos
                File file = new File(context.getFilesDir(), FILE_NAME);

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
