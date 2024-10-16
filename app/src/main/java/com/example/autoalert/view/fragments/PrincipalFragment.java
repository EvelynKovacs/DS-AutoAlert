package com.example.autoalert.view.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.autoalert.R;
import com.example.autoalert.view.activities.RedActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PrincipalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrincipalFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PrincipalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PrincipalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PrincipalFragment newInstance(String param1, String param2) {
        PrincipalFragment fragment = new PrincipalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_principal, container, false);

        // Obtener referencia del botón de sensores
        ImageButton btnSensores = view.findViewById(R.id.botonSensores);
        // Obtener referencia del botón de Simulacion
        ImageButton btnSimulacion = view.findViewById(R.id.botonSimulacion);
        // Obtener referencia del botón de Usuario
        ImageButton btnUsuario = view.findViewById(R.id.botonUsuario);
        // Obtener referencia del botón de Red
        ImageButton btnRed = view.findViewById(R.id.botonRedes);

        // Configurar el clic en el botón
        btnSensores.setOnClickListener(v -> {
            // Iniciar transacción para cambiar al SensoresFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new SensoresFragment()); // Cambia a SensoresFragment
            transaction.addToBackStack(null); // Agregar a la pila de retroceso
            transaction.commit(); // Asegúrate de llamar a commit()
        });

        // Configurar el clic en el botón
        btnSimulacion.setOnClickListener(v -> {
            // Iniciar transacción para cambiar al SimulacionFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new SimulacionFragment()); // Cambia a SensoresFragment
            transaction.addToBackStack(null); // Agregar a la pila de retroceso
            transaction.commit(); // Asegúrate de llamar a commit()
        });

        // Configurar el clic en el botón
        btnUsuario.setOnClickListener(v -> {
            // Iniciar transacción para cambiar al UsarioFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new VerUsuarioFragment()); // Cambia a SensoresFragment
            transaction.addToBackStack(null); // Agregar a la pila de retroceso
            transaction.commit(); // Asegúrate de llamar a commit()
        });

        // Configurar el clic en el botón
        btnRed.setOnClickListener(v -> {
            // Iniciar transacción para cambiar al RedActivity
            // Crear un intent para iniciar RedActivity
            Intent intent = new Intent(getActivity(), RedActivity.class);
            startActivity(intent); // Iniciar la actividad
        });


        return view;
    }
}