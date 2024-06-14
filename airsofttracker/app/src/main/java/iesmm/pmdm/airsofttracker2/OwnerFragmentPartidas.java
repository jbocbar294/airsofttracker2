package iesmm.pmdm.airsofttracker2;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OwnerFragmentPartidas extends Fragment {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uidCampo;

    private ViewGroup llPartidas;
    private ScrollView svPartidas;
    private TextView tvNoPartidas;
    private Button btnAddPartida;

    public OwnerFragmentPartidas() {
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

        llPartidas = view.findViewById(R.id.llPartidas);
        svPartidas = view.findViewById(R.id.svPartidas);
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
                            llPartidas.setVisibility(View.GONE);
                            svPartidas.setVisibility(View.GONE);
                        } else {
                            tvNoPartidas.setVisibility(View.GONE);
                            llPartidas.setVisibility(View.VISIBLE);
                            svPartidas.setVisibility(View.VISIBLE);
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                agregarCardPartida(document);
                            }
                        }
                    } else {
                        tvNoPartidas.setText(getResources().getString(R.string.errorCargarPartidas));
                        tvNoPartidas.setVisibility(View.VISIBLE);
                    }
                });
    }

    @SuppressLint("ResourceAsColor")
    private void agregarCardPartida(DocumentSnapshot document) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View cardView = inflater.inflate(R.layout.item_partida_duenyo, llPartidas, false);

        TextView tvNombre = cardView.findViewById(R.id.tvNombre);
        TextView tvFechaInicio = cardView.findViewById(R.id.tvFechaInicio);
        TextView tvFechaFin = cardView.findViewById(R.id.tvFechaFin);
        TextView tvJugadores = cardView.findViewById(R.id.tvJugadores);
        Button btnVerDetalles = cardView.findViewById(R.id.btnVerDetalles);
        Button btnCancelar = cardView.findViewById(R.id.btnCancelar);
        TextView tvGanador = cardView.findViewById(R.id.tvGanador);
        View llDetalles = cardView.findViewById(R.id.llDetalles);
        ViewGroup llEquipo1 = cardView.findViewById(R.id.llEquipo1);
        ViewGroup llEquipo2 = cardView.findViewById(R.id.llEquipo2);

        tvNombre.setText(document.getString("nombre"));
        tvFechaInicio.setText(document.getString("fechaInicio"));
        tvFechaFin.setText(document.getString("fechaFin"));

        List<String> jugadoresEquipo1 = (List<String>) document.get("jugadoresEquipo1");
        List<String> jugadoresEquipo2 = (List<String>) document.get("jugadoresEquipo2");
        int totalJugadores = (jugadoresEquipo1 != null ? jugadoresEquipo1.size() : 0) +
                (jugadoresEquipo2 != null ? jugadoresEquipo2.size() : 0);
        int maxJugadores = document.getLong("maxJugadoresPorEquipo").intValue() * 2;
        tvJugadores.setText(totalJugadores + "/" + maxJugadores);

        cargarNombresJugadores(llEquipo1, jugadoresEquipo1);
        cargarNombresJugadores(llEquipo2, jugadoresEquipo2);

        String fechaFin = document.getString("fechaFin");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m");
        LocalDateTime fechaFinDateTime = LocalDateTime.parse(fechaFin, formatter);
        LocalDateTime fechaHoraActual = LocalDateTime.now();

        String ganador = document.getString("ganador");
        if (ganador != null && !ganador.isEmpty()) {
            btnCancelar.setVisibility(View.GONE);
            tvGanador.setVisibility(View.VISIBLE);
            tvGanador.setText(getResources().getString(R.string.ganadorEquipo) + ganador);
        } else if (fechaHoraActual.isAfter(fechaFinDateTime)) {
            btnCancelar.setText(getResources().getString(R.string.asignarGanador));
            btnCancelar.setTextColor(getResources().getColor(R.color.texto_secundario));
            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarGanador(document.getId(), jugadoresEquipo1, jugadoresEquipo2, cardView);
                }
            });
        } else {
            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(getResources().getString(R.string.cancelarPartida))
                            .setMessage(getResources().getString(R.string.quieresCancelarLaPartida))
                            .setPositiveButton(getResources().getString(R.string.si), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    cancelarPartida(document.getId(), cardView);
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.no), null)
                            .show();
                }
            });
        }

        btnVerDetalles.setOnClickListener(v -> {
            if (llDetalles.getVisibility() == View.GONE) {
                llDetalles.setVisibility(View.VISIBLE);
            } else {
                llDetalles.setVisibility(View.GONE);
            }
        });

        llPartidas.addView(cardView);
    }

    @SuppressLint("ResourceAsColor")
    private void cargarNombresJugadores(ViewGroup container, List<String> jugadores) {
        if (jugadores != null && !jugadores.isEmpty()) {
            for (String jugadorId : jugadores) {
                DocumentReference usuarioRef = mFirestore.collection("usuarios").document(jugadorId);
                usuarioRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nombreUsuario = document.getString("usuario");

                            TextView tvJugador = new TextView(getContext());
                            tvJugador.setText(nombreUsuario);
                            tvJugador.setTextColor(getResources().getColor(R.color.texto_primario));
                            tvJugador.setTextSize(14);
                            tvJugador.setPadding(10, 10, 10, 15);
                            container.addView(tvJugador);

                            View view = new View(getContext());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    1
                            );
                            view.setLayoutParams(params);
                            view.setBackgroundColor(getResources().getColor(R.color.negro));
                            container.addView(view);
                        }
                    }
                });
            }
        }
    }

    private void cancelarPartida(String partidaId, View cardView) {
        DocumentReference partidaRef = mFirestore.collection("partidas").document(partidaId);
        partidaRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> jugadoresEquipo1 = (List<String>) document.get("jugadoresEquipo1");
                    List<String> jugadoresEquipo2 = (List<String>) document.get("jugadoresEquipo2");

                    eliminarPartidaDeJugadores(jugadoresEquipo1, partidaId);
                    eliminarPartidaDeJugadores(jugadoresEquipo2, partidaId);

                    partidaRef.delete().addOnSuccessListener(aVoid -> {
                        llPartidas.removeView(cardView);
                        if (llPartidas.getChildCount() == 0) {
                            tvNoPartidas.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(requireContext(), "Partida cancelada", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error al cancelar la partida", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                Toast.makeText(requireContext(), "Error al obtener los detalles de la partida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarPartidaDeJugadores(List<String> jugadores, String partidaId) {
        if (jugadores != null) {
            for (String jugadorId : jugadores) {
                DocumentReference usuarioRef = mFirestore.collection("usuarios").document(jugadorId);
                usuarioRef.update("partidas", FieldValue.arrayRemove(partidaId))
                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error al actualizar la lista de partidas del usuario", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void asignarGanador(String partidaId, List<String> jugadoresEquipo1, List<String> jugadoresEquipo2, View cardView) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getResources().getString(R.string.asignarGanador))
                .setMessage(getResources().getString(R.string.seleccionaEquipoGanador))
                .setPositiveButton(getResources().getString(R.string.equipo1), (dialog, which) -> {
                    actualizarGanador(partidaId, "1", jugadoresEquipo1, cardView);
                })
                .setNegativeButton(getResources().getString(R.string.equipo2), (dialog, which) -> {
                    actualizarGanador(partidaId, "2", jugadoresEquipo2, cardView);
                })
                .setNeutralButton(getResources().getString(R.string.cancelar), null)
                .show();
    }

    private void actualizarGanador(String partidaId, String ganador, List<String> jugadoresGanadores, View cardView) {
        DocumentReference partidaRef = mFirestore.collection("partidas").document(partidaId);
        partidaRef.update("ganador", ganador).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        TextView tvGanador = cardView.findViewById(R.id.tvGanador);
                        Button btnCancelar = cardView.findViewById(R.id.btnCancelar);
                        tvGanador.setText(getResources().getString(R.string.ganadorEquipo) + ganador);
                        tvGanador.setVisibility(View.VISIBLE);
                        btnCancelar.setVisibility(View.GONE);
                        actualizarPartidasGanadasJugador(jugadoresGanadores);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), getResources().getString(R.string.errorAsignarGanador), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarPartidasGanadasJugador(List<String> jugadoresGanadores) {
        for (String jugadorId : jugadoresGanadores) {
            DocumentReference usuarioRef = mFirestore.collection("usuarios").document(jugadorId);
            usuarioRef.update("partidasGanadas", FieldValue.increment(1));
        }
    }
}
