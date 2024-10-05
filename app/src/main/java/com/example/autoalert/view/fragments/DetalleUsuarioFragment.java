package com.example.autoalert.view.fragments;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.viewmodel.UsuarioViewModel;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetalleUsuarioFragment extends Fragment {
    private TextView tvNombreUsuario, tvApellidoUsuario, tvDni, tvFechaNacimiento, tvGrupoSanguineo, tvDatosMedicos;
    private CircleImageView imgProfile;
    private UsuarioViewModel usuarioViewModel;
    private View view;

    private static final int PERMISSION_REQUEST_CODE = 100; // Código único para identificar la solicitud de permisos
    private boolean isScreenshotCaptured = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        view = inflater.inflate(R.layout.fragment_detalle_usuario, container, false);

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

        Bundle args = getArguments();
        if (args != null) {
            int usuarioId = args.getInt("userId", -1);
            Log.d("DetalleUsuarioFragment", "Usuario ID recibido: " + usuarioId);

            ProjectModel usuario = usuarioViewModel.getUsuarioById(usuarioId);

            if (usuario != null) {
                mostrarDatos(usuario);
            } else {
                Log.d("DetalleUsuarioFragment", "No se encontró un usuario con ID: " + usuarioId);
            }
        } else {
            Log.e("DetalleUsuarioFragment", "No se recibieron argumentos");
        }

        // Verifica si los permisos ya están otorgados y captura la pantalla
        if (!hasPermissions()) {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        } else {
            captureScreenshot();
        }
    }



    private void mostrarDatos(ProjectModel usuario) {
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
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    private void captureScreenshot() {
        if (isScreenshotCaptured) return;

        View rootView = getView();
        if (rootView != null) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove the listener based on the API level
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }


                    // Now the view is laid out, get the dimensions
                    int width = rootView.getWidth();
                    int height = rootView.getHeight();

                    // Check if dimensions are valid
                    if (width > 0 && height > 0) {
                        isScreenshotCaptured = true;

                        // Capture the screenshot
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        rootView.draw(canvas);

                        // Save the screenshot
                        saveScreenshotToGallery(bitmap);
                    } else {
                        Log.e("Screenshot", "View dimensions are still invalid after layout");
                        // Handle the error appropriately (e.g., show a message)
                    }
                }
            });
        }
    }

    private void saveScreenshotToGallery(Bitmap bitmap) {
        String imageName = "screenshot_" + Calendar.getInstance().getTimeInMillis() + ".jpg";

        // Crear un ContentValues para insertar la imagen en MediaStore
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        try {
            // Insertar en MediaStore y obtener la URI
            Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                // Guardar la imagen en la URI
                try (FileOutputStream outputStream = (FileOutputStream) requireContext().getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    Toast.makeText(getContext(), "Captura de pantalla guardada en la galería", Toast.LENGTH_SHORT).show();
                    setLockScreenWallpaper(uri); // Establecer como fondo de pantalla de bloqueo
                }
            } else {
                Toast.makeText(getContext(), "Error al crear la entrada en la galería", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al guardar la captura: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setLockScreenWallpaper(Uri uri) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(requireContext());
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);

            // Establecer el wallpaper de bloqueo
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                Toast.makeText(getContext(), "Fondo de pantalla de bloqueo establecido correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Esta función requiere API nivel 24 o superior", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al establecer el fondo de pantalla: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}