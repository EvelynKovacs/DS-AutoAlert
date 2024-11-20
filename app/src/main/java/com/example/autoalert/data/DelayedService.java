package com.example.autoalert.data;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.example.autoalert.view.activities.MenuInicioActivity;

public class DelayedService extends Service {
    private Handler handler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Retrasa la ejecución 10 segundos
        handler.postDelayed(() -> {
            // Lanza MainActivity con un extra para mostrar el fragmento
            Intent activityIntent = new Intent(this, MenuInicioActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activityIntent.putExtra("show_fragment", true);
            startActivity(activityIntent);
        }, 10000); // 10 segundos en milisegundos

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

