package iesmm.pmdm.airsofttracker2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin, btnRegistrar;
    private EditText etUsuario, etContrasenya;
    private String usuario, contrasenya;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        btnLogin = findViewById(R.id.btnRegistrar2);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnLogin.setOnClickListener(this);
        btnRegistrar.setOnClickListener(this);
        etUsuario = findViewById(R.id.etUsuario);
        etContrasenya = findViewById(R.id.etContrasenya);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == btnLogin.getId()) {
            usuario = etUsuario.getText().toString();
            contrasenya = etContrasenya.getText().toString();
            if (usuario.equals("") || contrasenya.equals("")) {
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
        mAuth.signInWithEmailAndPassword(usuario, contrasenya).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("usuario", usuario);
                    bundle.putString("uid", uid);
                    // Llamar siguiente actividad
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}