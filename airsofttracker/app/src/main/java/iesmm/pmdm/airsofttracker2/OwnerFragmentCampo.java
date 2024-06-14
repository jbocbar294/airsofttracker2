package iesmm.pmdm.airsofttracker2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerFragmentCampo extends Fragment {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private Button btnEditarCampo;

    public OwnerFragmentCampo() {
    }

    public static OwnerFragmentCampo newInstance() {
        return new OwnerFragmentCampo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_duenyo_campo, container, false);
        TextView tvOwnerNombreCampo = view.findViewById(R.id.tvOwnerNombreCampo);
        TextView tvOwnerDescripcionCampo = view.findViewById(R.id.tvOnwerDescripcionCampo);
        btnEditarCampo = view.findViewById(R.id.btnEditarCampo);

        String uid = mAuth.getCurrentUser().getUid();
        if (uid != null) {
            DocumentReference docRef = mFirestore.collection("campos").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String nombreCampo = document.getString("nombre");
                        String descripcionCampo = document.getString("descripcion");
                        tvOwnerNombreCampo.setText(nombreCampo);
                        tvOwnerDescripcionCampo.setText(descripcionCampo);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener el campo", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d(":::ERROR UID", "UID no encontrado");
        }

        btnEditarCampo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_pageMiCampo_to_pageEditField);
            }
        });

        return view;
    }
}
