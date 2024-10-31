package com.example.autoalert.view.fragments;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.autoalert.R;
import com.example.autoalert.view.activities.MenuInicioActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsuariosConectadosFragment extends Fragment {

    private ArrayList<String> aliasList;
    private MenuInicioActivity mainActivity;
    private ArrayAdapter<String> adapter;
    private Handler handler;
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 3000; // Intervalo de 5 segundos

    public UsuariosConectadosFragment() {
        // Required empty public constructor
    }

    public static UsuariosConectadosFragment newInstance() {
        return new UsuariosConectadosFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MenuInicioActivity) requireActivity();
        aliasList = new ArrayList<>();
        cargarAliasConectados(); // Método que filtra y carga solo alias conectados
    }

    private void cargarAliasConectados() {
        aliasList.clear();  // Limpia la lista actual para evitar duplicados
        HashMap<String, String> aliasMap = mainActivity.readMapFromFile("map-ip-alias");
        HashMap<String, String> statusMap = mainActivity.readMapFromFile("map-ip-message"); // Estado de conexión

        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            String ip = entry.getKey();
            String alias = entry.getValue();

            // Verifica si la IP está "conectada" en map-ip-message antes de agregar el alias
            if ("conectado".equalsIgnoreCase(statusMap.get(ip))) {
                aliasList.add(alias);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios_conectados, container, false);

        ListView listView = view.findViewById(R.id.listView_aliases);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, aliasList);
        listView.setAdapter(adapter);

        // Configuración del Handler para refrescar la lista periódicamente
        handler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                actualizarListaUsuarios();
                handler.postDelayed(this, REFRESH_INTERVAL); // Vuelve a ejecutar después del intervalo
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshRunnable); // Inicia el refresco periódico
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable); // Detiene el refresco cuando el fragmento no está visible
    }

    // Método que permite refrescar la lista manualmente o en un evento específico
    public void actualizarListaUsuarios() {
        cargarAliasConectados();
        adapter.notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
    }
}
