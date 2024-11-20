package com.example.autoalert.view.fragments;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.autoalert.R;

public class MyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla el diseño del fragmento
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        // Configura un botón en el fragmento
        Button button = view.findViewById(R.id.myButton);
        button.setOnClickListener(v ->
                Toast.makeText(getActivity(), "¡Botón presionado!", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}

