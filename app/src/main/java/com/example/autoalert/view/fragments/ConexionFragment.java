package com.example.autoalert.view.fragments;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.autoalert.R;
import com.example.autoalert.view.activities.CreacionRedActivity;
import com.example.autoalert.utils.NetworkUtils;
import com.example.autoalert.view.activities.WifiHotspot;
import com.example.autoalert.view.activities.MenuInicioActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConexionFragment extends Fragment {

    private TextView ipTextView;
    private TextView myIpTextView;
    public Set<String> ipList = new HashSet<>();
    private TextView ipMessageTextView;
    private Button btnYes;
    private Button btnNo;
    private TextView responseTextView;
    private Button btnCreacionRed;
    private TextView resultadoTextView;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private NetworkUtils networkUtils;
    MenuInicioActivity mainActivity;

    private WifiHotspot hotspotManager;
    private Handler handler;
    private Runnable runnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mainActivity = (MenuInicioActivity) requireActivity();

        Button btnSendMessages = rootView.findViewById(R.id.btnSendMessages);
        ipTextView = rootView.findViewById(R.id.ipTextView);
        Button btnSendBroadcast = rootView.findViewById(R.id.btnSendBroadcast);
        ipMessageTextView = rootView.findViewById(R.id.ipMessageTextView);
        myIpTextView = rootView.findViewById(R.id.myIpTextView);
        btnYes = rootView.findViewById(R.id.btnYes);
        btnNo = rootView.findViewById(R.id.btnNo);
        responseTextView = rootView.findViewById(R.id.responseTextView);
        btnCreacionRed = rootView.findViewById(R.id.creacionRedbutton);
        resultadoTextView = rootView.findViewById(R.id.resultadoTextView);

        networkUtils = new NetworkUtils();

        btnSendBroadcast.setOnClickListener(view -> {
            this.mainActivity.sendBroadcast();
        });

        btnYes.setOnClickListener(view -> {
            setStatusTextViewOnYes();
        });

        btnNo.setOnClickListener(view -> {
            setStatusTextViewOnNo();
        });

        btnCreacionRed.setOnClickListener(view -> {
            irACrecionRed(view);
        });

        btnSendMessages.setOnClickListener(view -> {
            this.mainActivity.enviarMensaje();
        });

        String deviceIpAddress = networkUtils.getDeviceIpAddress();
        ipTextView.setText("Lista de IPs" + deviceIpAddress);

        String myDeviceIpAddress = networkUtils.getDeviceIpAddress();
        myIpTextView.setText("Mi IP: " + myDeviceIpAddress);


        // Instancia el Handler
        handler = new Handler();

        // Crea una tarea que se repite cada 5 segundos
        runnable = new Runnable() {
            @Override
            public void run() {
                // Llama a la función que actualiza los TextView
                actualizarTextViews();

                // Vuelve a ejecutar la tarea después de 5 segundos
                handler.postDelayed(this, 5000);
            }
        };

        // Comienza la tarea
        handler.postDelayed(runnable, 5000);


        return rootView;

    }

    private void actualizarTextViews() {
        HashMap<String,String> ipMessage = mainActivity.readMapFromFile("map-ip-message");
        HashMap<String,String> ipAlias = mainActivity.readMapFromFile("map-ip-alias");

        StringBuilder displayText = new StringBuilder("Mensajes recibidos:\n");
        for (Map.Entry<String, String> ip : ipMessage.entrySet()) {
            displayText.append("ALIAS: ").append(ipAlias.get(ip.getKey())).append(" - Mensaje: ").append(ip.getValue()).append("\n");
        }
        ipMessageTextView.setText(displayText.toString());
        ipList = mainActivity.leerListaIpsEnArchivo();
        StringBuilder ips = new StringBuilder("IPs recibidas:\n");
        for (String savedIp : ipList) {
            ips.append(savedIp).append("\n");
        }
        ipTextView.setText(ips.toString());
    }

    public void irACrecionRed(View view){
        mainActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CreacionRedFragment()).
                addToBackStack(null)
                .commit();
    }

    public void setStatusTextViewOnYes() {
        mainActivity.saveStateInFile("SI");
        responseTextView.setText("SI");
    }

    public void setStatusTextViewOnNo() {
        mainActivity.saveStateInFile("NO");
        responseTextView.setText("NO");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        hotspotManager.stopHotspot();
//        CreacionRedActivity creacionRedActivity = new CreacionRedActivity();
//        creacionRedActivity.stopWifiDirectHotspot();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            connectivityManager.unregisterNetworkCallback(networkCallback);
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        handler.post(runnable);
    }
}

