package iesmm.pmdm.airsofttracker2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class OwnerFragmentPartidas extends Fragment {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uidCampo;

    private TableLayout tblPartidas;
    private TextView tvNoPartidas;
    private Button btnAddPartida;

    public OwnerFragmentPartidas() {
        // Required empty public constructor
    }

    public static OwnerFragmentPartidas newInstance() {
        return new OwnerFragmentPartidas();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uidCampo = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_duenyo_partidas, container, false);

        tblPartidas = view.findViewById(R.id.tblPartidas);
        tvNoPartidas = view.findViewById(R.id.tvNoPartidas);
        btnAddPartida = view.findViewById(R.id.btnAddPartida);

        cargarPartidas();

        btnAddPartida.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_pageMisPartidas_to_pageAddPartida);
        });

        return view;
    }

    private void cargarPartidas() {
        mFirestore.collection("partidas")
                .whereEqualTo("idCampo", uidCampo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot.isEmpty()) {
                            tvNoPartidas.setVisibility(View.VISIBLE);
                            tblPartidas.setVisibility(View.GONE);
                        } else {
                            tvNoPartidas.setVisibility(View.GONE);
                            tblPartidas.setVisibility(View.VISIBLE);
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                agregarFilaPartida(document);
                            }
                        }
                    } else {
                        tvNoPartidas.setText("Error al cargar las partidas");
                        tvNoPartidas.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void agregarFilaPartida(DocumentSnapshot document) {
        TableRow row = new TableRow(getContext());

        TextView tvNombre = new TextView(getContext());
        tvNombre.setText(document.getString("nombre"));
        tvNombre.setPadding(8, 8, 8, 8);
        tvNombre.setGravity(View.TEXT_ALIGNMENT_CENTER);
        TableRow.LayoutParams paramsNombre = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvNombre.setLayoutParams(paramsNombre);
        row.addView(tvNombre);

        TextView tvFechaInicio = new TextView(getContext());
        tvFechaInicio.setText(document.getString("fechaInicio"));
        tvFechaInicio.setPadding(8, 8, 8, 8);
        tvFechaInicio.setGravity(View.TEXT_ALIGNMENT_CENTER);
        TableRow.LayoutParams paramsFechaInicio = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f);
        tvFechaInicio.setLayoutParams(paramsFechaInicio);
        row.addView(tvFechaInicio);

        TextView tvFechaFin = new TextView(getContext());
        tvFechaFin.setText(document.getString("fechaFin"));
        tvFechaFin.setPadding(8, 8, 8, 8);
        tvFechaFin.setGravity(View.TEXT_ALIGNMENT_CENTER);
        TableRow.LayoutParams paramsFechaFin = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f);
        tvFechaFin.setLayoutParams(paramsFechaFin);
        row.addView(tvFechaFin);

        List<String> jugadoresEquipo1 = (List<String>) document.get("jugadoresEquipo1");
        List<String> jugadoresEquipo2 = (List<String>) document.get("jugadoresEquipo2");
        int totalJugadores = (jugadoresEquipo1 != null ? jugadoresEquipo1.size() : 0) +
                (jugadoresEquipo2 != null ? jugadoresEquipo2.size() : 0);
        int maxJugadores = document.getLong("maxJugadoresPorEquipo").intValue() * 2; // Multiplicamos por 2 porque es el total por ambos equipos

        TextView tvJugadores = new TextView(getContext());
        tvJugadores.setText(totalJugadores + "/" + maxJugadores);
        tvJugadores.setPadding(8, 8, 8, 8);
        tvJugadores.setGravity(View.TEXT_ALIGNMENT_CENTER);
        TableRow.LayoutParams paramsJugadores = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvJugadores.setLayoutParams(paramsJugadores);
        row.addView(tvJugadores);

        tblPartidas.addView(row);
    }
}
