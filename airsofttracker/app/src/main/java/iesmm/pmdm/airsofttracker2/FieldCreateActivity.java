package iesmm.pmdm.airsofttracker2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FieldCreateActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion;
    private Spinner spinnerOpciones;
    private Button btnRegistrar2;
    private TextView tvContador;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duenyo_crear_campo);

        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerOpciones = findViewById(R.id.spinnerOpciones);
        btnRegistrar2 = findViewById(R.id.btnCrearCampo);
        tvContador = findViewById(R.id.tvContador);

        // Añade las opciones al spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOpciones.setAdapter(adapter);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etDescripcion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int longitud = s.length();
                tvContador.setText(longitud + "/150");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnRegistrar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString().trim();
                String descripcion = etDescripcion.getText().toString().trim();

                if (nombre.isEmpty() || descripcion.isEmpty()) {
                    Toast.makeText(FieldCreateActivity.this, getResources().getString(R.string.debesCompletarCampos), Toast.LENGTH_SHORT).show();
                } else {
                    registrarCampo(nombre, descripcion);
                }
            }
        });
    }

    public void registrarCampo(String nombre, String descripcion) {
        String uid = mAuth.getCurrentUser().getUid();
        String tipo = spinnerOpciones.getSelectedItem().toString();
        DocumentReference docRef = mFirestore.collection("campos").document(uid);

        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombre);
        map.put("descripcion", descripcion);
        map.put("tipo", tipo);
        map.put("duenyo", uid);
        docRef.set(map).addOnSuccessListener(aVoid -> {
            Toast.makeText(getApplicationContext(), "Campo registrado con éxito", Toast.LENGTH_SHORT).show();

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), OwnerActivity.class);
                startActivity(intent);
                finish();
            }, 1500);
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Error al registrar el campo", Toast.LENGTH_SHORT).show();
        });
    }
}
