package iesmm.pmdm.airsofttracker2;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OwnerFragmentFieldEdit extends Fragment {

    private EditText etNombre, etDescripcion;
    private Spinner spinnerOpciones;
    private Button btnGuardar;
    private TextView tvContador;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uid;

    public OwnerFragmentFieldEdit() {
    }

    public static OwnerFragmentFieldEdit newInstance() {
        return new OwnerFragmentFieldEdit();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Instancia de Firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid(); // Obtener el ID del usuario actual
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_duenyo_editar_campo, container, false);

        // Asignar objetos
        etNombre = view.findViewById(R.id.etNombre);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        spinnerOpciones = view.findViewById(R.id.spinnerOpciones);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        tvContador = view.findViewById(R.id.tvContador);

        // Configurar el Spinner con las opciones
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.opciones_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOpciones.setAdapter(adapter);

        // Escuchador para actualizar el contador de caracteres
        etDescripcion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvContador.setText(length + "/150");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Cargar datos existentes del campo
        camposActuales();

        // Configuración del botón guardar
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString().trim();
                String descripcion = etDescripcion.getText().toString().trim();
                String tipo = spinnerOpciones.getSelectedItem().toString();

                // Verificar que todos los campos estén completos
                if (nombre.isEmpty() || descripcion.isEmpty()) {
                    Toast.makeText(getContext(), "Debes completar todos los campos para continuar", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Guardar los datos en firebase
                DocumentReference docRef = mFirestore.collection("campos").document(uid);
                Map<String, Object> map = new HashMap<>();
                map.put("nombre", nombre);
                map.put("descripcion", descripcion);
                map.put("tipo", tipo);

                // Actualizar el documento en firebase
                docRef.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Campo actualizado con éxito", Toast.LENGTH_SHORT).show();
                        getFragmentManager().popBackStack(); // Volver al fragmento anterior
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error al actualizar el campo", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }

    // Método para cargar los datos existentes del campo
    private void camposActuales() {
        if (uid != null) {
            DocumentReference docRef = mFirestore.collection("campos").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Asignar datos a los textviews
                        String nombreCampo = document.getString("nombre");
                        String descripcionCampo = document.getString("descripcion");
                        etNombre.setText(nombreCampo);
                        etDescripcion.setText(descripcionCampo);
                    } else {
                        Toast.makeText(getContext(), "No se encontró el campo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener el documento", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
