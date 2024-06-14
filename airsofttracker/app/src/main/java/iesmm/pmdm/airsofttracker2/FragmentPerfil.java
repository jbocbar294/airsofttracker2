package iesmm.pmdm.airsofttracker2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FragmentPerfil extends Fragment {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uid;
    private final String TAG = ":::ERROR_FRAGMENT_PERFIL";
    private EditText etNombre, etEdad, etApellidos, etTelefono, etUsuario;
    private Button btnGuardar, btnCerrarSesion;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public FragmentPerfil() {
    }

    public static FragmentPerfil newInstance() {
        return new FragmentPerfil();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        etNombre = view.findViewById(R.id.etNombre);
        etEdad = view.findViewById(R.id.etEdad);
        etApellidos = view.findViewById(R.id.etApellidos);
        etTelefono = view.findViewById(R.id.etTelefono);
        etUsuario = view.findViewById(R.id.etUsuario);
        btnGuardar = view.findViewById(R.id.btnGuardarPerfil);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        sharedPreferences = getActivity().getSharedPreferences("configuraciones", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        cargarInfoPerfil();

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString();
                String apellidos = etApellidos.getText().toString();
                String telefono = etTelefono.getText().toString();
                String usuario = etUsuario.getText().toString();
                String edadAux = etEdad.getText().toString();

                if (nombre.isEmpty() || apellidos.isEmpty() || telefono.isEmpty() || usuario.isEmpty() || edadAux.isEmpty()) {
                    Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    int edad;
                    try {
                        edad = Integer.parseInt(edadAux);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "La edad debe ser un número", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> perfil = new HashMap<>();
                    perfil.put("nombre", nombre);
                    perfil.put("apellidos", apellidos);
                    perfil.put("telefono", telefono);
                    perfil.put("usuario", usuario);
                    perfil.put("edad", edad);

                    DocumentReference docRef = mFirestore.collection("usuarios").document(uid);
                    docRef.update(perfil).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Guardar Info: document.exists() es erróneo");
                        }
                    });
                }
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("recordarUsuario", false);
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                mAuth.getInstance().signOut();
            }
        });

        return view;
    }

    private void cargarInfoPerfil() {
        DocumentReference docRef = mFirestore.collection("usuarios").document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    etNombre.setText(document.getString("nombre"));
                    etApellidos.setText(document.getString("apellidos"));
                    etTelefono.setText(document.getString("telefono"));
                    etUsuario.setText(document.getString("usuario"));

                    Object edadObj = document.get("edad");
                    if (edadObj instanceof Long) {
                        etEdad.setText(String.valueOf((Long) edadObj));
                    } else {
                        etEdad.setText("");
                    }

                } else {
                    Log.d(TAG, "Cargar Info: document.exists() es erróneo");
                }
            } else {
                Log.d(TAG, "Cargar Info: task.isSuccessful() es erróneo");
            }
        });
    }
}
