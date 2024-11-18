package com.example.autoalert.view.fragments;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import android.os.Handler;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//
//import com.example.autoalert.R;
//import com.example.autoalert.view.activities.RedActivity;
//import com.github.anastr.speedviewlib.SpeedView;
//import com.google.android.material.button.MaterialButton;
//
//
//
//public class PrincipalFragment extends Fragment {
//
//    private SimulacionFragment simulacionFragment;
//
//    private ActivityResultLauncher<String[]> requestPermissionLauncher;
//
//    public PrincipalFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflar el layout del fragmento
//        View view = inflater.inflate(R.layout.fragment_principal, container, false);
//
//        // Inicializar el lanzador de permisos
//        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
//            Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
//            Boolean writeStorageGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            Boolean readStorageGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
//
//            // Si algún permiso no fue otorgado, mostrar un mensaje
//            if (fineLocationGranted != null && !fineLocationGranted ||
//                    writeStorageGranted != null && !writeStorageGranted ||
//                    readStorageGranted != null && !readStorageGranted) {
//                // Manejo de la negación de permisos
//            } else {
//                startSimulation();
//            }
//        });
//
//        // Botones de la interfaz
//        MaterialButton btnEnvioMensaje = view.findViewById(R.id.sendMessageButton);
//        ImageButton btnUsuario = view.findViewById(R.id.editProfileButton);
//        ImageButton btnRed = view.findViewById(R.id.createNetworkButton);
//
//        // Crear instancia del SimulacionFragment
//        simulacionFragment = new SimulacionFragment();
//
//        btnEnvioMensaje.setOnClickListener(v -> {
//            if (hasPermissions()) {
//                startSimulation();
//            } else {
//                requestPermissions();
//            }
//        });
//
//        btnUsuario.setOnClickListener(v -> {
//            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//            transaction.replace(R.id.fcv_main_container, new VerUsuarioFragment());
//            transaction.addToBackStack(null);
//            transaction.commit();
//        });
//
//        btnRed.setOnClickListener(v -> {
//            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//            transaction.replace(R.id.fcv_main_container, new ConexionFragment());
//            transaction.addToBackStack(null);
//            transaction.commit();
//        });
//
//        // Actualiza el velocímetro cada 2 segundos
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateSpeedometer(view, 50f); // Simula velocidad de 50 km/h
//                new Handler().postDelayed(this, 2000);
//            }
//        }, 2000);
//
//        return view;
//    }
//
//    private void startSimulation() {
//        // Iniciar transacción para cambiar al SimulacionFragment
//        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//        transaction.replace(R.id.fcv_main_container, simulacionFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }
//
//    private boolean hasPermissions() {
//        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
//    }
//
//    private void requestPermissions() {
//        requestPermissionLauncher.launch(new String[]{
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//        });
//    }
//
//    private void updateSpeedometer(View view, float speed) {
//        SpeedView speedView = view.findViewById(R.id.speedView);
//
//        if (speedView != null) {
//            speedView.setMaxSpeed(300);
//            speedView.speedTo(speed); // Solo una llamada a speedTo
//        }
//    }
//
//
//
//}

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.autoalert.R;
import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.view.activities.MenuInicioActivity;
import com.example.autoalert.view.fragments.ConexionFragment;
import com.example.autoalert.view.fragments.SimulacionFragment;
import com.example.autoalert.view.fragments.VerUsuarioFragment;
import com.example.autoalert.viewmodel.SpeedViewModel; // Asegúrate de que SpeedViewModel esté en el paquete correcto
import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class PrincipalFragment extends Fragment {

    private SimulacionFragment simulacionFragment;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private SpeedViewModel speedViewModel; // Agrega tu ViewModel de velocidad
    private SpeedView speedView;

    private MenuInicioActivity menuInicioActivity;


    public PrincipalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_principal, container, false);

        // Configura el SpeedView
        speedView = view.findViewById(R.id.speedView);
        speedView.setMaxSpeed(300);


        // Inicializa el SpeedViewModel
        speedViewModel = new ViewModelProvider(this).get(SpeedViewModel.class);
        speedViewModel.checkLocationPermissions();


        // Configura el Observer para la velocidad
        speedViewModel.getSpeed().observe(getViewLifecycleOwner(), speedKmh -> {
            if (speedKmh != null) {
                String location = String.valueOf(speedViewModel.getLocation().getValue()); // Obtén el valor de MutableLiveData
//                Log.d("SpeedObserver", "La ubicacion actual: " + location);

//                // Agregar el Toast para mostrar la velocidad
//                Toast.makeText(getContext(), "Velocidad actual: " + speedKmh + " km/h", Toast.LENGTH_SHORT).show();
//                Log.d("SpeedObserver", "Velocidad actual ACA: " + speedKmh);
                speedView.speedTo(speedKmh.floatValue());
            } else {
                Log.d("SpeedObserver", "Velocidad nula recibida.");
            }
        });

        // Inicializar el lanzador de permisos
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean writeStorageGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Boolean readStorageGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);

            // Si algún permiso no fue otorgado, mostrar un mensaje
            if (fineLocationGranted != null && !fineLocationGranted ||
                    writeStorageGranted != null && !writeStorageGranted ||
                    readStorageGranted != null && !readStorageGranted) {
                // Manejo de la negación de permisos
            } else {
                startSimulation();
            }
        });

        // Botones de la interfaz
        MaterialButton btnEnvioMensaje = view.findViewById(R.id.sendMessageButton);
        ImageButton btnUsuario = view.findViewById(R.id.editProfileButton);
        ImageButton btnRed = view.findViewById(R.id.createNetworkButton);
        ImageButton btnConectados = view.findViewById(R.id.viewUsersButton);

       MaterialButton btnFrontal = view.findViewById(R.id.botonFrontal);

        // Obtener referencia a bannerImage
        ImageView bannerImage = view.findViewById(R.id.bannerImage);

        // Crear instancia del SimulacionFragment
        simulacionFragment = new SimulacionFragment();

        btnEnvioMensaje.setOnClickListener(v -> {
            if (hasPermissions()) {
                startSimulation();
            } else {
                requestPermissions();
            }
        });

        btnUsuario.setOnClickListener(v -> {
            ProjectModel user = readJsonData(); // Carga los datos del usuario

            if (user != null) {
                // Crear un nuevo fragmento de edición
                AddProjectFragment addProjectFragment = new AddProjectFragment();

                // Serializar los datos de ProjectModel y pasarlos en el Bundle
                Bundle bundle = new Bundle();
                bundle.putParcelable("projectModel", user);
                addProjectFragment.setArguments(bundle);

                // Realizar la transición al fragmento de edición
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fcv_main_container, addProjectFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        btnRed.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new CreacionRedFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnConectados.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new UsuariosConectadosFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Configurar OnClickListener para bannerImage
        bannerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuInicioActivity activity = (MenuInicioActivity) getActivity();
                if (activity != null) {
                    activity.resetAccidenteDetectado();
                } else {
                    Log.e("PrincipalFragment", "MenuInicioActivity is null");
                }
            }
        });

        btnFrontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuInicioActivity activity = (MenuInicioActivity) getActivity();
                if (activity != null) {
                    activity.iniciarLecturaUbicaciones();
                } else {
                    Log.e("PrincipalFragment", "MenuInicioActivity is null");
                }
            }
        });


        return view;
    }

    private void startSimulation() {
        // Iniciar transacción para cambiar al SimulacionFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_main_container, simulacionFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private boolean hasPermissions() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        requestPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        });
    }

    // PARA EL USUARIO
    private ProjectModel readJsonData() {
        ProjectModel user = null;
        try {
            // Abre el archivo desde el almacenamiento interno
            FileInputStream fis = requireActivity().openFileInput("user_data.json");
            InputStreamReader isr = new InputStreamReader(fis);
            Gson gson = new Gson();

            // Deserializa el JSON a un objeto ProjectModel
            user = gson.fromJson(isr, ProjectModel.class);

            // Cierra el InputStreamReader
            isr.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }
}

