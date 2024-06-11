package iesmm.pmdm.airsofttracker2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class PlayerFragmentVerCampo extends Fragment {

    private TextView tvNombreCampo, tvTipoCampo, tvDescripcionCampo;
    private Button btnDejarSeguirCampo;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uid;
    private String campoId;

    public PlayerFragmentVerCampo() {
    }

    public static PlayerFragmentVerCampo newInstance() {
        return new PlayerFragmentVerCampo();
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
        View view = inflater.inflate(R.layout.fragment_jugador_ver_campo, container, false);

        tvNombreCampo = view.findViewById(R.id.tvNombreCampo);
        tvTipoCampo = view.findViewById(R.id.tvTipoCampo);
        tvDescripcionCampo = view.findViewById(R.id.tvDescripcionCampo);
        btnDejarSeguirCampo = view.findViewById(R.id.btnDejarSeguirCampo);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        campoId = sharedPreferences.getString("campoId", null);

        cargarDatosCampo();

        btnDejarSeguirCampo.setOnClickListener(v -> mostrarDialogoDejarSeguir());

        return view;
    }

    private void cargarDatosCampo() {
        DocumentReference campoRef = mFirestore.collection("campos").document(campoId);
        campoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    tvNombreCampo.setText(document.getString("nombre"));
                    tvTipoCampo.setText(document.getString("tipo"));
                    tvDescripcionCampo.setText(document.getString("desc"));
                } else {
                    Toast.makeText(getContext(), "Campo no encontrado", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar datos del campo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoDejarSeguir() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Dejar de seguir")
                .setMessage("¿Seguro que quieres dejar de seguir este campo?")
                .setPositiveButton("Sí", (dialog, which) -> dejarDeSeguirCampo())
                .setNegativeButton("No", null)
                .show();
    }

    private void dejarDeSeguirCampo() {
        DocumentReference userRef = mFirestore.collection("usuarios").document(uid);
        userRef.update("camposSeguidos", FieldValue.arrayRemove(campoId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Has dejado de seguir el campo", Toast.LENGTH_SHORT).show();
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_jugador);
                        navController.navigate(R.id.action_playerFragmentVerCampo_to_pageMisPartidasJugador);
                    } else {
                        Toast.makeText(getContext(), "Error al dejar de seguir el campo", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
