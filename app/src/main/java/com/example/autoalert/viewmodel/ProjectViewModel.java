package com.example.autoalert.viewmodel;

import android.app.Application;
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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProjectViewModel extends AndroidViewModel {
    private static final String FILE_NAME = "user_data.json";
    private MutableLiveData<List<ProjectModel>> projectLiveData;

    public ProjectViewModel(Application application) {
        super(application);
        projectLiveData = new MutableLiveData<>();
        loadProjectsFromJson(); // Cargar proyectos al iniciar el ViewModel
    }


    public void addProject(ProjectModel newProject) {
        List<ProjectModel> currentProjects = projectLiveData.getValue();
        if (currentProjects != null) {
            currentProjects.add(newProject); // Agregar nuevo proyecto
            projectLiveData.setValue(currentProjects); // Notificar a LiveData
            saveProjectsToJson(currentProjects); // Guardar en JSON
        }
    }


    // Método para cargar los proyectos desde el archivo JSON
    private void loadProjectsFromJson() {
        try {
            FileInputStream fis = getApplication().openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            Gson gson = new Gson();

            // First, attempt to parse the JSON as a list
            Type projectListType = new TypeToken<List<ProjectModel>>(){}.getType();
            List<ProjectModel> projectList;

            try {
                JsonElement jsonElement = JsonParser.parseReader(isr);

                if (jsonElement.isJsonArray()) {
                    // Parse as a list
                    projectList = gson.fromJson(jsonElement, projectListType);
                } else if (jsonElement.isJsonObject()) {
                    // Handle a single object, wrap it in a list
                    ProjectModel singleProject = gson.fromJson(jsonElement, ProjectModel.class);
                    projectList = new ArrayList<>();
                    projectList.add(singleProject);
                } else {
                    projectList = new ArrayList<>(); // Default to empty list
                }

                if (projectList == null) {
                    projectList = new ArrayList<>(); // Default to empty list if null
                }
            } catch (JsonSyntaxException e) {
                Log.e("ProjectViewModel", "JSON parsing error", e);
                projectList = new ArrayList<>(); // Return empty list on error
            }

            fis.close();
            projectLiveData.setValue(projectList);
        } catch (Exception e) {
            Log.e("ProjectViewModel", "Error loading projects", e);
            projectLiveData.setValue(new ArrayList<>()); // Return empty list on error
        }
    }



    // Método para obtener todos los proyectos como LiveData
    public LiveData<List<ProjectModel>> getAllProjectLive() {
        return projectLiveData;
    }

    // Método para eliminar un proyecto y actualizar el archivo JSON
    public void deleteProject(ProjectModel project) {
        List<ProjectModel> currentProjects = projectLiveData.getValue();
        if (currentProjects != null) {
            currentProjects.remove(project);
            projectLiveData.setValue(currentProjects);
            saveProjectsToJson(currentProjects);
        }
    }

    // Método para guardar los proyectos en el archivo JSON
    private void saveProjectsToJson(List<ProjectModel> projects) {
        try {
            FileOutputStream fos = getApplication().openFileOutput(FILE_NAME, getApplication().MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Gson gson = new Gson();
            String json = gson.toJson(projects);
            osw.write(json);
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para verificar si un usuario ya existe basado en su DNI
    public boolean userExists(String nombreUsuario, String apellidoUsuario, String dni, ProjectModel existingUser) {
        List<ProjectModel> allProjects = projectLiveData.getValue(); // Utilizar datos del LiveData cargados

        if (allProjects == null) return false;

        for (ProjectModel project : allProjects) {
            if (project != null) {  // Verifica si el proyecto no es nulo
                boolean isDniMatch = project.getDni().equals(dni);
                boolean isDifferentUser = existingUser == null || project.getUsuarioId() != existingUser.getUsuarioId();

                if (isDniMatch && isDifferentUser) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setProjects(List<ProjectModel> projects) {
        this.projectLiveData.setValue(projects); // Assuming you have a MutableLiveData<List<ProjectModel>> named projects
    }

}
