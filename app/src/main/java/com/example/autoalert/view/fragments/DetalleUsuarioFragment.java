package com.example.autoalert.view.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.view.activities.DetalleUsuarioActivity;
import com.example.autoalert.viewmodel.UsuarioViewModel;


import de.hdodenhof.circleimageview.CircleImageView;

public class DetalleUsuarioFragment extends Fragment {
    private TextView tvNombreUsuario, tvApellidoUsuario, tvDni, tvFechaNacimiento, tvGrupoSanguineo, tvDatosMedicos;
    private CircleImageView imgProfile;
    private UsuarioViewModel usuarioViewModel; // Asegúrate de tener este ViewModel


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_detalle_usuario, container, false);

        // Enlazar las vistas
        imgProfile = view.findViewById(R.id.imgProfile);
        tvNombreUsuario = view.findViewById(R.id.tvNombreUsuario);
        tvApellidoUsuario = view.findViewById(R.id.tvApellidoUsuario);
        tvDni = view.findViewById(R.id.tvDni);
        tvFechaNacimiento = view.findViewById(R.id.tvFechaNacimiento);
        tvGrupoSanguineo = view.findViewById(R.id.tvGrupoSanguineo);
        tvDatosMedicos = view.findViewById(R.id.tvDatosMedicos);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usuarioViewModel = new ViewModelProvider(this).get(UsuarioViewModel.class);

        int usuarioId =  getArguments().getInt("userId", -1); // Asegúrate de que el ID se pase correctamente

        Log.d("DetalleUsuarioFragment", "Usuario ID recibido ACAAA PAPAPAAA: " + usuarioId);

        ProjectModel usuario = usuarioViewModel.getUsuarioById(usuarioId);

        if (usuario != null) {
            tvNombreUsuario.setText("Nombre: " + usuario.getNombreUsuario());
            tvApellidoUsuario.setText("Apellido: " + usuario.getApellidoUsuario());
            tvDni.setText("DNI: " + usuario.getDni());
            tvFechaNacimiento.setText("Fecha de Nacimiento: " + usuario.getFechaNacimiento());
            tvGrupoSanguineo.setText("Grupo Sanguíneo: " + usuario.getGrupoSanguineo());

            if (!usuario.getDatosMedicos().isEmpty()) {
                tvDatosMedicos.setVisibility(View.VISIBLE);
                tvDatosMedicos.setText("Datos Médicos: " + usuario.getDatosMedicos());
            }

            // Decodificación de imagen si existe
            if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
                byte[] imageBytes = Base64.decode(usuario.getFoto(), Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imgProfile.setImageBitmap(decodedImage);
            }

            // Registro de datos para verificación
            Log.d("DetalleUsuarioFragment", "Datos mostrados: " + usuario.getNombreUsuario() + ", " + usuario.getApellidoUsuario());
        } else {
            Log.d("DetalleUsuarioFragment", "No se encontró un usuario con ID: " + usuarioId);
        }
    }

}