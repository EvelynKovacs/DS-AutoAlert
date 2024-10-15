package com.example.autoalert.view.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autoalert.R;

import com.example.autoalert.databinding.FragmentAddProjectBinding;
import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.view.adapters.ContactAdapter;
import com.example.autoalert.viewmodel.ProjectViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;


import de.hdodenhof.circleimageview.CircleImageView;

public class AddProjectFragment extends Fragment {

    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter; // Asegúrate de tener un adaptador para tus contactos

    // Imagen
    CircleImageView profileImg;
    private Uri selectedImageUri;

    // Para obtener los contactos
    private static final int REQUEST_READ_CONTACTS = 100;
    private static final int REQUEST_CONTACT = 1;
    private Set<String> contactNamesSet;
    private List<String> contactNamesList;
    //private TextView tvContactNames;

    // Para guardar y obtener los usuarios
    private FragmentAddProjectBinding binding;
    private ProjectViewModel projectViewModel;
    private ProjectModel projectModel;
    private boolean isEdit = false;


    MaterialButton btnAddProject;


    private final String[] grupoSanguineo = {"Seleccionar Grupo","A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-"};
    private String selectedGrupoSanguineo;
    Spinner spinnerGrupoSanguineo;

    // Variables para el TextInputEditText y el Calendar
    TextInputEditText edtFechaNacimiento;
    Calendar calendar;


