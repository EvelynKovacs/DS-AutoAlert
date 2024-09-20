package com.example.autoalert.view.activities;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autoalert.R;
import com.example.autoalert.databinding.ActivityAddProjectBinding;
import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.view.adapters.ContactAdapter;
import com.example.autoalert.viewmodel.ProjectViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class AddProjectActivity extends AppCompatActivity {

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
    private ActivityAddProjectBinding binding;
    private ProjectViewModel projectViewModel;
    private ProjectModel projectModel;
    private boolean isEdit = false;


    MaterialButton btnAddProject;


    private final String[] grupoSanguineo = {"A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-"};
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
                    Toast.makeText(this, "Permiso para acceder a contactos denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializa el RecyclerView
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));

        edtFechaNacimiento = findViewById(R.id.edtFechaNacimiento);
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
                Toast.makeText(this, "Índice fuera de límites", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewContacts.setAdapter(contactAdapter);
        contactAdapter.notifyDataSetChanged();

        // Referencia al botón
        btnAddProject = findViewById(R.id.btnAddProject);
        spinnerGrupoSanguineo = findViewById(R.id.edtGrupoSanguineo); // Initialize Spinner


        projectViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ProjectViewModel.class);

        contactNamesSet = new HashSet<>();
        //tvContactNames = findViewById(R.id.edtContactNames);
        profileImg =findViewById(R.id.profile_img);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grupoSanguineo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrupoSanguineo.setAdapter(adapter);

        initDropDown();

        // Recibir URI de la imagen si está disponible
        if (getIntent().hasExtra("image_uri")) {
            String imageUriString = getIntent().getStringExtra("image_uri");
            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                profileImg.setImageURI(imageUri);
            }
        }

        if (getIntent().hasExtra("model")) {
            projectModel = getIntent().getParcelableExtra("model");
            if (projectModel != null) {
                binding.edtNombreUsuario.setText(projectModel.getNombreUsuario());
                binding.edtApellidoUsuario.setText(projectModel.getApellidoUsuario());
                binding.edtDni.setText(projectModel.getDni());
                //binding.edtEdad.setText(String.valueOf(projectModel.getEdad()));
                binding.edtFechaNacimiento.setText(projectModel.getFechaNacimiento());
                binding.edtDatosMedicos.setText(projectModel.getDatosMedicos());
                spinnerGrupoSanguineo.setSelection(Arrays.asList(grupoSanguineo).indexOf(projectModel.getGrupoSanguineo()));

                // Decodificar y mostrar la imagen si existe
                String base64Image = projectModel.getFoto();
                if (base64Image != null && !base64Image.isEmpty()) {
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    profileImg.setImageBitmap(bitmap);
                }

                // Obtener la lista de contactos del projectModel
                List<String> contactosList = projectModel.getContactos();
                // Verificar si la lista no está vacía antes de agregar
                if (contactosList != null && !contactosList.isEmpty()) {
                    contactNamesList.addAll(contactosList); // Agregar los contactos existentes a la lista
                    contactNamesSet.addAll(contactosList);  // Asegurarse de no duplicarlos
                } else {
                    Log.e("Contactos", "La lista de contactos está vacía. No se agregan elementos.");
                }
                // Cambiar el texto del botón a "Actualizar"
                btnAddProject.setText("Actualizar");
                isEdit = true;

            }
        }else {
            // Si no es edición, configurar el texto del botón como "Agregar"
            btnAddProject.setText("Agregar");
        }
        // Limitar la longitud del DNI a 9 caracteres (solo dígitos)
        binding.edtDni.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});

        // Configura la visibilidad del botón de eliminar
        binding.btnDeleteProject.setVisibility(isEdit ? View.VISIBLE : View.GONE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnAddProject.setOnClickListener(view -> handleSaveProject());
        binding.btnDeleteProject.setOnClickListener(view -> showDeleteConfirmationDialog());
        binding.btnAddContact.setOnClickListener(view -> showContacts());
        // Configura la visibilidad del botón de eliminar
        binding.btnDeleteProject.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        binding.profileImg.setOnClickListener(view -> clickImage());
        edtFechaNacimiento.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showContacts() {
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
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
        boolean isValid = true;


        // Obtener valores de los campos
        String nombreUsuario = binding.edtNombreUsuario.getText().toString().trim();
        String apellidoUsuario = binding.edtApellidoUsuario.getText().toString().trim();
        String dni = binding.edtDni.getText().toString().trim();
        String edadStr = binding.edtFechaNacimiento.getText().toString().trim();
        String datosMedicos = binding.edtDatosMedicos.getText().toString().trim();
        //String contactos = binding.edtContactNames.getText().toString();
        String fechaNacimientoStr = binding.edtFechaNacimiento.getText().toString().trim();
        String grupoSanguineo = spinnerGrupoSanguineo.getSelectedItem().toString();

/*
        // Separar los contactos por salto de línea
        String[] contactosArray = contactos.split("\n");
        List<String> contactosList = Arrays.asList(contactosArray);*/

       // Set<String> uniqueContactsSet = new HashSet<>(contactosList); // Elimina duplicados

        Uri imageUri = getImageUriFromProfileImg(); // Obtener URI actualizada
        String base64Image = encodeImageToBase64(imageUri); // Codificar la imagen en base64

        if (imageUri != null) {
            base64Image = encodeImageToBase64(imageUri); // Codificar la imagen en base64
        } else {
            // Si no se selecciona una nueva imagen, mantener la imagen existente
            base64Image = projectModel != null ? projectModel.getFoto() : null;
        }
/*
        if (uniqueContactsSet.size() != contactosList.size()) {
            Toast.makeText(this, "No se permiten contactos duplicados", Toast.LENGTH_SHORT).show();
            return;
        }*/

        if (nombreUsuario.isEmpty() || apellidoUsuario.isEmpty()  || edadStr.isEmpty() || dni.isEmpty() || grupoSanguineo.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato de fecha de nacimiento
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);  // Deshabilitar la tolerancia a fechas inválidas
        Date fechaNacimiento;
        try {
            fechaNacimiento = sdf.parse(fechaNacimientoStr); // Convertir String a Date
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha de nacimiento inválido", Toast.LENGTH_SHORT).show();
            binding.edtFechaNacimiento.setBackgroundResource(R.drawable.error_border);
            return;
        }

        // Calcular la edad a partir de la fecha
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaNacimiento);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int edad = calculateAge(year, month, day);

        // Validar que la edad esté entre 10 y 110 años
        if (edad < 10 || edad > 110) {
            Toast.makeText(this, "La edad debe estar entre 10 y 110 años", Toast.LENGTH_SHORT).show();
            binding.edtFechaNacimiento.setBackgroundResource(R.drawable.error_border);
            return;
        } else {
            binding.edtFechaNacimiento.setBackgroundResource(R.drawable.normal_border);
        }



        if (!isValidName(nombreUsuario)) {
            isValid = false;
            binding.edtNombreUsuario.setBackgroundResource(R.drawable.error_border);
            Toast.makeText(this, "El nombre no puede contener numeros o caracteres especiales", Toast.LENGTH_SHORT).show();
            return;
        } else {
            binding.edtNombreUsuario.setBackgroundResource(R.drawable.normal_border);
            }

        if (!isValidName(apellidoUsuario)) {
            binding.edtApellidoUsuario.setBackgroundResource(R.drawable.error_border);
            Toast.makeText(this, "El apellido no puede contener numeros o caracteres especiales", Toast.LENGTH_SHORT).show();
            return;
            } else {
            binding.edtApellidoUsuario.setBackgroundResource(R.drawable.normal_border);
            }



        if (!dni.matches("\\d+")) {
            isValid = false;
            binding.edtDni.setBackgroundResource(R.drawable.error_border);
            Toast.makeText(this, "El DNI debe contener solo números", Toast.LENGTH_SHORT).show();
            return;
            }else {
            binding.edtDni.setBackgroundResource(R.drawable.normal_border);
        }

        if (dni.length() == 9 && Long.parseLong(dni) > 999999999) {
            Toast.makeText(this, "El DNI debe ser un número válido menor a 999999999", Toast.LENGTH_SHORT).show();
            return;
        }



        boolean usuarioExiste = projectViewModel.userExists(nombreUsuario, apellidoUsuario, dni, projectModel);
        if (!isEdit && usuarioExiste) {
            Toast.makeText(this, "Este usuario ya está registrado con ese DNI", Toast.LENGTH_SHORT).show();
            binding.edtDni.setBackgroundResource(R.drawable.error_border);
            return;
        }else {
            binding.edtDni.setBackgroundResource(R.drawable.normal_border);
        }

        // Validación del grupo sanguíneo
        if (grupoSanguineo.isEmpty()) {
            isValid = false;
            spinnerGrupoSanguineo.setBackgroundResource(R.drawable.error_border);
            Toast.makeText(this, "No selecciono grupo sanguineo", Toast.LENGTH_SHORT).show();
            return;
        } else {
            spinnerGrupoSanguineo.setBackgroundResource(R.drawable.normal_border);
        }

        if (isEdit) {
            btnAddProject.setText("Actualizar");
            updateProject(nombreUsuario, apellidoUsuario, dni, fechaNacimientoStr,datosMedicos,grupoSanguineo, base64Image, contactNamesList);
        } else {
            btnAddProject.setText("Agregar");
            createProject(nombreUsuario, apellidoUsuario, dni, fechaNacimientoStr,datosMedicos,grupoSanguineo, base64Image, contactNamesList);
        }
    }

    private void updateProject(String nombreUsuario, String apellidoUsuario, String dni, String fechaNacimiento,String datosMedicos, String grupoSanguineo , String base64Image, List<String> contactosList) {
        if (projectModel != null) {
            projectModel.setNombreUsuario(nombreUsuario);
            projectModel.setApellidoUsuario(apellidoUsuario);
            projectModel.setDni(dni);
            projectModel.setFechaNacimiento(fechaNacimiento);
            projectModel.setDatosMedicos(datosMedicos);

            // LIMITE DNI, EDAD MAX 110, QUE SALTE UNA NOTIFICACION CUANDO LA PERSONA, PONE UNA EDAD IMPOSIBLE, EL GRUPO SANGUINEO QUE SEA OBLIGATORIO, PONER * A LADO DE LAS COSAS OBLIGATORIAS
            // PODER ELIMINAR CONTACTOS, QUE NO PERMITA ESCRIBIR EN EL GRUPO SANGUINEO, SACAR EL OJO DE LA PRIMERA PANTALLA.
            projectModel.setGrupoSanguineo(grupoSanguineo); // Asegúrate de que esto se establezca correctamente

            projectModel.setFoto(base64Image); // Establecer la nueva imagen codificada
            projectModel.setContactos(contactosList);
            projectViewModel.updateProject(projectModel);
            Log.d("ContactoActualizado", "Despues de Actualizar " + contactosList);
            Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void createProject(String nombreUsuario, String apellidoUsuario, String dni, String fechaNacimiento,String datosMedicos, String grupoSanguineo , String base64Image, List<String> contactosList) {
        projectModel = new ProjectModel();
        projectModel.setNombreUsuario(nombreUsuario);
        projectModel.setApellidoUsuario(apellidoUsuario);
        projectModel.setDni(dni);
        projectModel.setFechaNacimiento(fechaNacimiento);
        projectModel.setDatosMedicos(datosMedicos);

        projectModel.setGrupoSanguineo(grupoSanguineo);

        projectModel.setFoto(base64Image); // Establecer la imagen codificada
        projectModel.setContactos(contactosList);
        projectViewModel.insertProject(projectModel);
        Log.d("ContactoInsertado", "Despues de Insertar " + contactosList);
        Toast.makeText(this, "Insertado", Toast.LENGTH_SHORT).show();
        finish();
    }


    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este usuario?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteProject())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteProject() {
        if (projectModel != null) {
            projectViewModel.deleteProject(projectModel);
            Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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
                Cursor cursor = getContentResolver().query(contactUri, queryFields, null, null, null);

                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                        if (idIndex != -1 && nameIndex != -1) {
                            String contactId = cursor.getString(idIndex);
                            String name = cursor.getString(nameIndex);

                            if (contactNamesSet.contains(name)) {
                                Toast.makeText(this, "Este contacto ya ha sido seleccionado", Toast.LENGTH_SHORT).show();
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
        Cursor phoneCursor = getContentResolver().query(
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
            Toast.makeText(this, "Permiso para acceder a contactos denegado", Toast.LENGTH_SHORT).show();
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
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
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
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            file = new File(getCacheDir(), "profile_image.png");
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
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grupoSanguineo);
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
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
