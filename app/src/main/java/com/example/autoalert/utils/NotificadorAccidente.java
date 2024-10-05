package com.example.autoalert.utils;

public class NotificadorAccidente {

    private static NotificadorAccidente instancia;

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

    // Método para notificar sobre el tipo de accidente
    public void notificarAccidente(String tipoAccidente) {
        // Aquí podrías enviar una notificación, llamar a un servicio, etc.
        System.out.println("Accidente detectado: " + tipoAccidente);
        // Enviar notificaciones, manejar alertas, llamadas, etc.
    }
}
