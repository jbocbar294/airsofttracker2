package iesmm.pmdm.airsofttracker2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etEdad, etApellido, etEmail, etTelefono, etUsuario, etContrasenya;
    private CheckBox cbDuenyo;
    private Button btnRegistrar;
    private ImageButton btnAtras;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private boolean existente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);

        // Asignar objetos
        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        etApellido = findViewById(R.id.etApellidosRegister);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etUsuario = findViewById(R.id.etUsuarioRegister);
        etContrasenya = findViewById(R.id.etContrasenya);
        cbDuenyo = findViewById(R.id.cbduenyo);
        btnRegistrar = findViewById(R.id.btnCrearCampo);
        btnAtras = findViewById(R.id.btnAtras);

        existente = false;

        // Escuchador del botón registrar
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verificar que todos los campos estén completos
                if (!etNombre.getText().toString().isEmpty()
                        && !etEdad.getText().toString().isEmpty()
                        && !etApellido.getText().toString().isEmpty()
                        && !etEmail.getText().toString().isEmpty()
                        && !etTelefono.getText().toString().isEmpty()
                        && !etUsuario.getText().toString().isEmpty()
                        && !etContrasenya.getText().toString().isEmpty()) {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!existente) {
                                registrarUsuario(etNombre.getText().toString(), Integer.parseInt(etEdad.getText().toString()), etApellido.getText().toString(), etEmail.getText().toString(), etTelefono.getText().toString(), etUsuario.getText().toString(), etContrasenya.getText().toString());
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.cuentaYaExiste), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 700); // Espera 700 ms
                } else {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.debesCompletarCampos), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Escuchador del botón atrás
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Instancia de Firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // Método para registrar un nuevo usuario
    private void registrarUsuario(String nombre, int edad, String apellidos, String email, String telefono, String usuario, String contrasenya) {
        String duenyo = cbDuenyo.isChecked() ? "SI" : "NO";

        // Crear un nuevo usuario con FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, contrasenya).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();

                    DocumentReference docRef = mFirestore.collection("usuarios").document(uid); // Obtenemos la coleccion de usuarios

                    // Map para almacenar los datos del usuario
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", nombre);
                    map.put("edad", edad);
                    map.put("apellidos", apellidos);
                    map.put("email", email);
                    map.put("telefono", telefono);
                    map.put("usuario", usuario);
                    map.put("duenyo", duenyo);

                    // Si el usuario no es dueño, inicializamos un campo de partidas ganadas y listas vacías para campos seguidos y partidas
                    if (duenyo.equals("NO")) {
                        map.put("camposSeguidos", new ArrayList<String>());
                        map.put("partidas", new ArrayList<String>());
                        map.put("partidasGanadas", 0);
                    }

                    // Guardar los datos del usuario en Firebase
                    docRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.usuarioCreadoConExito), Toast.LENGTH_SHORT).show();

                                // Redirigir a la actividad correspondiente según el tipo de usuario
                                Intent intent = null;
                                if (duenyo.equals("SI")) {
                                    intent = new Intent(RegisterActivity.this, FieldCreateActivity.class);
                                } else {
                                    intent = new Intent(RegisterActivity.this, PlayerActivity.class);
                                }

                                // Guardar el nombre de usuario en SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("configuraciones", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("usuario", usuario);
                                editor.apply();

                                // Iniciamos la actividad
                                startActivity(intent);
                                finish(); // Finalizamos la actual
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.errorRegistrarUsuario), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.errorRegistrarUsuario), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
