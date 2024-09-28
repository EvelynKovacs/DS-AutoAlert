package com.example.autoalert.view.activities;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.autoalert.R;
import com.example.autoalert.view.fragments.DetalleUsuarioFragment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class DetalleUsuarioActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100; // Código único para identificar la solicitud de permisos
    private boolean isScreenshotCaptured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_usuario);

        // Verifica si los permisos ya están otorgados
        if (!hasPermissions()) {
            // Si no están otorgados, solicita los permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.SET_WALLPAPER},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Si los permisos ya están otorgados, captura la pantalla
            captureScreenshot();
        }

        if (savedInstanceState == null) {
            int usuarioId = getIntent().getIntExtra("userId", -1);
            DetalleUsuarioFragment detalleUsuarioFragment = new DetalleUsuarioFragment();
            Bundle args = new Bundle();
            args.putInt("userId", usuarioId);
            detalleUsuarioFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detalleUsuarioFragment)
                    .commit();
        }
    }

    // Verificar si los permisos ya están otorgados
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Verifica si la solicitud de permisos es la que esperábamos
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Verifica si los permisos fueron otorgados
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisos otorgados, captura la captura de pantalla
                captureScreenshot();
            } else {
                // Permisos no otorgados, muestra un mensaje al usuario
                Toast.makeText(this, "Permisos necesarios no otorgados", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si los permisos ya están otorgados, asegúrate de no resetear la bandera innecesariamente
        if (hasPermissions() && !isScreenshotCaptured) {
            captureScreenshot();
        }
    }




    public void captureScreenshot() {
        if (isScreenshotCaptured) return;

        View rootView = getWindow().getDecorView().getRootView();
        isScreenshotCaptured = true;
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                rootView.draw(canvas);

                saveScreenshotToGallery(bitmap);
            }
        });
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
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                // Guardar la imagen en la URI
                try (FileOutputStream outputStream = (FileOutputStream) getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    Toast.makeText(this, "Captura de pantalla guardada en la galería", Toast.LENGTH_SHORT).show();
                    setLockScreenWallpaper(uri); // Establecer como fondo de pantalla de bloqueo
                }
            } else {
                Toast.makeText(this, "Error al crear la entrada en la galería", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al guardar la captura: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setLockScreenWallpaper(Uri uri) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            // Establecer el wallpaper de bloqueo
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                Toast.makeText(this, "Fondo de pantalla de bloqueo establecido correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Esta función requiere API nivel 24 o superior", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al establecer el fondo de pantalla: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}

