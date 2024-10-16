package com.example.autoalert.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CreateZipFile{

    // Constructor
    public CreateZipFile() {

    }

    public void createZipFile(ArrayList<String> csvFilePaths, String zipFilePath) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath));

        for (String filePath : csvFilePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                Log.e("ZipError", "El archivo no existe: " + filePath);
                continue; // Saltar si el archivo no existe
            }

            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
            fis.close();
        }

        zos.close();
    }

}