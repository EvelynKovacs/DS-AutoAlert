package com.example.autoalert.view.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
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

import org.json.JSONObject;

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

        // Inicializa los botones y la barra de progreso
        play = root.findViewById(R.id.button2);
        stop = root.findViewById(R.id.button_stop);
        progressBar = root.findViewById(R.id.progress_circular);
        showMessage = root.findViewById(R.id.button_msg_accidente);

        // Check if progressBar is null
        if (progressBar == null) {
            Log.e("SimulacionFragment", "ProgressBar is null");
        }

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
                enviarMensaje(v);
            }
        });

        showMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirmationPressed = true; // Marca que el botón de confirmación fue presionado
                Log.i("EnvioMensaje","Se presiono el botonEnviarMensaje");
                enviarMensaje(v);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Llama a AudioMediaPlayer al entrar a la pantalla
        AudioMediaPlayer();
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

        // ESTO ES EL SONIDOOOOO
        // Ajustar el volumen del dispositivo al máximo
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);int volumeToSet = (int) (maxVolume * 0.25); // Calculate 25% of max volume
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeToSet, 0);
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
                    enviarMensaje(showMessage); // Pasar el botón directamente
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

    public void enviarMensaje(View view) {
        Log.i("DentroEnviar", "Estoy en enviarMensaje");

        // Detenemos el temporizador y el sonido si están corriendo
        stopSoundAndTimer();

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Bandera para evitar manejar más de una ubicación
            final boolean[] ubicacionYaEnviada = {false};

            // Crea un LocationListener para recibir la ubicación actual
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Si ya hemos manejado una ubicación, no hacemos nada
                    if (ubicacionYaEnviada[0]) return;
                    // Una vez que se obtiene la ubicación, detenemos las actualizaciones
                    locationManager.removeUpdates(this);

                    // Manejar la ubicación obtenida
                    manejarUbicacion(location);
                    ubicacionYaEnviada[0] = true;  // Marcamos que ya se ha manejado la ubicación
                    Log.d("UbicacionActual", "Ubicación actual obtenida: " + location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            // Solicita actualizaciones de ubicación (solo GPS)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);

            // Establece un temporizador para obtener la última ubicación conocida si no se obtiene la actual en 10 segundos
            new Handler().postDelayed(() -> {
                // Si ya hemos manejado una ubicación, no hacemos nada
                Log.d("Handlerr", "el boolean tiene: "+ubicacionYaEnviada[0]);
                if (ubicacionYaEnviada[0]) return;
                Log.d("UbicacionActual", "Intentando obtener la última ubicación conocida...");
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // Intenta obtener la ubicación desde el archivo JSON
                Location ultimaUbicacionJson = obtenerUltimaUbicacion();
                if (ultimaUbicacionJson != null) {
                    manejarUbicacion(ultimaUbicacionJson);
                    ubicacionYaEnviada[0] = true;  // Marcamos que ya se ha manejado la ubicación
                    Log.d("UbicacionJSON", "Ubicación obtenida del archivo JSON: " + ultimaUbicacionJson);
                } else {
                    // Manejar el caso cuando no hay ubicación disponible en absoluto
                    Toast.makeText(getContext(), "No se pudo obtener la ubicación actual ni la del archivo JSON", Toast.LENGTH_SHORT).show();
                }
            }, 6000);
        } else {
            // Solicitar permisos si no han sido otorgados
            requestPermissions();
        }
    }

    private void manejarUbicacion(Location location) {
        if (location != null) {
            try {
                // Ensure the fragment is attached to an activity
                if (getActivity() == null) {
                    Log.e("SimulacionFragment", "Fragment is not attached to an activity.");
                    return;
                }

                Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                String address = addresses.size() > 0 ? addresses.get(0).getAddressLine(0) : "Ubicación no disponible";

                // Create emergency message with location or address
                String emergencyMessage = "Mensaje de emergencia DE PRUEBA NO ES VERDAD. " +
                        "La persona: " + getDatos() +
                        " tuvo un accidente en la dirección aproximada: " + address;
                Log.d("SimulacionFragment", "Mensaje: " + emergencyMessage);

                // Enviar SMS de emergencia usando AppCompatActivity
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                SmsUtils.checkAndSendSms(activity, contactos.toArray(new String[0]), emergencyMessage);

                // Navegar al DetalleUsuarioFragment
                DetalleUsuarioFragment detalleUsuarioFragment = new DetalleUsuarioFragment();
                Bundle args = new Bundle();
                args.putInt("userId", 1); // Ensure the user ID is set
                detalleUsuarioFragment.setArguments(args);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fcv_main_container, detalleUsuarioFragment)
                        .addToBackStack(null)
                        .commit();

            } catch (IOException e) {
                Log.e("SimulacionFragment", "Error al obtener la dirección de la ubicación", e);
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

    // Método para obtener la última ubicación desde el archivo JSON
    private Location obtenerUltimaUbicacion() {
        Location location = null;
        try {
            File file = new File(requireActivity().getFilesDir(), "ultima_ubicacion.json");

            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                char[] buffer = new char[(int) file.length()];
                fileReader.read(buffer);
                fileReader.close();

                // Parsear el contenido JSON
                String json = new String(buffer);
                JSONObject jsonObject = new JSONObject(json);
                double latitud = jsonObject.getDouble("latitud");
                double longitud = jsonObject.getDouble("longitud");

                // Crear una instancia de Location con los datos del JSON
                location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(latitud);
                location.setLongitude(longitud);
            } else {
                Log.e("SimulacionFragment", "El archivo ubicacion.json no existe.");
            }
        } catch (Exception e) {
            Log.e("SimulacionFragment", "Error al leer la última ubicación del archivo JSON.", e);
        }

        return location;
    }

    // Method to request permissions
    private void requestPermissions() {
        requestPermissionLauncher.launch(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS // Solicitar permiso para enviar SMS
        });
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED; // Verificar permiso para SMS
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