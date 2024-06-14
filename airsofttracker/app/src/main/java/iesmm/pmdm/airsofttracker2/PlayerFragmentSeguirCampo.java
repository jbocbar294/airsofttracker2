package iesmm.pmdm.airsofttracker2;

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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

        // Instancias de FireBase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid(); // ID del usuario logeado
        listaCampos = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jugador_seguir_campo, container, false);
        // Elementos del layout
        llCampos = view.findViewById(R.id.tblCamposJugador);
        tvNoCampos = view.findViewById(R.id.tvNoCampos);
        etBuscarCampo = view.findViewById(R.id.etBuscarCampo);

        cargarCampos();

        // Escuchador que actualiza la lista de campos al buscar
        etBuscarCampo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<QueryDocumentSnapshot> filteredCampos = new ArrayList<>();
                for (QueryDocumentSnapshot campo : listaCampos) {
                    String nombreCampo = campo.getString("nombre");
                    if (nombreCampo != null && nombreCampo.toLowerCase().contains(s.toString().toLowerCase())) {
                        filteredCampos.add(campo);
                    }
                }
                mostrarListaCampos(filteredCampos);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    // Método que carga los campos no seguidos
    private void cargarCampos() {
        DocumentReference usuarioRef = mFirestore.collection("usuarios").document(uid); // Obtenemos el documento del usuario
        usuarioRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot usuarioDoc = task.getResult(); // Lo almacenamos
                List<String> camposSeguidos = (List<String>) usuarioDoc.get("camposSeguidos"); // Obtenemos la lista de IDs de campos que sigue el usuario

                CollectionReference camposRef = mFirestore.collection("campos"); // Obtenemos la colección de campos
                camposRef.get().addOnCompleteListener(camposTask -> {
                    if (camposTask.isSuccessful()) {
                        listaCampos.clear(); // Vaciamos la lista
                        for (QueryDocumentSnapshot document : camposTask.getResult()) { // Recorremos la lista de campos
                            if (camposSeguidos == null || !camposSeguidos.contains(document.getId())) { // Si la el campo no se encuentra en la lista de campos seguidos...
                                listaCampos.add(document); // Lo añadimos a la lista de campos no seguidos
                            }
                        }
                        mostrarListaCampos(listaCampos); // Pasamos la lista al método
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.errorCargarCampos), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.errorCargarCampos), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarListaCampos(List<QueryDocumentSnapshot> listaCampos) {
        llCampos.removeAllViews(); // Vaciamos la lista
        if (listaCampos.isEmpty()) { // Si la lista de campos está vacía
            // Ocultamos la lista y notificamos
            llCampos.setVisibility(View.GONE);
            tvNoCampos.setVisibility(View.VISIBLE);
        } else { // Si no...
            // Mostramos la lista
            llCampos.setVisibility(View.VISIBLE);
            tvNoCampos.setVisibility(View.GONE);
            // Recorremos la lista de campos
            for (QueryDocumentSnapshot campo : listaCampos) {
                // Obtenemos los datos del campo
                String nombreCampo = campo.getString("nombre");
                String tipoCampo = campo.getString("tipo");
                String descripcionCampo = campo.getString("descripcion");

                // CardView
                LayoutInflater inflater = LayoutInflater.from(getContext());
                CardView cardView = (CardView) inflater.inflate(R.layout.item_campo_jugador, llCampos, false);

                // Elementos del layout del cardview
                TextView tvNombre = cardView.findViewById(R.id.tvCampoNombre);
                TextView tvTipo = cardView.findViewById(R.id.tvCampoTipo);
                TextView tvDescripcion = cardView.findViewById(R.id.tvCampoDescripcion);
                Button btnVerCampo = cardView.findViewById(R.id.btnVerCampo);

                // Asignamos los datos obtenidos
                tvNombre.setText(nombreCampo);
                tvTipo.setText(tipoCampo);
                tvDescripcion.setText(descripcionCampo);

                // Escuchador del botón para seguir al campo
                btnVerCampo.setText(getResources().getString(R.string.unirse));
                btnVerCampo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unirseCampo(campo.getId(), nombreCampo);
                    }
                });

                llCampos.addView(cardView); // Añadimos el cardview a la lista
            }
        }
    }

    private void unirseCampo(String campoId, String nombreCampo) {
        DocumentReference usuarioRef = mFirestore.collection("usuarios").document(uid); // Obtenemos el documento del usuario

        usuarioRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> camposSeguidos = (List<String>) documentSnapshot.get("camposSeguidos"); // Obtenemos la lista de campos seguidos
                if (camposSeguidos == null) {
                    camposSeguidos = new ArrayList<>();
                }

                if (!camposSeguidos.contains(campoId)) { // Controlamos que el usuario no sigue al campo
                    camposSeguidos.add(campoId); // Añadimos el id del campo a la lista
                    usuarioRef.update("camposSeguidos", camposSeguidos) // actualizamos la lista
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), getResources().getString(R.string.teHasUnidoAlCampo) + " " + nombreCampo, Toast.LENGTH_SHORT).show();
                                cargarCampos();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), getResources().getString(R.string.errorSeguirCampo) + " " + nombreCampo, Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.yaSiguesA) + " " + nombreCampo, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), getResources().getString(R.string.errorDatosUsuario), Toast.LENGTH_SHORT).show();
        });
    }
}
