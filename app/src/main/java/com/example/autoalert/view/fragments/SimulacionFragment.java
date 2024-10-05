package com.example.autoalert.view.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.autoalert.R;
import com.example.autoalert.utils.SmsUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SimulacionFragment extends Fragment {
    MediaPlayer mp;
    CountDownTimer timer;
    Button play, stop, showMessage;
    ProgressBar progressBar;
    ArrayList<String> contactos;

    private static final int PERMISSION_REQUEST_CODE = 100; // Código de solicitud de permisos

    boolean isConfirmationPressed = false; // Variable de estado para rastrear si se presionó el botón de confirmación

    // Declare the launcher for permission requests
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_simulacion, container, false);

        // Initialize the launcher for permissions
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean writeStorageGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Boolean readStorageGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);

            // If permissions are null, default them to 'false' to avoid issues with older API levels
            if (fineLocationGranted == null) fineLocationGranted = false;
            if (writeStorageGranted == null) writeStorageGranted = false;
            if (readStorageGranted == null) readStorageGranted = false;

            if (fineLocationGranted && writeStorageGranted && readStorageGranted) {
                Toast.makeText(getContext(), "Permisos otorgados", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permisos necesarios no otorgados", Toast.LENGTH_SHORT).show();
            }
        });

        // Verifica si los permisos ya han sido concedidos
        if (!hasPermissions()) {
            requestPermissions();
        }


        play = root.findViewById(R.id.button2);
        stop = root.findViewById(R.id.button_stop);
        progressBar = root.findViewById(R.id.progress_circular);
        showMessage = root.findViewById(R.id.button_msg_accidente);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioMediaPlayer();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSoundAndTimer();
            }
        });

        showMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirmationPressed = true; // Marca que el botón de confirmación fue presionado
                showMessageDialog(v);
            }
        });

        return root;
    }

    public void AudioMediaPlayer() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }

        if (timer != null) {
            timer.cancel();
        }

        mp = MediaPlayer.create(getActivity(), R.raw.sound_long);
        mp.start();

        isConfirmationPressed = false; // Restablece el estado cada vez que se inicie el timer

        startTimer(30); // Inicia el temporizador de 30 segundos
    }

    private void startTimer(final int seconds) {
        progressBar.setMax(seconds);

        timer = new CountDownTimer(seconds * 1000, 500) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long secondsRemaining = leftTimeInMilliseconds / 1000;
                progressBar.setProgress((int) secondsRemaining);
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(0);
                if (mp != null && mp.isPlaying()) {
                    mp.stop();
                    mp.release();
                    mp = null;
                }

                // Si el botón de confirmación no fue presionado, muestra el mensaje de emergencia
                if (!isConfirmationPressed) {
                    showMessageDialog(showMessage); // Pasar el botón directamente
                }
            }
        }.start();
    }

    public void stopSoundAndTimer() {
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.release();
            mp = null;
        }

        if (timer != null) {
            timer.cancel();
        }

        progressBar.setProgress(0);
    }

    public void showMessageDialog(View view) {

        // Verifica y obtiene la ubicación actual
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String address = addresses.size() > 0 ? addresses.get(0).getAddressLine(0) : "Ubicación no disponible";

                    // Crear mensaje de emergencia con ubicación o dirección
                    String emergencyMessage = "Mensaje de emergencia DE PRUEBA NO ES VERDAD. "+"La persona: "+getDatos() +" Tuvo un accidente en la " +"Dirección aproximada: " + address;
                    Log.d("SimulacionFragment", "Mensaje: " + emergencyMessage);
                    //Log.d("SimulacionFragment", "Mensaje: " + contactos.toArray(new String[0]));
                    //String emergencyMessage = "Mensaje de emergencia DE PRUEBA NO ES VERDAD. "+"La persona: "+getDatos() +" Tuvo un accidente en la " +"Dirección aproximada: " + address;
                    // Enviar SMS de emergencia usando AppCompatActivity
                    AppCompatActivity activity = (AppCompatActivity) getActivity();
                    SmsUtils.checkAndSendSms(activity, contactos.toArray(new String[0]), emergencyMessage);


                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Mensaje")
                            .setMessage("Se envió el mensaje de emergencia.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Crear una nueva instancia del fragmento y pasar argumentos
                                    DetalleUsuarioFragment detalleUsuarioFragment = new DetalleUsuarioFragment();
                                    Bundle args = new Bundle();
                                    args.putInt("userId", 1); // Asegúrate de establecer el ID del usuario
                                    detalleUsuarioFragment.setArguments(args);

                                    // Reemplazar el fragmento actual con DetalleUsuarioFragment
                                    getActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fcv_main_container, detalleUsuarioFragment) // Reemplaza 'fragment_container' con el ID de tu contenedor de fragmentos
                                            .addToBackStack(null) // Añadir a la pila de retroceso si es necesario
                                            .commit();
                                }
                            })
                            .show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    // Método para obtener los datos del archivo JSON
    public String getDatos() {
        String datos = "";
        try {
            // Ruta del archivo JSON
            File file = new File(requireActivity().getFilesDir(), "user_data.json");

            if (file.exists()) {
                // Lee el archivo JSON
                FileReader fileReader = new FileReader(file);

                // Usa Gson para parsear el archivo JSON
                Gson gson = new Gson();
                JsonObject userData = gson.fromJson(fileReader, JsonObject.class);

                // Verifica si los campos existen y no son nulos antes de acceder a ellos
                String nombreUsuario = userData.has("nombreUsuario") && !userData.get("nombreUsuario").isJsonNull()
                        ? userData.get("nombreUsuario").getAsString() : "Nombre desconocido";

                String apellidoUsuario = userData.has("apellidoUsuario") && !userData.get("apellidoUsuario").isJsonNull()
                        ? userData.get("apellidoUsuario").getAsString() : "Apellido desconocido";

                // Accede al array de contactos correctamente
                if (userData.has("contactos") && !userData.get("contactos").isJsonNull()) {
                    // Convierte el array JSON a un ArrayList de Strings
                    contactos = new ArrayList<>();
                    userData.getAsJsonArray("contactos").forEach(contacto -> {
                        String contactoString = contacto.getAsString();
                        // Asume que los contactos están en formato "Nombre - Número"
                        int dashIndex = contactoString.lastIndexOf(" - ");
                        if (dashIndex != -1) {
                            // Extrae solo el número de teléfono después del guion
                            String numeroTelefono = contactoString.substring(dashIndex + 3).trim();
                            contactos.add(numeroTelefono);
                        }
                    });
                } else {
                    contactos = new ArrayList<>(); // Inicializa la lista vacía si no hay contactos
                }

                Log.d("SimulacionFragment", "Contactos: " + contactos);

                // Crea el alias con nombre y apellido
                datos = nombreUsuario + " " + apellidoUsuario;

                fileReader.close();
            } else {
                Log.e("MainActivity", "El archivo JSON no existe.");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error al leer el archivo JSON: " + e.getMessage());
        }

        return datos.trim(); // Devuelve los datos del usuario
    }

    // Method to request permissions
    private void requestPermissions() {
        requestPermissionLauncher.launch(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.release();
            mp = null;
        }

        if (timer != null) {
            timer.cancel();
        }
    }
}