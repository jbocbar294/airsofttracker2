package iesmm.pmdm.airsofttracker2;

import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
        // Instancia de Firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid(); // Obtenemos el ID del usuario logueado
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jugador_ver_campo, container, false);

        // Elementos del layout
        tvNombreCampo = view.findViewById(R.id.tvNombreCampo);
        tvTipoCampo = view.findViewById(R.id.tvTipoCampo);
        tvDescripcionCampo = view.findViewById(R.id.tvDescripcionCampo);
        btnDejarSeguirCampo = view.findViewById(R.id.btnDejarSeguirCampo);

        // Obtenemos el ID del campo de las sharedpreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        campoId = sharedPreferences.getString("campoId", null);

        cargarDatosCampo(); // Cargamos los datos del campo

        btnDejarSeguirCampo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDejarDeSeguir();
            }
        });

        return view;
    }

    // Método para cargar los datos del campo desde Firestore
    private void cargarDatosCampo() {
        DocumentReference campoRef = mFirestore.collection("campos").document(campoId); // Obtenemos el documento del campo
        campoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult(); // Lo almacenamos
                if (document.exists()) {
                    // Actualizamos los TextViews con los datos del documento
                    tvNombreCampo.setText(document.getString("nombre"));
                    tvTipoCampo.setText(document.getString("tipo"));
                    tvDescripcionCampo.setText(document.getString("descripcion"));
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.errorCargarCampos), Toast.LENGTH_SHORT).show(); // Mensaje de error si el documento no existe
                }
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.errorCargarCampos), Toast.LENGTH_SHORT).show(); // Mensaje de error si falla la carga
            }
        });
    }

    // Método para mostrar el diálogo de confirmación para dejar de seguir el campo
    private void dialogDejarDeSeguir() {
        new AlertDialog.Builder(requireContext()) // Mostramos el AlertDialog
                // Asignamos título, mensaje y botones
                .setTitle(getResources().getString(R.string.dejarDeSeguir))
                .setMessage(getResources().getString(R.string.quieresDejarDeSeguir))
                .setPositiveButton(getResources().getString(R.string.si), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dejarDeSeguir();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), null) // Acción para cancelar
                .show();
    }

    // Método para dejar de seguir el campo
    private void dejarDeSeguir() {
        DocumentReference userRef = mFirestore.collection("usuarios").document(uid); // Obtenemos el documento del usuario
        userRef.update("camposSeguidos", FieldValue.arrayRemove(campoId)) // Eliminamos el ID del campo de la lista de campos seguidos
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), getResources().getString(R.string.hasDejadoDeSeguir), Toast.LENGTH_SHORT).show();
                        // Navegamos de vuelta a la pantalla de mis partidas
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_jugador);
                        navController.navigate(R.id.action_playerFragmentVerCampo_to_pageMisPartidasJugador);
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.errorDejarDeSeguir), Toast.LENGTH_SHORT).show(); // Mensaje de error
                    }
                });
    }
}
