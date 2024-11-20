package com.example.autoalert.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.autoalert.model.entities.ProjectModel;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProjectViewModel extends AndroidViewModel {
    private static final String FILE_NAME = "user_data.json";
    private MutableLiveData<ProjectModel> projectLiveData;

    public ProjectViewModel(Application application) {
        super(application);
        projectLiveData = new MutableLiveData<>();
        loadProjectsFromJson(); // Cargar proyectos al iniciar el ViewModel
    }


    // Método para agregar un proyecto
    public void addProject(ProjectModel newProject) {
        projectLiveData.setValue(newProject); // Establecer el nuevo proyecto
        saveProjectsToJson(newProject); // Guardar el proyecto en JSON
    }



    // Método para cargar los proyectos desde el archivo JSON
    // Método para cargar el proyecto desde el archivo JSON
    private void loadProjectsFromJson() {
        try {
            FileInputStream fis = getApplication().openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            Gson gson = new Gson();

            // Verifica si el archivo tiene datos
            JsonElement jsonElement = JsonParser.parseReader(isr);

            if (jsonElement.isJsonNull() || !jsonElement.isJsonObject()) {
                // Si el archivo está vacío o no contiene un objeto, inicializa un proyecto vacío
                ProjectModel emptyProject = new ProjectModel();  // Un proyecto vacío
                projectLiveData.setValue(emptyProject); // Establecer el objeto vacío
            } else {
                // Si contiene un proyecto, deserialízalo
                ProjectModel project = gson.fromJson(jsonElement, ProjectModel.class);
                projectLiveData.setValue(project); // Establecer el proyecto cargado
            }

            fis.close();
        } catch (Exception e) {
            Log.e("ProjectViewModel", "Error al cargar el proyecto", e);
            projectLiveData.setValue(new ProjectModel()); // En caso de error, asegura un objeto vacío
        }
    }





    // Método para obtener todos los proyectos como LiveData
    public LiveData<ProjectModel> getAllProjectLive() {
        return projectLiveData;
    }

    // Método para eliminar el proyecto y actualizar el archivo JSON
    public void deleteProject() {
        ProjectModel emptyProject = new ProjectModel();  // Crear un proyecto vacío
        projectLiveData.setValue(emptyProject); // Establecer el proyecto vacío
        saveProjectsToJson(emptyProject); // Guardar el proyecto vacío
    }


    // Método para guardar los proyectos en el archivo JSON
    private void saveProjectsToJson(ProjectModel project) {
        try {
            // Crear un nuevo archivo si no existe
            FileOutputStream fos = getApplication().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            Gson gson = new Gson();

            // Guardar el objeto como JSON, no una lista
            String json = gson.toJson(project);  // Guarda el proyecto directamente, no una lista
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("ProjectViewModel", "Error al guardar el proyecto", e);
        }
    }


    // Método para verificar si un usuario ya existe basado en su DNI
    public boolean userExists(String nombreUsuario, String apellidoUsuario, String dni, ProjectModel existingUser) {
        ProjectModel allProjects = projectLiveData.getValue(); // Utilizar datos del LiveData cargados

        if (allProjects == null) return false;


        if (allProjects != null) {  // Verifica si el proyecto no es nulo
                boolean isDniMatch = allProjects.getDni().equals(dni);
                boolean isDifferentUser = existingUser == null || allProjects.getUsuarioId() != existingUser.getUsuarioId();

                if (isDniMatch && isDifferentUser) {
                    return true;
                }
            }

        return false;
    }

    public void setProjects(ProjectModel projects) {
        this.projectLiveData.setValue(projects); // Assuming you have a MutableLiveData<List<ProjectModel>> named projects
    }

}
