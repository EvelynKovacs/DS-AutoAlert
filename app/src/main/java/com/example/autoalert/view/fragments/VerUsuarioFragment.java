package com.example.autoalert.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.autoalert.R;
import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.utils.OnClickItemInterface;
import com.example.autoalert.viewmodel.ProjectViewModel;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class VerUsuarioFragment extends Fragment implements OnClickItemInterface {

    private TextView txtPName;
    private TextView txtDNI;
    private ProjectViewModel projectViewModel;

    public VerUsuarioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ver_usuario, container, false);


        // Configurar el Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Ver Usuario");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        // Agregar un botón de retroceso en el Toolbar
        toolbar.setNavigationIcon(R.drawable.arrow_back_48px); // Reemplaza con tu icono de retroceso
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambiar el fragmento actual por el fragmento principal
                Fragment principalFragment = new PrincipalFragment(); // Cambia MainFragment por el nombre de tu fragmento principal
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fcv_main_container, principalFragment) // Asegúrate de que este ID coincida con el contenedor de tu fragmento
                        .addToBackStack(null) // Si deseas permitir volver a este fragmento más tarde
                        .commit();
            }
        });

        // Inicializa las vistas
        txtPName = view.findViewById(R.id.txtPName);
        txtDNI = view.findViewById(R.id.txtDNI);

        // Llama al método para leer los datos del JSON
        updateUserData();

        // Configura los listeners
        view.findViewById(R.id.addProject).setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, new AddProjectFragment()); // Asegúrate de usar el ID correcto para el contenedor de fragmentos
            transaction.addToBackStack(null);
            transaction.commit();
        });

        ImageView imgEdit = view.findViewById(R.id.imgEdit);
        imgEdit.setOnClickListener(this::onEditClick);

        return view;
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

    public void onEditClick(View view) {
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
    }


    @Override
    public void onClickItem(ProjectModel projectModel, boolean isEdit) {
        if (isEdit) {
            AddProjectFragment addProjectFragment = new AddProjectFragment();

            // Crear un bundle y pasar el ProjectModel como Parcelable
            Bundle bundle = new Bundle();
            bundle.putParcelable("projectModel", projectModel);
            addProjectFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fcv_main_container, addProjectFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            projectViewModel.deleteProject(projectModel);
        }
    }

}
