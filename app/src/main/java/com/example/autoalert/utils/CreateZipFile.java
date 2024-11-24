package com.example.autoalert.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CreateZipFile {

    // Método para crear un archivo ZIP a partir de una lista de archivos
    public void createZipFile(ArrayList<String> filePaths, String zipFilePath) throws IOException {
        // Crear un FileOutputStream para el archivo ZIP
        FileOutputStream fos = new FileOutputStream(zipFilePath);

        // Crear un ZipOutputStream para comprimir los archivos
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        // Recorrer los archivos que deseas añadir al ZIP
        for (String filePath : filePaths) {
            File file = new File(filePath);

            // Asegurarse de que el archivo existe
            if (!file.exists()) {
                System.out.println("Archivo no encontrado: " + filePath);
                continue;  // Si no existe, pasamos al siguiente archivo
            }

            // Crear un FileInputStream para leer el archivo que queremos añadir al ZIP
            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());  // El nombre dentro del ZIP
            zipOut.putNextEntry(zipEntry);  // Añadir la entrada al ZIP

            // Leer y escribir el contenido del archivo al ZIP
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zipOut.write(buffer, 0, length);
            }

            fis.close();  // Cerrar el flujo de entrada del archivo
            zipOut.closeEntry();  // Finalizar esta entrada en el ZIP
        }

        zipOut.close();  // Cerrar el ZipOutputStream
        fos.close();  // Cerrar el FileOutputStream

        // Confirmar que el ZIP se ha creado
        System.out.println("Archivo ZIP creado en: " + zipFilePath);
    }
}