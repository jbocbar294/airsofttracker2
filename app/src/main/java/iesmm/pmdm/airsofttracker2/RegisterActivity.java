package iesmm.pmdm.airsofttracker2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etNombre, etEdad, etApellido, etEmail, etTelefono, etUsuario, etContrasenya;
    private CheckBox cbDuenyo;
    private Button btnRegistrar;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);

        // Asignar objetos
        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        etApellido = findViewById(R.id.etApellido);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etUsuario = findViewById(R.id.etUsuario);
        etContrasenya = findViewById(R.id.etContrasenya);
        cbDuenyo = findViewById(R.id.cbduenyo);
        btnRegistrar = findViewById(R.id.btnRegistrar2);

        btnRegistrar.setOnClickListener(this);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void comprobarUsuarioExistente(String usuario, String email, int telefono) { // find

        registrarUsuario(etNombre.getText().toString(), Integer.parseInt(etEdad.getText().toString()), etApellido.getText().toString(), email, telefono, usuario, etContrasenya.getText().toString());
        // Snackbar.make(findViewById(android.R.id.content), "Este usuario ya está registrado", Snackbar.LENGTH_LONG).show();

    }

    private void registrarUsuario(String nombre, int edad, String apellidos, String email, int telefono, String usuario, String contrasenya) { // insert
        String duenyo = cbDuenyo.isChecked() ? "SI" : "NO";

        mAuth.createUserWithEmailAndPassword(email, contrasenya).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();
                    DocumentReference docRef = mFirestore.collection("usuarios").document(uid);

                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", nombre);
                    map.put("edad", edad);
                    map.put("apellidos", apellidos);
                    map.put("email", email);
                    map.put("telefono", telefono);
                    map.put("usuario", usuario);
                    map.put("duenyo", duenyo);

                    docRef.set(map);
                    Toast.makeText(RegisterActivity.this, "Usuario creado con éxito", Toast.LENGTH_SHORT).show();

                    Intent intent  = new Intent(RegisterActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("usuario", usuario);
                    bundle.putString("uid", uid);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == btnRegistrar.getId()) {
            if (!etNombre.getText().toString().isEmpty()
                    && !etEdad.getText().toString().isEmpty()
                    && !etApellido.getText().toString().isEmpty()
                    && !etEmail.getText().toString().isEmpty()
                    && !etTelefono.getText().toString().isEmpty()
                    && !etUsuario.getText().toString().isEmpty()
                    && !etContrasenya.getText().toString().isEmpty()) {
                comprobarUsuarioExistente(etUsuario.getText().toString(), etEmail.getText().toString(), Integer.parseInt(etTelefono.getText().toString()));
            } else {
                Toast.makeText(RegisterActivity.this, "Debes completar todos los campos para continuar.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}