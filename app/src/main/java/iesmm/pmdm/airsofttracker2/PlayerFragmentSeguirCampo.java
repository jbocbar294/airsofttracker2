package iesmm.pmdm.airsofttracker2;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PlayerFragmentSeguirCampo extends Fragment {

    private LinearLayout llCampos;
    private TextView tvNoCampos;
    private EditText etBuscarCampo;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uid;

    private List<QueryDocumentSnapshot> listaCampos;

    public PlayerFragmentSeguirCampo() {
    }

    public static PlayerFragmentSeguirCampo newInstance() {
        return new PlayerFragmentSeguirCampo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        listaCampos = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jugador_seguir_campo, container, false);
        llCampos = view.findViewById(R.id.tblCamposJugador);
        tvNoCampos = view.findViewById(R.id.tvNoCampos);
        etBuscarCampo = view.findViewById(R.id.etBuscarCampo);

        cargarCampos();

        etBuscarCampo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarCampos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void cargarCampos() {
        DocumentReference usuarioRef = mFirestore.collection("usuarios").document(uid);
        usuarioRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot usuarioDoc = task.getResult();
                List<String> camposSeguidos = (List<String>) usuarioDoc.get("camposSeguidos");

                CollectionReference camposRef = mFirestore.collection("campos");
                camposRef.get().addOnCompleteListener(camposTask -> {
                    if (camposTask.isSuccessful()) {
                        listaCampos.clear();
                        for (QueryDocumentSnapshot document : camposTask.getResult()) {
                            if (camposSeguidos == null || !camposSeguidos.contains(document.getId())) {
                                listaCampos.add(document);
                            }
                        }
                        mostrarListaCampos(listaCampos);
                    } else {
                        Toast.makeText(getContext(), "Error al cargar campos", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarListaCampos(List<QueryDocumentSnapshot> campos) {
        llCampos.removeAllViews();
        if (campos.isEmpty()) {
            llCampos.setVisibility(View.GONE);
            tvNoCampos.setVisibility(View.VISIBLE);
        } else {
            llCampos.setVisibility(View.VISIBLE);
            tvNoCampos.setVisibility(View.GONE);
            for (QueryDocumentSnapshot campo : campos) {
                String nombreCampo = campo.getString("nombre");

                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);

                TextView tvNombre = new TextView(getContext());
                tvNombre.setText(nombreCampo);
                tvNombre.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                tvNombre.setPadding(16, 16, 16, 16);

                Button btnUnirse = new Button(getContext());
                btnUnirse.setText("Unirse");
                btnUnirse.setBackgroundResource(R.drawable.estilo_boton);
                btnUnirse.setTextColor(Color.WHITE);
                btnUnirse.setAllCaps(false);
                btnUnirse.setHeight((int) getResources().getDimension(R.dimen.button_height));
                btnUnirse.setOnClickListener(v -> unirseCampo(campo.getId(), nombreCampo));

                row.addView(tvNombre);
                row.addView(btnUnirse);
                llCampos.addView(row);
            }
        }
    }

    private void filtrarCampos(String query) {
        List<QueryDocumentSnapshot> filteredCampos = new ArrayList<>();
        for (QueryDocumentSnapshot campo : listaCampos) {
            String nombreCampo = campo.getString("nombre");
            if (nombreCampo != null && nombreCampo.toLowerCase().contains(query.toLowerCase())) {
                filteredCampos.add(campo);
            }
        }
        mostrarListaCampos(filteredCampos);
    }

    private void unirseCampo(String campoId, String nombreCampo) {
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference usuarioRef = mFirestore.collection("usuarios").document(uid);

        usuarioRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> camposSeguidos = (List<String>) documentSnapshot.get("camposSeguidos");
                if (camposSeguidos == null) {
                    camposSeguidos = new ArrayList<>();
                }

                if (!camposSeguidos.contains(campoId)) {
                    camposSeguidos.add(campoId);
                    usuarioRef.update("camposSeguidos", camposSeguidos)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Te has unido al campo: " + nombreCampo, Toast.LENGTH_SHORT).show();
                                cargarCampos();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al unirse al campo: " + nombreCampo, Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), "Ya sigues este campo: " + nombreCampo, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
        });
    }
}
