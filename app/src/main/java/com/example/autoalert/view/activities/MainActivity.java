package com.example.autoalert.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.example.autoalert.R;
import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.utils.OnClickItemInterface;
import com.example.autoalert.view.adapters.ProjectAdapter;
import com.example.autoalert.viewmodel.ProjectViewModel;
import com.example.autoalert.databinding.ActivityMainBinding;
import com.google.gson.Gson;


import java.io.FileInputStream;
import java.io.IOException;

import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements OnClickItemInterface {

    private ActivityMainBinding binding;
    private ProjectViewModel projectViewModel;
    private ProjectAdapter adapter;

    private TextView txtPName;
    private TextView txtDNI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa las vistas
        txtPName = findViewById(R.id.txtPName);
        txtDNI = findViewById(R.id.txtDNI); // Asegúrate de que el ID sea correcto en el XML

        // Llama al método para leer los datos del JSON
        updateUserData();

        findViewById(R.id.addProject).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddProjectActivity.class));
        });

        findViewById(R.id.buttonDetalleUsuario).setOnClickListener(v -> {
            int usuarioId = 1; // Este es solo un ejemplo
            Intent intent = new Intent(MainActivity.this, DetalleUsuarioActivity.class);
            intent.putExtra("userId", usuarioId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualiza los datos cada vez que el Activity se reanuda
        updateUserData();
    }

    private void updateUserData() {
        ProjectModel user = readJsonData();

        // Si el usuario no es null, muestra su nombre y DNI
        if (user != null) {
            txtPName.setText(user.getNombreUsuario());
            txtDNI.setText(user.getDni());
        } else {
            // Opcional: manejar el caso en que no hay datos de usuario
            txtPName.setText("Sin nombre");
            txtDNI.setText("Sin DNI");
        }
    }

    private ProjectModel readJsonData() {
        ProjectModel user = null;
        try {
            // Abre el archivo desde el almacenamiento interno
            FileInputStream fis = openFileInput("user_data.json");
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

    public void onEditClick(View view) {
        ProjectModel user = readJsonData(); // Carga los datos del usuario

        if (user != null) {
            onClickItem(user, true); // Llama al método onClickItem para editar
        }
    }

    @Override
    public void onClickItem(ProjectModel projectModel, boolean isEdit) {
        if (isEdit) {
            Intent intent = new Intent(MainActivity.this, AddProjectActivity.class);
            intent.putExtra("model", projectModel); // Asegúrate de que estás pasando el modelo correcto
            startActivity(intent);
        } else {
            projectViewModel.deleteProject(projectModel);
        }
    }

}
