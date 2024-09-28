package com.example.autoalert.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.autoalert.model.entities.ProjectModel;
import com.google.gson.Gson;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UsuarioViewModel extends AndroidViewModel {
    private static final String JSON_FILE_NAME = "user_data.json"; // Nombre del archivo JSON

    public UsuarioViewModel(@NonNull Application application) {
        super(application); // Llama al constructor de la clase base
    }

    public ProjectModel getUsuarioById(int usuarioId) {
        Log.d("UsuarioViewModel", "Buscando usuario con ID: " + usuarioId);

        // Cargar el usuario desde el archivo JSON
        ProjectModel usuario = loadUsuarioFromJson();

        // Verificar si el usuario es el que estamos buscando
        if (usuario != null && usuario.getUsuarioId() == usuarioId) {
            return usuario;
        }

        return null; // Si no se encontró el usuario
    }

    private ProjectModel loadUsuarioFromJson() {
        ProjectModel usuario = null;

        try {
            // Leer el archivo JSON desde el almacenamiento interno
            InputStream inputStream = getApplication().openFileInput("user_data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Convertir el JSON a un objeto ProjectModel
            Gson gson = new Gson();
            usuario = gson.fromJson(reader, ProjectModel.class); // Deserializa un solo objeto

            reader.close();

            if (usuario != null) {
                Log.d("UsuarioViewModel", "Usuario cargado: " + usuario.getNombreUsuario()+" "+usuario.getUsuarioId());
            } else {
                Log.e("UsuarioViewModel", "El usuario es null después de cargar");
            }
        } catch (Exception e) {
            Log.e("UsuarioViewModel", "Error al cargar usuario desde JSON: " + e.getMessage());
        }

        return usuario;
    }

}
