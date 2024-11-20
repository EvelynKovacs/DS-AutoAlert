package com.example.autoalert.data;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.utils.FileUtils;
import com.example.autoalert.utils.NetworkUtils;
import com.example.autoalert.view.activities.BackgroundAccidentActivity;
import com.example.autoalert.view.activities.MenuInicioActivity;
import com.example.autoalert.view.activities.MessageSender;
import com.example.autoalert.view.activities.SistemaVotacion;
import com.example.autoalert.view.fragments.SimulacionFragment;
import com.example.autoalert.viewmodel.AccidentViewModel;
import com.example.autoalert.viewmodel.SpeedViewModel;
import com.example.autoalert.utils.NotificadorAccidente;

import java.util.HashMap;
import java.util.Set;

public class AccidentDetectionService extends LifecycleService {

    private boolean isLockScreenActivityShown = false;

    private static final String CHANNEL_ID = "AccidentDetectionServiceChannel";
    private SpeedViewModel speedViewModel;
    private AccidentViewModel accidentViewModel;
    private NotificadorAccidente notificador;

    private MessageSender messageSender;
    private FileUtils fileUtils;
    private boolean accidenteDetectado=false;
    private SimulacionFragment simulacionFragment;
    private int cont = 0;
    private SistemaVotacion sistemaVotacion;
    private MenuInicioActivity menuInicioActivity;
    private NetworkUtils networkUtils;


    private FragmentManager fragmentManager;

    private MutableLiveData<Boolean> accidenteDetectadoLiveData = new MutableLiveData<>(false);

    private final IBinder binder = new LocalBinder();


    @Override
    public void onCreate() {
        super.onCreate();

        // Crear canal de notificación
        createNotificationChannel();

        // Configurar la notificación del Foreground Service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AutoAlert")
                .setContentText("Monitoreando posibles accidentes...")
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        // Iniciar el servicio en primer plano
        startForeground(1, notification);

        // Inicializar utilidades y fragmentos
        simulacionFragment = new SimulacionFragment();
        fileUtils = new FileUtils(this);
        messageSender = new MessageSender();
        sistemaVotacion = new SistemaVotacion(this.menuInicioActivity);
        networkUtils = new NetworkUtils();

        // Inicializar ViewModels con el contexto de aplicación
        speedViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(SpeedViewModel.class);
        accidentViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(AccidentViewModel.class);

        // Verificar si accidentViewModel es nulo
        if (accidentViewModel == null) {
            Log.e("AccidentService", "accidentViewModel es null");
        } else {
            Log.d("AccidentService", "accidentViewModel inicializado correctamente");
        }

        // Pasar el ViewModel al NotificadorAccidente
        NotificadorAccidente notificador = NotificadorAccidente.getInstancia();
        notificador.setAccidentViewModel(accidentViewModel);


        // Observar cambios en el estado de accidentes con getApplication()
        accidentViewModel.getAccidenteDetectado().observe(this, accidentDetected -> {
            if (!accidenteDetectado) {
                fileUtils.saveStateInFile("SI");
                enviarMensaje();
            }
        });


        // Iniciar las actualizaciones de ubicación
        speedViewModel.resumeLocationUpdates();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Llama al método de la clase base
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AccidentService", "Servicio detenido.");
        // Cancelar la notificación del servicio en primer plano si es necesario
        stopForeground(true);
        // Detener la ubicación y otras tareas que estén corriendo
        //speedViewModel.pauseLocationUpdates();
    }


    private void enviarMensaje() {
        Log.d("AccidentService", "Enviando SMS de emergencia...");
        String message = fileUtils.readState();
        Set<String> ipListArchivo = fileUtils.leerListaIpsEnArchivo();
        Log.d("AccidentService", "ipListArchivo esta: "+ipListArchivo.isEmpty());
        if (!ipListArchivo.isEmpty()) {
            for (String targetIp : ipListArchivo) {
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de mensaje", "Mensaje enviado a: " + targetIp + " con " + message);
            }
            if (message.equals("SI")) {
                accidenteDetectado=true;
                accidenteDetectadoLiveData.setValue(accidenteDetectado);  // Notify activity
                enviarEstado();

            }
        } else {
            Log.e("Envio de mensaje", "HUBO ACCIDENTE pero No hay IPs disponibles para enviar el mensaje.");
            Toast.makeText(this, "HUBO ACCIDENTE pero No hay IPs disponibles para enviar el mensaje", Toast.LENGTH_SHORT).show();
            accidenteDetectado=true;
            //accidenteDetectadoLiveData.setValue(accidenteDetectado);  // Notify activity
            showLockScreenActivity();

        }


        // Restablecer accidenteDetectado a false para permitir futuras detecciones
        //accidenteDetectado = false;

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Accident Detection Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public AccidentDetectionService getService() {
            return AccidentDetectionService.this;
        }
    }

    public MutableLiveData<Boolean> getAccidenteDetectadoLiveData() {
        return accidenteDetectadoLiveData;
    }


    public void enviarEstado(){
        Set<String> listaIps = fileUtils.leerListaIpsEnArchivo();
        fileUtils.clearVotoFileContent();
        String estado = fileUtils.readState();
        String message;
        if(estado.equals("SI")){
            message = "VOTO:SI";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:SI");
        } else {
            message = "VOTO:NO";
            Log.i("Envio de Estado", "Enviando mensaje: VOTO:NO");

        }
        if(!listaIps.isEmpty()){
            for(String targetIp : listaIps) {
                messageSender.sendMessage(targetIp, message);
                Log.i("Envio de Estado", "Enviando mensaje a " + targetIp + " con: " + message);
            }
        }

        startVotacionTimer();
    }

    public void startVotacionTimer() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                HashMap<String, String> votos = fileUtils.readMapfromFile("map-ip-voto");
                String myOwnVote = fileUtils.readState();
                votos.put(networkUtils.getDeviceIpAddress(), myOwnVote);
                boolean veredicto = sistemaVotacion.iniciarConteo(votos);
                reiniciarContador();
                if(veredicto) {
                    accidenteDetectado=true;
                    Log.i("Votacion", "HAY ACCIDENTE");
                    setResultadoText("HAY ACCIDENTE");
                    showLockScreenActivity();
                } else {
                    Log.i("Votacion", "NO HAY ACCIDENTE");
                    setResultadoText("NO HAY ACCIDENTE");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Hilo contador", "Error al al contar votos" + e.getMessage());
            }
        }).start();
    }

    public void reiniciarContador(){
        cont = 0;
    }

    public void setResultadoText(String message){
        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        displayText.append(message).append("\n");
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Aquí puedes detener el servicio
        Log.d("AccidentService", "La aplicación fue cerrada, el servicio se detendrá.");
        stopSelf(); // Detener el servicio
    }

    private void showLockScreenActivity() {
        if (!isLockScreenActivityShown) {
            isLockScreenActivityShown = true;
            Intent intent = new Intent(this, BackgroundAccidentActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }



}
