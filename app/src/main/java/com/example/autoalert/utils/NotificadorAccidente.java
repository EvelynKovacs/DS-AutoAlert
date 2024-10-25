package com.example.autoalert.utils;

import com.example.autoalert.viewmodel.AccidentViewModel;

public class NotificadorAccidente {

    private static NotificadorAccidente instancia;
    private AccidentViewModel accidentViewModel;

    // Constructor privado para Singleton
    private NotificadorAccidente() {
    }

    // Obtener la única instancia de la clase (Singleton)
    public static synchronized NotificadorAccidente getInstancia() {
        if (instancia == null) {
            instancia = new NotificadorAccidente();
        }
        return instancia;
    }

    // Método para asignar el ViewModel
    public void setAccidentViewModel(AccidentViewModel viewModel) {
        this.accidentViewModel = viewModel;
    }

    // Método para notificar sobre el tipo de accidente
    public void notificarAccidente(String tipoAccidente) {
        System.out.println("Accidente detectado: " + tipoAccidente);

        // Notificar al ViewModel si existe
        if (accidentViewModel != null) {
            accidentViewModel.notificarAccidente();  // Actualizar el LiveData
        }
    }
}
