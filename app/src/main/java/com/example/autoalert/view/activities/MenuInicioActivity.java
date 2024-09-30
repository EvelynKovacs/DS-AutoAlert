package com.example.autoalert.view.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.autoalert.R;
import com.example.autoalert.view.fragments.PrincipalFragment;


public class MenuInicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Transacción para añadir el fragmento
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setReorderingAllowed(true); // Permite la reordenación de fragmentos
            transaction.add(R.id.fcv_main_container, new PrincipalFragment()); // Agrega el fragmento al contenedor
            transaction.commit(); // Confirma la transacción
        }
    }
}
