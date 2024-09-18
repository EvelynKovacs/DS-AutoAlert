package com.example.autoalert.view.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.autoalert.R;
import com.example.autoalert.databinding.ActivityAddProjectBinding;
import com.example.autoalert.model.entities.ProjectModel;
import com.example.autoalert.viewmodel.ProjectViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddProjectActivity extends AppCompatActivity {

    // Imagen
    CircleImageView profileImg;
    private Uri selectedImageUri;

    // Para obtener los contactos
    private static final int REQUEST_READ_CONTACTS = 100;
    private static final int REQUEST_CONTACT = 1;
    private Set<String> contactNamesSet;
    private List<String> contactNamesList;
    private TextView tvContactNames;

    // Para guardar y obtener los usuarios
    private ActivityAddProjectBinding binding;
    private ProjectViewModel projectViewModel;
    private ProjectModel projectModel;
    private boolean isEdit = false;

    private final String[] grupoSanguineo = {"A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-"};
    private String selectedGrupoSanguineo;

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
        initDropDown();

        projectViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ProjectViewModel.class);

        contactNamesSet = new HashSet<>();
        contactNamesList = new ArrayList<>();

        tvContactNames = findViewById(R.id.edtContactNames);

        profileImg =findViewById(R.id.profile_img);

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
                binding.edtEdad.setText(String.valueOf(projectModel.getEdad()));
                binding.edtDatosMedicos.setText(projectModel.getDatosMedicos());
                binding.edtGrupoSanguineo.setText(projectModel.getGrupoSanguineo());

                // Obtener la lista de contactos del projectModel
                List<String> contactosList = projectModel.getContactos();
                // Unir los contactos en un solo String con saltos de línea
                String contactosString = TextUtils.join("\n", contactosList);
                // Establecer el texto en el EditText
                binding.edtContactNames.setText(contactosString);

                // Decodificar y mostrar la imagen si existe
                String base64Image = projectModel.getFoto();
                if (base64Image != null && !base64Image.isEmpty()) {
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    profileImg.setImageBitmap(bitmap);
                }


                isEdit = true;

            }
        }

        // Configura la visibilidad del botón de eliminar
        binding.btnDeleteProject.setVisibility(isEdit ? View.VISIBLE : View.GONE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnAddProject.setOnClickListener(view -> handleSaveProject());
        binding.btnDeleteProject.setOnClickListener(view -> showDeleteConfirmationDialog());
        binding.btnAddContact.setOnClickListener(view -> showContacts());
        binding.profileImg.setOnClickListener(view -> clickImage());
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
        Uri imageUri = getImageUriFromProfileImg(); // Obtener URI actualizada
        String base64Image = encodeImageToBase64(imageUri); // Codificar la imagen en base64

        if (imageUri != null) {
            base64Image = encodeImageToBase64(imageUri); // Codificar la imagen en base64
        } else {
            // Si no se selecciona una nueva imagen, mantener la imagen existente
            base64Image = projectModel != null ? projectModel.getFoto() : null;
        }


        // Obtener valores de los campos
        String nombreUsuario = binding.edtNombreUsuario.getText().toString().trim();
        String apellidoUsuario = binding.edtApellidoUsuario.getText().toString().trim();
        String dni = binding.edtDni.getText().toString().trim();
        String edadStr = binding.edtEdad.getText().toString().trim();
        String datosMedicos = binding.edtDatosMedicos.getText().toString().trim();
        String contactos = binding.edtContactNames.getText().toString();
        String grupoSanguineo = binding.edtGrupoSanguineo.getText().toString();



        Log.d("Sangre","Aca lo tiene "+grupoSanguineo);

        // Separar los contactos por salto de línea
        String[] contactosArray = contactos.split("\n");
        List<String> contactosList = Arrays.asList(contactosArray);

        if (nombreUsuario.isEmpty() || apellidoUsuario.isEmpty()  || edadStr.isEmpty() || dni.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadStr);
            if (edad < 0 || edad > 100) {
                Toast.makeText(this, "La edad debe ser un número entre 0 y 100", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Edad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidName(nombreUsuario)) {
            Toast.makeText(this, "El nombre no puede contener numeros o caracteres especiales", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidName(apellidoUsuario)) {
            Toast.makeText(this, "El apellido no puede contener numeros o caracteres especiales", Toast.LENGTH_SHORT).show();
            return;
        }



        if (!dni.matches("\\d+")) {
            Toast.makeText(this, "El DNI debe contener solo números", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean usuarioExiste = projectViewModel.userExists(nombreUsuario, apellidoUsuario, dni, projectModel);
        if (!isEdit && usuarioExiste) {
            Toast.makeText(this, "Este usuario ya está registrado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEdit) {

            updateProject(nombreUsuario, apellidoUsuario, dni, edad,datosMedicos,grupoSanguineo, base64Image, contactosList);

        } else {
            createProject(nombreUsuario, apellidoUsuario, dni, edad,datosMedicos,grupoSanguineo, base64Image, contactosList);
        }
    }

    private void updateProject(String nombreUsuario, String apellidoUsuario, String dni, int edad,String datosMedicos, String grupoSanguineo , String base64Image, List<String> contactosList) {
        if (projectModel != null) {
            projectModel.setNombreUsuario(nombreUsuario);
            projectModel.setApellidoUsuario(apellidoUsuario);
            projectModel.setDni(dni);
            projectModel.setEdad(edad);
            projectModel.setDatosMedicos(datosMedicos);

            projectModel.setGrupoSanguineo(grupoSanguineo); // Asegúrate de que esto se establezca correctamente

            projectModel.setFoto(base64Image); // Establecer la nueva imagen codificada
            projectModel.setContactos(contactosList);
            projectViewModel.updateProject(projectModel);
            Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void createProject(String nombreUsuario, String apellidoUsuario, String dni, int edad,String datosMedicos, String grupoSanguineo , String base64Image, List<String> contactosList) {
        projectModel = new ProjectModel();
        projectModel.setNombreUsuario(nombreUsuario);
        projectModel.setApellidoUsuario(apellidoUsuario);
        projectModel.setDni(dni);
        projectModel.setEdad(edad);
        projectModel.setDatosMedicos(datosMedicos);

        projectModel.setGrupoSanguineo(grupoSanguineo);

        projectModel.setFoto(base64Image); // Establecer la imagen codificada
        projectModel.setContactos(contactosList);
        projectViewModel.insertProject(projectModel);
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
            // Manejo de selección de contacto...
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
                            contactNamesSet.add(name);
                            contactNamesList.add(contactInfo);
                            updateContactNames();
                        }
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
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

    // Actualiza el TextView con los nombres y números de los contactos seleccionados
    private void updateContactNames()
    {
        StringBuilder namesBuilder = new StringBuilder();
        for (String contact : contactNamesList)
        {
            namesBuilder.append(contact).append("\n");
        }
        tvContactNames.setText(namesBuilder.toString());
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
            Toast.makeText(this, "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show();
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
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, grupoSanguineo);
        binding.edtGrupoSanguineo.setAdapter(arrayAdapter);
        binding.edtGrupoSanguineo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedGrupoSanguineo = (String) adapterView.getItemAtPosition(position); // Update member variable
                Log.d("GrupoSanguineo", "Grupo sanguíneo seleccionado: " + selectedGrupoSanguineo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No hacer nada si no se selecciona nada
            }
        });
    }
}
