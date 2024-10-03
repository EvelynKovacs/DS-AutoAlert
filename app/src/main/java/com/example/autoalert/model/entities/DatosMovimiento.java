package com.example.autoalert.model.entities;

public class  DatosMovimiento {
    double latitud;
    double longitud;
    double velocidad;  // en metros/segundo
    long tiempo;       // en milisegundos

    public DatosMovimiento(double latitud, double longitud, double velocidad, long tiempo) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.velocidad = velocidad;
        this.tiempo = tiempo;
    }

    public double getLatitud(){
        return this.latitud;
    }


    // Setter para latitud
    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    // Getter para longitud
    public double getLongitud() {
        return longitud;
    }

    // Setter para longitud
    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    // Getter para velocidad
    public double getVelocidad() {
        return velocidad;
    }

    // Setter para velocidad
    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
    }

    // Getter para tiempo
    public long getTiempo() {
        return tiempo;
    }

    // Setter para tiempo
    public void setTiempo(long tiempo) {
        this.tiempo = tiempo;
    }

}
