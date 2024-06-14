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
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duenyo_crear_campo);

        // Asignamos vistas a los objetos
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerOpciones = findViewById(R.id.spinnerOpciones);
        btnRegistrar2 = findViewById(R.id.btnCrearCampo);
        tvContador = findViewById(R.id.tvContador);

        // Añadimos las opciones al spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOpciones.setAdapter(adapter);

        // Instancias de Firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Escuchador para actualizar el contador de caracteres de la descripción
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

        // Escuchador para el botón registrar
        btnRegistrar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString().trim();
                String descripcion = etDescripcion.getText().toString().trim();

                // Verificamos que los campos no estén vacíos
                if (nombre.isEmpty() || descripcion.isEmpty()) {
                    Toast.makeText(FieldCreateActivity.this, getResources().getString(R.string.debesCompletarCampos), Toast.LENGTH_SHORT).show();
                } else {
                    // Registramos el campo
                    registrarCampo(nombre, descripcion);
                }
            }
        });
    }

    // Método para registrar el campo en firebase
    public void registrarCampo(String nombre, String descripcion) {
        String uid = mAuth.getCurrentUser().getUid(); // Obtenemos UID del usuario actual
        String tipo = spinnerOpciones.getSelectedItem().toString(); // Obtenemos el tipo seleccionado en el spinner
        DocumentReference docRef = mFirestore.collection("campos").document(uid); // Obtenemos el documento del campo

        // Creamos un mapa con los datos del campo
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombre);
        map.put("descripcion", descripcion);
        map.put("tipo", tipo);
        map.put("duenyo", uid);

        // Guardamos el campo en firebase
        docRef.set(map).addOnSuccessListener(aVoid -> {
            Toast.makeText(getApplicationContext(), "Campo registrado con éxito", Toast.LENGTH_SHORT).show();

            // Esperamos 1.5 segundos
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                // Redirigimos a la nueva actividad
                Intent intent = new Intent(getApplicationContext(), OwnerActivity.class);
                startActivity(intent);
                finish();
            }, 1500);
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Error al registrar el campo", Toast.LENGTH_SHORT).show();
        });
    }
}
