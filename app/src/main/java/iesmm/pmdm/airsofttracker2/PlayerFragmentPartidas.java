package iesmm.pmdm.airsofttracker2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlayerFragmentPartidas extends Fragment {

    private LinearLayout tblCamposSeguidos, tblPartidasActuales;
    private TextView tvNoCamposSeguidos;
    private ScrollView svPartidasActuales;
    private TextView tvNoPartidasActuales;
    private Button btnUnirsePartida, btnSeguirCampo;
    private ImageButton btnExpandirSVPartidas;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uid;
    private boolean expandido;
    private ViewGroup.LayoutParams params;

    public PlayerFragmentPartidas() {
    }

    public static PlayerFragmentPartidas newInstance() {
        return new PlayerFragmentPartidas();
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
        View view = inflater.inflate(R.layout.fragment_jugador_partidas, container, false);

        tblPartidasActuales = view.findViewById(R.id.tblPartidasActuales);
        tvNoPartidasActuales = view.findViewById(R.id.tvNoPartidasActuales);
        tblCamposSeguidos = view.findViewById(R.id.llCamposSeguidos);
        svPartidasActuales = view.findViewById(R.id.svPartidasActuales);
        tvNoCamposSeguidos = view.findViewById(R.id.tvNoCamposSeguidos);
        btnUnirsePartida = view.findViewById(R.id.btnUnirsePartida);
        btnSeguirCampo = view.findViewById(R.id.btnSeguirCampo);
        btnExpandirSVPartidas = view.findViewById(R.id.btnExpandirSVPartidas);
        expandido = false;

        cargarCamposSeguidos();
        cargarPartidasActuales();

        btnUnirsePartida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_jugador);
                navController.navigate(R.id.action_pageMisPartidasJugador_to_playerFragmentUnirsePartida);
            }
        });

        btnSeguirCampo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_jugador);
                navController.navigate(R.id.action_pageMisPartidasJugador_to_playerFragmentSeguirCampo);
            }
        });

        btnExpandirSVPartidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int initialHeight = svPartidasActuales.getHeight();
                final int targetHeight;

                if (expandido) {
                    targetHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());
                } else {
                    targetHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
                }

                // Animate the height of the ScrollView
                ValueAnimator heightAnimator = ValueAnimator.ofInt(initialHeight, targetHeight);
                heightAnimator.setDuration(1000);
                heightAnimator.setInterpolator(new DecelerateInterpolator());
                heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ViewGroup.LayoutParams params = svPartidasActuales.getLayoutParams();
                        params.height = (int) animation.getAnimatedValue();
                        svPartidasActuales.setLayoutParams(params);
                    }
                });

                float startRotation = 0f, endRotation = 0f;

                if (expandido) {
                    startRotation = 180f;
                } else {
                    endRotation = 180f;
                }
                ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(btnExpandirSVPartidas, "rotationX", startRotation, endRotation);
                rotationAnimator.setDuration(1700);
                rotationAnimator.setInterpolator(new DecelerateInterpolator());

                heightAnimator.start();
                rotationAnimator.start();

                expandido = !expandido;
            }
        });

        return view;
    }

    private void cargarCamposSeguidos() {
        DocumentReference userRef = mFirestore.collection("usuarios").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> camposSeguidos = (List<String>) document.get("camposSeguidos");
                    if (camposSeguidos == null || camposSeguidos.isEmpty()) {
                        tblCamposSeguidos.setVisibility(View.GONE);
                        tvNoCamposSeguidos.setVisibility(View.VISIBLE);
                        btnExpandirSVPartidas.setVisibility(View.VISIBLE);
                    } else {
                        tblCamposSeguidos.setVisibility(View.VISIBLE);
                        tvNoCamposSeguidos.setVisibility(View.GONE);
                        for (String campoId : camposSeguidos) {
                            mFirestore.collection("campos").document(campoId).get()
                                    .addOnCompleteListener(campoTask -> {
                                        if (campoTask.isSuccessful()) {
                                            DocumentSnapshot campoDocument = campoTask.getResult();
                                            if (campoDocument.exists()) {

                                                String campoNombre = campoDocument.getString("nombre");
                                                LinearLayout row = new LinearLayout(getContext());
                                                row.setOrientation(LinearLayout.HORIZONTAL);
                                                TextView tvNombre = new TextView(getContext());
                                                tvNombre.setText(campoNombre);
                                                tvNombre.setPadding(16, 16, 16, 16);
                                                tvNombre.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                                Button btnVerCampo = new Button(getContext());
                                                btnVerCampo.setText("Ver campo");
                                                btnVerCampo.setBackgroundResource(R.drawable.estilo_boton);
                                                btnVerCampo.setTextColor(getResources().getColor(R.color.blanco));
                                                btnVerCampo.setAllCaps(false);

                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                                        (int) getResources().getDimension(R.dimen.button_height));

                                                btnVerCampo.setLayoutParams(params);

                                                btnVerCampo.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("preferencias", Context.MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.putString("campoId", campoId);
                                                        editor.apply();

                                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_jugador);
                                                        navController.navigate(R.id.action_pageMisPartidasJugador_to_playerFragmentVerCampo);
                                                    }
                                                });

                                                row.addView(tvNombre);
                                                row.addView(btnVerCampo);
                                                tblCamposSeguidos.addView(row);
                                            }
                                        }
                                    });
                        }
                    }
                } else {
                    tblCamposSeguidos.setVisibility(View.GONE);
                    tvNoCamposSeguidos.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar campos seguidos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPartidasActuales() {
        DocumentReference userRef = mFirestore.collection("usuarios").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> partidasActuales = (List<String>) document.get("partidas");
                    if (partidasActuales == null || partidasActuales.isEmpty()) {
                        svPartidasActuales.setVisibility(View.GONE);
                        tblPartidasActuales.setVisibility(View.GONE);
                        tvNoPartidasActuales.setVisibility(View.VISIBLE);
                    } else {
                        for (String partidaId : partidasActuales) {
                            mFirestore.collection("partidas").document(partidaId).get()
                                    .addOnCompleteListener(partidaTask -> {
                                        if (partidaTask.isSuccessful()) {
                                            DocumentSnapshot partidaDocument = partidaTask.getResult();
                                            if (partidaDocument.exists()) {
                                                agregarFilaPartida(partidaDocument);
                                            } else {
                                                Toast.makeText(getContext(), "No se encontró la partida", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Error al cargar la partida", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                } else {
                    tblPartidasActuales.setVisibility(View.GONE);
                    tvNoPartidasActuales.setVisibility(View.VISIBLE);
                    btnExpandirSVPartidas.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar partidas actuales", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void agregarFilaPartida(DocumentSnapshot partidaDocument) {
        String idCampo = partidaDocument.getString("idCampo");

        DocumentReference campoRef = mFirestore.collection("campos").document(idCampo);
        campoRef.get().addOnCompleteListener(campoTask -> {
            if (campoTask.isSuccessful()) {
                DocumentSnapshot campoDocument = campoTask.getResult();
                if (campoDocument.exists()) {
                    String fechaInicio = partidaDocument.getString("fechaInicio");
                    String fechaFin = partidaDocument.getString("fechaFin");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m");

                    LocalDateTime fechaHoraActual = LocalDateTime.now();
                    LocalDateTime fechaFinDateTime = LocalDateTime.parse(fechaFin, formatter);
                    LocalDateTime fechaInicioDateTime = LocalDateTime.parse(fechaInicio, formatter);

                    if (fechaHoraActual.isAfter(fechaFinDateTime)) {
                        return;
                    } else {
                        svPartidasActuales.setVisibility(View.VISIBLE);
                        tblPartidasActuales.setVisibility(View.VISIBLE);
                        tvNoPartidasActuales.setVisibility(View.GONE);

                        String partidaId = partidaDocument.getId();
                        String nombrePartida = partidaDocument.getString("nombre");
                        String nombreCampo = campoDocument.getString("nombre");
                        Long maxJugadoresEquipo = partidaDocument.getLong("maxJugadoresPorEquipo");
                        List<String> jugadoresEquipo1 = (List<String>) partidaDocument.get("jugadoresEquipo1");
                        List<String> jugadoresEquipo2 = (List<String>) partidaDocument.get("jugadoresEquipo2");
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View partidaView = inflater.inflate(R.layout.cardview_partida_pequenyo, tblPartidasActuales, false);

                        TextView tvNombrePartida = partidaView.findViewById(R.id.tvNombrePartida);
                        TextView tvNombreCampo = partidaView.findViewById(R.id.tvNombreCampo);
                        TextView tvJugadoresEquipo1 = partidaView.findViewById(R.id.tvJugadoresEquipo1);
                        TextView tvJugadoresEquipo2 = partidaView.findViewById(R.id.tvJugadoresEquipo2);
                        TextView tvFecha = partidaView.findViewById(R.id.tvFecha);
                        Button btnSalirPartida = partidaView.findViewById(R.id.btnCv);

                        tvNombrePartida.setText(nombrePartida);
                        tvNombreCampo.setText(nombreCampo);
                        tvJugadoresEquipo1.setText("Equipo 1: " + jugadoresEquipo1.size() + " / " + maxJugadoresEquipo);
                        tvJugadoresEquipo2.setText("Equipo 2: " + jugadoresEquipo2.size() + " / " + maxJugadoresEquipo);
                        tvFecha.setText(fechaInicio + " - " + fechaFin);

                        if (fechaHoraActual.isAfter(fechaInicioDateTime) && fechaHoraActual.isBefore(fechaFinDateTime)) {
                            btnSalirPartida.setText("En curso");
                            btnSalirPartida.setBackgroundColor(R.color.naranja);
                            btnSalirPartida.setEnabled(false);
                        } else {
                            btnSalirPartida.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog builder = new AlertDialog.Builder(getContext())
                                            .setTitle(nombrePartida)
                                            .setMessage("¿Quieres darte de baja?")
                                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                salirDePartida(partidaId);
                                            }
                                            }).setNegativeButton("No", null)
                                            .show();

                                }
                            });
                        }

                        tblPartidasActuales.addView(partidaView);
                    }


                }
            }
        });
    }


    private void salirDePartida(String partidaId) {
        DocumentReference partidaRef = mFirestore.collection("partidas").document(partidaId);
        DocumentReference usuarioRef = mFirestore.collection("usuarios").document(uid);

        usuarioRef.update("partidas", FieldValue.arrayRemove(partidaId))
                .addOnSuccessListener(aVoid -> {
                    partidaRef.update("jugadoresEquipo1", FieldValue.arrayRemove(uid));
                    partidaRef.update("jugadoresEquipo2", FieldValue.arrayRemove(uid))
                            .addOnCompleteListener(task -> {
                                Toast.makeText(getContext(), "Has salido de la partida", Toast.LENGTH_SHORT).show();
                                cargarPartidasActuales();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al salir del equipo en la partida", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al actualizar la lista de partidas del usuario", Toast.LENGTH_SHORT).show();
                });
    }
}
