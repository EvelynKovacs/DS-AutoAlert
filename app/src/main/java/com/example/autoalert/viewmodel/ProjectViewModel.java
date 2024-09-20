package com.example.autoalert.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.repository.AppRepo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProjectViewModel extends AndroidViewModel {

    private AppRepo appRepo;

    public ProjectViewModel(@NonNull Application application) {
        super(application);

        appRepo = new AppRepo(application);
    }

    public void insertProject(ProjectModel projectModel) {
        appRepo.insertProject(projectModel);
    }

    public void updateProject(ProjectModel projectModel) {
        appRepo.updateProject(projectModel);
    }

    public void deleteProject(ProjectModel projectModel) {
        appRepo.deleteProject(projectModel);
    }

    public List<ProjectModel> getAllProjectFuture() throws ExecutionException, InterruptedException {
        return appRepo.getAllProjectFuture();
    }

    public LiveData<List<ProjectModel>> getAllProjectLive() {
        return appRepo.getAllProjectLive();
    }

    public boolean userExists(String nombreUsuario, String apellidoUsuario, String dni, ProjectModel existingUser) {
        try {
            List<ProjectModel> allProjects = appRepo.getAllProjectFuture();

            // Log todos los usuarios y sus DNI
            for (ProjectModel project : allProjects) {
                Log.d("UserExistsDebug", "Usuario: " + project.getNombreUsuario() +
                        ", Apellido: " + project.getApellidoUsuario() +
                        ", DNI: " + project.getDni() +
                        ", UsuarioID: " + project.getUsuarioId() +
                        ", Grupo Sanguineo: " + project.getGrupoSanguineo()+
                        ", Contactos: "+ project.getContactos());
            }

            Log.d("Actualizando Usuario", "Usuario: " + nombreUsuario +
                    ", Apellido: " + apellidoUsuario+
                    ", NUEVO DNI: " + dni);

            for (ProjectModel project : allProjects) {
                // Verificar si el DNI ya está registrado y no es el DNI del usuario actual
                boolean isDniMatch = project.getDni().equals(dni);
                boolean isDifferentUser = existingUser == null || project.getUsuarioId() != existingUser.getUsuarioId();

                if (isDniMatch && isDifferentUser) {
                    Log.d("Busca si ya existe", "Lo encuentra: DNI match = " + isDniMatch + ", Different user = " + isDifferentUser);
                    return true;
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }



}