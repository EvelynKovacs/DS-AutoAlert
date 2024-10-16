package com.example.autoalert.view.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.autoalert.R;
import com.example.autoalert.view.activities.MenuInicioActivity;
import com.google.android.material.button.MaterialButton;

public class PasosASeguirFragment extends Fragment {


    public interface OnCompleteListener {
        void onComplete();
    }

    private OnCompleteListener listener;

    public void setOnCompleteListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    public PasosASeguirFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_pasos_a_seguir, container, false);

        // Obtener referencia del botón de usuario
        TextView btnContinue = view.findViewById(R.id.btnContinue);


        // Configurar el clic en el botón
        btnContinue.setOnClickListener(v -> {
            // Iniciar transacción para cambiar al Usuario
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new AddProjectFragment());
            transaction.addToBackStack(null);
            transaction.commit();

            // Marcar que ya no es la primera vez que ingresa
            ((MenuInicioActivity) getActivity()).markFirstTimeCompleted();

            if (listener != null) {
                listener.onComplete();
            }
        });

        return view;
    }
}

