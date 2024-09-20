package com.example.autoalert.model.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "usuario")
public class ProjectModel implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int usuarioId;

    @ColumnInfo(name = "p_title")
    private String nombreUsuario;
    private String apellidoUsuario;

    @ColumnInfo(name = "contactos")  // La lista de contactos se almacenará como un String
    private List<String> contactos;

    private String dni;
    //private int edad;
    private String fechaNacimiento;
    private String datosMedicos;
    public String grupoSanguineo;
    private String foto;

    // Constructor vacío
    public ProjectModel() {}

    // Constructor para Parcel
    protected ProjectModel(Parcel in) {
        usuarioId = in.readInt();
        nombreUsuario = in.readString();
        apellidoUsuario = in.readString();
        contactos = new ArrayList<>();
        Log.d("ProjectModel", "Contactos asignados: " + contactos.size());
        in.readStringList(contactos);

        // Elimina entradas vacías de la lista de contactos
        contactos.removeIf(contacto -> contacto == null || contacto.isEmpty());

        Log.d("ProjectModel", "Contactos asignados después de eliminar vacíos: " + contactos.size());
        dni = in.readString();
        //edad = in.readInt();
        fechaNacimiento = in.readString();
        datosMedicos = in.readString();
        grupoSanguineo = in.readString();
        foto = in.readString();  // Para leer el array de bytes correctamente
    }

    // Getters y Setters
    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }

    public List<String> getContactos() {
        return contactos;
    }

    public void setContactos(List<String> contactos) {
        this.contactos = contactos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
/*
    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }*/

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDatosMedicos() {
        return datosMedicos;
    }

    public void setDatosMedicos(String datosMedicos) {
        this.datosMedicos = datosMedicos;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getGrupoSanguineo() {
        return grupoSanguineo;
    }

    public void setGrupoSanguineo(String grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    // Métodos de Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(usuarioId);
        dest.writeString(nombreUsuario);
        dest.writeString(apellidoUsuario);
        dest.writeStringList(contactos);  // Escribe la lista de contactos correctamente
        dest.writeString(dni);
        //dest.writeInt(edad);
        dest.writeString(fechaNacimiento);
        dest.writeString(datosMedicos);
        dest.writeString(grupoSanguineo);
        dest.writeString(foto);  // Para escribir el array de bytes correctamente
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProjectModel> CREATOR = new Creator<ProjectModel>() {
        @Override
        public ProjectModel createFromParcel(Parcel in) {
            return new ProjectModel(in);
        }

        @Override
        public ProjectModel[] newArray(int size) {
            return new ProjectModel[size];
        }
    };


}