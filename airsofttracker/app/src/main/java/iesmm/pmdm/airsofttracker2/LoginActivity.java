package iesmm.pmdm.airsofttracker2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin, btnRegistrar;
    private EditText etEmail, etContrasenya;
    private String email, contrasenya;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        // Asignar objetos a las vistas
        btnLogin = findViewById(R.id.btnCrearCampo);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnLogin.setOnClickListener(this);
        btnRegistrar.setOnClickListener(this);
        etEmail = findViewById(R.id.etUsuarioRegister);
        etContrasenya = findViewById(R.id.etContrasenya);

        // Instancias de Firebase y SharedPreferences
        sharedPreferences = getSharedPreferences("configuraciones", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == btnLogin.getId()) {
            email = etEmail.getText().toString();
            contrasenya = etContrasenya.getText().toString();
            if (email.isEmpty() || contrasenya.isEmpty()) {
                Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                iniciarSesion();
            }
        } else {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    }

    // Método para iniciar sesión
    private void iniciarSesion() {
        mostrarCargando();

        // Método de FirebaseAuth para iniciar sesión con correo y contraseña
        mAuth.signInWithEmailAndPassword(email, contrasenya).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();

                    obtenerUsuarioConEmail(uid, new FirestoreCallback() {
                        @Override
                        public void onCallback(String usuario) {
                            obtenerDuenyo(uid, new DuenyoCallback() {
                                @Override
                                public void onCallback(boolean esDuenyo) {
                                    ocultarCargando();
                                    if (usuario != null) {
                                        Intent intent;
                                        if (esDuenyo) {
                                            intent = new Intent(LoginActivity.this, OwnerActivity.class);
                                        } else {
                                            intent = new Intent(LoginActivity.this, PlayerActivity.class);
                                        }

                                        editor.putString("usuario", usuario);
                                        editor.commit();
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    ocultarCargando();
                    Toast.makeText(LoginActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Interfaz de callback para obtener el usuario
    public interface FirestoreCallback {
        void onCallback(String usuario);
    }

    // Método para obtener el usuario a partir del email
    private void obtenerUsuarioConEmail(String uid, final FirestoreCallback firestoreCallback) {
        DocumentReference docRef = mFirestore.collection("usuarios").document(uid);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(":::ERROR_CONSULTA_USUARIO", "Error al obtener el usuario a partir del email en login", error);
                    firestoreCallback.onCallback(null);
                    return;
                }
                if (value != null && value.exists()) {
                    String usuario = value.getString("usuario");
                    firestoreCallback.onCallback(usuario);
                } else {
                    firestoreCallback.onCallback(null);
                }
            }
        });
    }

    // Interfaz de callback para obtener el campo duenyo
    public interface DuenyoCallback {
        void onCallback(boolean esDuenyo);
    }

    private void obtenerDuenyo(String uid, final DuenyoCallback duenyoCallback) {
        DocumentReference docRef = mFirestore.collection("usuarios").document(uid);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(":::ERROR_CONSULTA_DUENYO", "Error al obtener el campo 'duenyo' del usuario", error);
                    duenyoCallback.onCallback(false);
                    return;
                }
                if (value != null && value.exists()) {
                    String duenyo = value.getString("duenyo");
                    duenyoCallback.onCallback(duenyo != null && duenyo.equals("SI"));
                } else {
                    duenyoCallback.onCallback(false);
                }
            }
        });
    }

    // Método para mostrar el ProgressDialog de carga
    private void mostrarCargando() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_cargando, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        progressDialog = builder.create();
        progressDialog.show();
    }

    // Método para ocultar el ProgressDialog de carga
    private void ocultarCargando() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