    // Launchers for contacts and images
    private final ActivityResultLauncher<String> requestContactPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    selectContact();
                } else {
                    Toast.makeText(requireContext(), "Permiso para acceder a contactos denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        binding = FragmentAddProjectBinding.inflate(inflater, container, false);
        View view = binding.getRoot(); // This is the root view

        // Inicializa SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        boolean isFirstEntry = sharedPreferences.getBoolean("isFirstEntry", true);


        // Inicializa el RecyclerView
        recyclerViewContacts = view.findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(requireContext()));

        edtFechaNacimiento = view.findViewById(R.id.edtFechaNacimiento);
        calendar = Calendar.getInstance();

        // Inicializa la lista de contactos
        contactNamesList = new ArrayList<>();  // O carga los contactos desde tu base de datos

        // Inicializa el adaptador con el listener
        contactAdapter = new ContactAdapter(contactNamesList, position -> {
            if (position >= 0 && position < contactNamesList.size()) {
                // Remover el contacto de la lista y del set
                contactNamesSet.remove(contactNamesList.get(position).split(" - ")[0]);
                contactNamesList.remove(position);

                // Notificar al adapter del cambio
                contactAdapter.notifyItemRemoved(position);
            } else {
                Toast.makeText(requireContext(), "Índice fuera de límites", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewContacts.setAdapter(contactAdapter);
        contactAdapter.notifyDataSetChanged();

        // Referencia al botón
        btnAddProject = view.findViewById(R.id.btnAddProject);
        spinnerGrupoSanguineo = view.findViewById(R.id.edtGrupoSanguineo); // Initialize Spinner


        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);

        contactNamesSet = new HashSet<>();
        profileImg =view.findViewById(R.id.profile_img);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, grupoSanguineo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrupoSanguineo.setAdapter(adapter);

        initDropDown();

        // Recibir URI de la imagen si está disponible
        if (getActivity().getIntent().hasExtra("image_uri")) {
            String imageUriString = getActivity().getIntent().getStringExtra("image_uri");
            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                profileImg.setImageURI(imageUri);
            }
        }

        if (getArguments() != null && getArguments().containsKey("projectModel")) {
            projectModel = getArguments().getParcelable("projectModel"); // Recupera el objeto Parcelable

            if (projectModel != null) {
                // Llenar los campos con los datos de projectModel
                binding.edtNombreUsuario.setText(projectModel.getNombreUsuario());
                binding.edtApellidoUsuario.setText(projectModel.getApellidoUsuario());
                binding.edtDni.setText(projectModel.getDni());
                binding.edtFechaNacimiento.setText(projectModel.getFechaNacimiento());
                binding.edtDatosMedicos.setText(projectModel.getDatosMedicos());
                spinnerGrupoSanguineo.setSelection(Arrays.asList(grupoSanguineo).indexOf(projectModel.getGrupoSanguineo()));

                // Decodificar y mostrar la imagen si existe
                String base64Image = projectModel.getFoto();
                if (base64Image != null && !base64Image.isEmpty()) {
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    profileImg.setImageBitmap(bitmap);
                }

                // Cargar contactos
                contactNamesList.addAll(projectModel.getContactos());
                contactNamesSet.addAll(projectModel.getContactos());
                contactAdapter.notifyDataSetChanged();

                // Cambiar el botón a "Actualizar"
                btnAddProject.setText("Actualizar");
                isEdit = true;
            }
        } else {
            // Si no es edición, configurar el texto del botón como "Agregar"
            btnAddProject.setText("Finalizar");
            isEdit = false;
        }
        // Limitar la longitud del DNI a 9 caracteres (solo dígitos)
        binding.edtDni.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});

        // Configura la visibilidad del botón de eliminar
        binding.btnDeleteProject.setVisibility(isEdit ? View.VISIBLE : View.GONE);

//     ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        btnAddProject.setOnClickListener(view1 -> {
            if (validateProjectFields()){
                if (isFirstEntry){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isFirstEntry",false);
                }

                handleSaveProject();// Navegar al siguiente fragmento
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fcv_main_container, new PrincipalFragment())
                        .addToBackStack(null)
                        .commit();
            } else {
                // Mostrar mensaje o manejar el caso de campos no válidos
                Toast.makeText(requireContext(), "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            }

        });
        binding.btnDeleteProject.setOnClickListener(view1 -> showDeleteConfirmationDialog());
        binding.btnAddContact.setOnClickListener(view1 -> showContacts());
        // Configura la visibilidad del botón de eliminar
        binding.btnDeleteProject.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        binding.profileImg.setOnClickListener(view1 -> clickImage());
        edtFechaNacimiento.setOnClickListener(view1 -> showDatePickerDialog());

        return view; // Return the root view
    }

    private void showContacts() {
        if (requireContext().checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            selectContact();
        }
    }

    private void selectContact() {
        Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContact, REQUEST_CONTACT);
    }

    private void handleSaveProject() {
        // Realizar validaciones antes de guardar
        if (!validateProjectFields()) {
            return;
        }

        // Obtener los valores necesarios para crear o actualizar el proyecto
        String nombreUsuario = binding.edtNombreUsuario.getText().toString().trim();
        String apellidoUsuario = binding.edtApellidoUsuario.getText().toString().trim();
        String dni = binding.edtDni.getText().toString().trim();
        String fechaNacimientoStr = binding.edtFechaNacimiento.getText().toString().trim();
        String datosMedicos = binding.edtDatosMedicos.getText().toString().trim();
        String grupoSanguineo = spinnerGrupoSanguineo.getSelectedItem().toString();

        Uri imageUri = getImageUriFromProfileImg(); // Obtener URI actualizada
        String base64Image = encodeImageToBase64(imageUri); // Codificar la imagen en base64

        if (imageUri != null) {
            base64Image = encodeImageToBase64(imageUri);
        } else {
            base64Image = projectModel != null ? projectModel.getFoto() : null;
        }

        // Crear o actualizar el proyecto según el caso
        if (isEdit) {
            updateProject(nombreUsuario, apellidoUsuario, dni, fechaNacimientoStr, datosMedicos, grupoSanguineo, base64Image, contactNamesList);
        } else {
            createProject(nombreUsuario, apellidoUsuario, dni, fechaNacimientoStr, datosMedicos, grupoSanguineo, base64Image, contactNamesList);
        }
    }


    private boolean validateProjectFields() {
        boolean isValid = true;

        // Obtener valores de los campos
        String nombreUsuario = binding.edtNombreUsuario.getText().toString().trim();
        String apellidoUsuario = binding.edtApellidoUsuario.getText().toString().trim();
        String dni = binding.edtDni.getText().toString().trim();
        String edadStr = binding.edtFechaNacimiento.getText().toString().trim();
        String fechaNacimientoStr = binding.edtFechaNacimiento.getText().toString().trim();
        String grupoSanguineo = spinnerGrupoSanguineo.getSelectedItem().toString();

        // Validación de campos vacíos
        if (nombreUsuario.isEmpty() || apellidoUsuario.isEmpty() || edadStr.isEmpty() || dni.isEmpty() || grupoSanguineo.isEmpty() || contactNamesList.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validación de formato de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        Date fechaNacimiento;
        try {
            fechaNacimiento = sdf.parse(fechaNacimientoStr);
        } catch (ParseException e) {
            Toast.makeText(requireContext(), "Formato de fecha de nacimiento inválido", Toast.LENGTH_SHORT).show();
            binding.edtFechaNacimiento.setBackgroundResource(R.drawable.error_border);
            return false;
        }

        // Validar la edad
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaNacimiento);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int edad = calculateAge(year, month, day);

        if (edad < 10 || edad > 110) {
            Toast.makeText(requireContext(), "La edad debe estar entre 10 y 110 años", Toast.LENGTH_SHORT).show();
            binding.edtFechaNacimiento.setBackgroundResource(R.drawable.error_border);
            return false;
        }

        // Validación de nombre
        if (!isValidName(nombreUsuario)) {
            binding.edtNombreUsuario.setBackgroundResource(R.drawable.error_border);
            Toast.makeText(requireContext(), "El nombre no puede contener números o caracteres especiales", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validación de apellido
        if (!isValidName(apellidoUsuario)) {
            binding.edtApellidoUsuario.setBackgroundResource(R.drawable.error_border);
            Toast.makeText(requireContext(), "El apellido no puede contener números o caracteres especiales", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validación de DNI
        if (!dni.matches("\\d+")) {
            binding.edtDni.setBackgroundResource(R.drawable.error_border);
            Toast.makeText(requireContext(), "El DNI debe contener solo números", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dni.length() == 9 && Long.parseLong(dni) > 999999999) {
            Toast.makeText(requireContext(), "El DNI debe ser un número válido menor a 999999999", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validación de grupo sanguíneo
        if (grupoSanguineo.equals("Seleccionar Grupo")) {
            spinnerGrupoSanguineo.setBackgroundResource(R.drawable.error_border);
            Toast.makeText(requireContext(), "Por favor, seleccione un grupo sanguíneo", Toast.LENGTH_SHORT).show();
            return false;
        }

        return isValid;
    }


    private void updateProject(String nombreUsuario, String apellidoUsuario, String dni, String fechaNacimiento,
                               String datosMedicos, String grupoSanguineo, String base64Image, List<String> contactosList) {
        if (projectModel != null) {
            projectModel.setUsuarioId(1);
            projectModel.setNombreUsuario(nombreUsuario);
            projectModel.setApellidoUsuario(apellidoUsuario);
            projectModel.setDni(dni);
            projectModel.setFechaNacimiento(fechaNacimiento);
            projectModel.setDatosMedicos(datosMedicos);
            projectModel.setGrupoSanguineo(grupoSanguineo);
            projectModel.setFoto(base64Image); // Establecer la nueva imagen codificada
            projectModel.setContactos(contactosList);

            // Actualizar archivo JSON
            saveProjectToFile(projectModel);
            Toast.makeText(requireContext(), "Actualizado", Toast.LENGTH_SHORT).show();
            // Navegar al fragmento VerUsuarioFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fcv_main_container, new VerUsuarioFragment()) // Asegúrate de que el ID sea correcto
                    .addToBackStack(null) // Opcional, para permitir regresar al fragmento anterior
                    .commit();
        }
    }


    private void createProject(String nombreUsuario, String apellidoUsuario, String dni, String fechaNacimiento,
                               String datosMedicos, String grupoSanguineo, String base64Image, List<String> contactosList) {
        projectModel = new ProjectModel();
        projectModel.setUsuarioId(1);
        projectModel.setNombreUsuario(nombreUsuario);
        projectModel.setApellidoUsuario(apellidoUsuario);
        projectModel.setDni(dni);
        projectModel.setFechaNacimiento(fechaNacimiento);
        projectModel.setDatosMedicos(datosMedicos);
        projectModel.setGrupoSanguineo(grupoSanguineo);
        projectModel.setFoto(base64Image); // Set the encoded image
        projectModel.setContactos(contactosList);

        // Save to JSON file
        saveProjectToFile(projectModel);
        Toast.makeText(requireContext(), "Inserted", Toast.LENGTH_SHORT).show();
        // Navegar al fragmento VerUsuarioFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fcv_main_container, new VerUsuarioFragment()) // Asegúrate de que el ID sea correcto
                .addToBackStack(null) // Opcional, para permitir regresar al fragmento anterior
                .commit();
    }


    private void saveProjectToFile(ProjectModel project) {
        Gson gson = new Gson();
        String projectJson = gson.toJson(project); // Convertir el objeto en JSON
        try {
            // Guardar en el directorio interno de la app
            FileOutputStream fos = requireContext().openFileOutput("user_data.json", Context.MODE_PRIVATE);
            fos.write(projectJson.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al guardar datos", Toast.LENGTH_SHORT).show();
        }
    }


    private ProjectModel loadProjectFromFile() {
        Gson gson = new Gson();
        ProjectModel project = null;
        try {
            FileInputStream fis = requireContext().openFileInput("user_data.json");
            InputStreamReader isr = new InputStreamReader(fis);
            project = gson.fromJson(isr, ProjectModel.class); // Convertir JSON a objeto
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
        }
        return project;
    }


    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este usuario?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteProject())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteProject() {
        if (projectModel != null) {
            // Eliminar el contenido del archivo JSON
            clearProjectFile();

            Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();

            // Navegar al fragmento VerUsuarioFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fcv_main_container, new VerUsuarioFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void clearProjectFile() {
        try {
            // Sobrescribir el archivo JSON con un archivo vacío o null
            FileOutputStream fos = requireContext().openFileOutput("user_data.json", Context.MODE_PRIVATE);
            fos.write("{}".getBytes()); // Escribir un objeto vacío en el archivo JSON
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al limpiar los datos", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidName(String name) {
        return name != null && !Pattern.compile(".*\\d.*").matcher(name).find();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 33 && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                profileImg.setImageURI(selectedImageUri);
            }

            if (requestCode == REQUEST_CONTACT && data != null) {
                Uri contactUri = data.getData();

                String[] queryFields = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
                Cursor cursor = requireActivity().getContentResolver().query(contactUri, queryFields, null, null, null);

                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                        if (idIndex != -1 && nameIndex != -1) {
                            String contactId = cursor.getString(idIndex);
                            String name = cursor.getString(nameIndex);

                            if (contactNamesSet.contains(name)) {
                                Toast.makeText(requireContext(), "Este contacto ya ha sido seleccionado", Toast.LENGTH_SHORT).show();
                            } else {
                                String phoneNumber = getPhoneNumber(contactId);
                                String contactInfo = name + " - " + phoneNumber;

                                Log.d("Dentro de Activity","phoneNumber: "+phoneNumber);

                                    // Agregar el contacto a la lista y al set
                                contactNamesSet.add(name);
                                contactNamesList.add(contactInfo);

                                // Actualizar el RecyclerView
                                contactAdapter.notifyItemInserted(contactNamesList.size() - 1);

                                contactAdapter.notifyDataSetChanged(); // Llama a esto después de actualizar contactNamesList

                            }
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        // Manejo de selección de imagen
        if (requestCode == 33 && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            profileImg.setImageURI(selectedImageUri);
            Log.d("Codigo De Imagen","Imagen "+profileImg);
        }
    }

    private String getPhoneNumber(String contactId) {
        String phoneNumber = "";
        Cursor phoneCursor = requireActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );
        if (phoneCursor != null && phoneCursor.moveToFirst())
        {
            int numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            if (numberIndex != -1)
            {
                phoneNumber = phoneCursor.getString(numberIndex);
            }
            phoneCursor.close();
        }
        return phoneNumber;
    }

    // Solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectContact();
        } else {
            Toast.makeText(requireContext(), "Permiso para acceder a contactos denegado", Toast.LENGTH_SHORT).show();
        }
    }


    private void clickImage()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,33);
    }

    private String encodeImageToBase64(Uri imageUri) {
        if (imageUri == null) {
            // Handle the case where the URI is null (you could return a default value or an error)
            Log.e("AddProjectActivity", "Image URI is null");
            return null;
        }
        try {
            // Obtener el bitmap desde la URI
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Redimensionar el bitmap
            Bitmap resizedBitmap = resizeBitmap(bitmap, 200, 200);

            // Comprimir el bitmap y convertirlo a base64
            byte[] compressedImage = compressBitmapToBytes(resizedBitmap, Bitmap.CompressFormat.JPEG, 80);
            return Base64.encodeToString(compressedImage, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    private byte[] compressBitmapToBytes(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, outputStream);
        return outputStream.toByteArray();
    }

    private Uri getImageUriFromProfileImg() {
        if (selectedImageUri != null) {
            return saveImageToFile(selectedImageUri);
        }
        return null;
    }

    private Uri saveImageToFile(Uri imageUri) {
        File file = null;
        try {
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
            file = new File(requireContext().getCacheDir(), "profile_image.png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
            }

            fileOutputStream.close();
            inputStream.close();

            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void initDropDown() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, grupoSanguineo);

        binding.edtGrupoSanguineo.setAdapter(arrayAdapter);
        binding.edtGrupoSanguineo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGrupoSanguineo = grupoSanguineo[position];
                Log.d("GrupoSanguineo", "Grupo sanguíneo seleccionado: " + selectedGrupoSanguineo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No hacer nada si no se selecciona nada
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Actualizar el campo con la fecha seleccionada
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    edtFechaNacimiento.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);

                    // Calcular la edad
                    int age = calculateAge(selectedYear, selectedMonth, selectedDay);
                    // Aquí puedes usar la edad como prefieras
                    Log.d("Edad Calculada", "Edad: " + age);
                }, year, month, day);
        datePickerDialog.show();
    }

    private int calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        dob.set(year, month, day);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

}


