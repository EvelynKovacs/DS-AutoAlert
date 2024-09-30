package com.example.autoalert.view.fragments;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.autoalert.R;


public class SimulacionFragment extends Fragment {
    MediaPlayer mp;
    CountDownTimer timer;
    Button play, stop, showMessage;
    ProgressBar progressBar;
    boolean isConfirmationPressed = false; // Variable de estado para rastrear si se presionó el botón de confirmación

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_simulacion, container, false);

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