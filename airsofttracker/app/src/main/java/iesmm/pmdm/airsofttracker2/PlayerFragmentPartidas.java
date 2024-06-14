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
import androidx.cardview.widget.CardView;
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
    private CardView cardViewPartidasActuales;
    private CardView cardViewCamposSeguidos;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uid;
    private boolean expandido;

    public PlayerFragmentPartidas() {
    }

    public static PlayerFragmentPartidas newInstance() {
        return new PlayerFragmentPartidas();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Instancias de firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid(); // ID del usuario logeado
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jugador_partidas, container, false);

        // Elementos del layout
        tblPartidasActuales = view.findViewById(R.id.tblPartidasActuales);
        tvNoPartidasActuales = view.findViewById(R.id.tvNoPartidasActuales);
        tblCamposSeguidos = view.findViewById(R.id.llCamposSeguidos);
        svPartidasActuales = view.findViewById(R.id.svPartidasActuales);
        tvNoCamposSeguidos = view.findViewById(R.id.tvNoCamposSeguidos);
        btnUnirsePartida = view.findViewById(R.id.btnUnirsePartida);
        btnSeguirCampo = view.findViewById(R.id.btnSeguirCampo);
        btnExpandirSVPartidas = view.findViewById(R.id.btnExpandirSVPartidas);
        cardViewPartidasActuales = view.findViewById(R.id.cardViewPartidasActuales);
        cardViewCamposSeguidos = view.findViewById(R.id.cardViewCamposSeguidos);
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

        // Escuchador del botón que permite expander la lista de partidas
        btnExpandirSVPartidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int initialHeight = cardViewPartidasActuales.getHeight();
                final int targetHeight;

                // Boolean que permite usar el ImageButton en forma de interruptor "on/off"
                if (expandido) {
                    targetHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
                } else {
                    targetHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
                }

                // Animación al expander o contraer las partidas
                ValueAnimator heightAnimator = ValueAnimator.ofInt(initialHeight, targetHeight);
                heightAnimator.setDuration(300); // Duración en ms
                heightAnimator.setInterpolator(new DecelerateInterpolator());
                heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ViewGroup.LayoutParams params = cardViewPartidasActuales.getLayoutParams();
                        params.height = (int) animation.getAnimatedValue();
                        cardViewPartidasActuales.setLayoutParams(params);
                    }
                });

                float startRotation = 0f, endRotation = 0f;

                if (expandido) {
                    startRotation = 180f;
                } else {
                    endRotation = 180f;
                }

                // Animación para girar la flecha
                ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(btnExpandirSVPartidas, "rotationX", startRotation, endRotation);
                rotationAnimator.setDuration(500); // Duración en ms
                rotationAnimator.setInterpolator(new DecelerateInterpolator());

                // Comienzan las animaciones
                heightAnimator.start();
                rotationAnimator.start();

                // Invertimos el valor ("on/off")
                expandido = !expandido;
            }
        });

        return view;
    }

    // Método que carga los campos a los que sigue el usuario
    private void cargarCamposSeguidos() {
        // Obtenemos el documento de usuario
        DocumentReference userRef = mFirestore.collection("usuarios").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult(); // Lo almacenamos
                if (document.exists()) {
                    List<String> camposSeguidos = (List<String>) document.get("camposSeguidos"); // Obtenemos la lista de IDs de campos seguidos
                    if (camposSeguidos != null && !camposSeguidos.isEmpty()) { // Mostramos o no los elementos en función de la lista de campos
                        cardViewCamposSeguidos.setVisibility(View.VISIBLE);
                        tvNoCamposSeguidos.setVisibility(View.GONE);
                        for (String campoId : camposSeguidos) { // Recorremos la lista de campos seguidos
                            mFirestore.collection("campos").document(campoId).get() // Obtenemos el documento del campo
                                    .addOnCompleteListener(campoTask -> {
                                        if (campoTask.isSuccessful()) {
                                            DocumentSnapshot campoDocument = campoTask.getResult(); // Lo almacenamos
                                            if (campoDocument.exists()) {
                                                // Obtenemos los campos a mostrar
                                                String campoNombre = campoDocument.getString("nombre");
                                                String campoTipo = campoDocument.getString("tipo");
                                                String campoDescripcion = campoDocument.getString("descripcion");

                                                // CardView
                                                LayoutInflater inflater = LayoutInflater.from(getContext());
                                                CardView cardView = (CardView) inflater.inflate(R.layout.item_campo_jugador, tblCamposSeguidos, false);
                                                // Elementos del layout del Cardview
                                                TextView tvNombre = cardView.findViewById(R.id.tvCampoNombre);
                                                TextView tvTipo = cardView.findViewById(R.id.tvCampoTipo);
                                                TextView tvDescripcion = cardView.findViewById(R.id.tvCampoDescripcion);
                                                Button btnVerCampo = cardView.findViewById(R.id.btnVerCampo);

                                                // Asignamos los valores obtenidos
                                                tvNombre.setText(campoNombre);
                                                tvTipo.setText(campoTipo);
                                                tvDescripcion.setText(campoDescripcion);

                                                btnVerCampo.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        // Guardamos el id del campo
                                                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("preferencias", Context.MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.putString("campoId", campoId);
                                                        editor.apply();

                                                        // Navegamos a PlayerFragmentVerCampo
                                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_jugador);
                                                        navController.navigate(R.id.action_pageMisPartidasJugador_to_playerFragmentVerCampo);
                                                    }
                                                });

                                                tblCamposSeguidos.addView(cardView); // Añadimos el cardview a la tabla
                                            }
                                        }
                                    });
                        }
                    }
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar campos seguidos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPartidasActuales() {
        // Obtenemos el documento del usuario
        DocumentReference userRef = mFirestore.collection("usuarios").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult(); // Lo almacenamos
                if (document.exists()) {
                    List<String> partidasActuales = (List<String>) document.get("partidas"); // Obtenemos la lista de partidas
                    if (partidasActuales != null && !partidasActuales.isEmpty()) {
                        for (String partidaId : partidasActuales) { // Recorremos la lista de partidas
                            mFirestore.collection("partidas").document(partidaId).get() // Obtenemos el documento de la partida
                                    .addOnCompleteListener(partidaTask -> {
                                        if (partidaTask.isSuccessful()) {
                                            DocumentSnapshot partidaDocument = partidaTask.getResult(); // La almacenamos
                                            if (partidaDocument.exists()) {
                                                agregarFilaPartida(partidaDocument); // Pasamos el documento al método para cargarla
                                            } else {
                                                Toast.makeText(getContext(), "No se encontró la partida", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Error al cargar la partida", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar partidas actuales", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método que agrega el CardView de la partida a la lista
    private void agregarFilaPartida(DocumentSnapshot partidaDocument) {
        String idCampo = partidaDocument.getString("idCampo"); // Obtenemos el id del campo

        DocumentReference campoRef = mFirestore.collection("campos").document(idCampo); // Obtenemos el documento del campo con el id
        campoRef.get().addOnCompleteListener(campoTask -> {
            if (campoTask.isSuccessful()) {
                DocumentSnapshot campoDocument = campoTask.getResult(); // Lo almacenamos
                if (campoDocument.exists()) {
                    // Obtenemos fechas de inicio y fin de la partida
                    String fechaInicio = partidaDocument.getString("fechaInicio");
                    String fechaFin = partidaDocument.getString("fechaFin");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m");

                    LocalDateTime fechaHoraActual = LocalDateTime.now(); // Obtenemos la fecha
                    // Formateamos las fechas de inicio y fin de partida
                    LocalDateTime fechaFinDateTime = LocalDateTime.parse(fechaFin, formatter);
                    LocalDateTime fechaInicioDateTime = LocalDateTime.parse(fechaInicio, formatter);

                    // Comparamos las fechas y solo añadimos las que no hayan empezado
                    if (fechaHoraActual.isBefore(fechaFinDateTime)) {
                        // Si se llega a este punto significa que al menos una partida se ha añadido,
                        // por lo que mostramos los elementos del layout
                        cardViewPartidasActuales.setVisibility(View.VISIBLE);
                        btnExpandirSVPartidas.setVisibility(View.VISIBLE);
                        tvNoPartidasActuales.setVisibility(View.GONE);

                        // Obtenemos los campos
                        String partidaId = partidaDocument.getId();
                        String nombrePartida = partidaDocument.getString("nombre");
                        String nombreCampo = campoDocument.getString("nombre");
                        Long maxJugadoresEquipo = partidaDocument.getLong("maxJugadoresPorEquipo");
                        List<String> jugadoresEquipo1 = (List<String>) partidaDocument.get("jugadoresEquipo1");
                        List<String> jugadoresEquipo2 = (List<String>) partidaDocument.get("jugadoresEquipo2");

                        // CardView
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View partidaView = inflater.inflate(R.layout.item_partida_jugador_pequenyo, tblPartidasActuales, false);

                        // Elementos del layout del CardView
                        TextView tvNombrePartida = partidaView.findViewById(R.id.tvNombrePartida);
                        TextView tvNombreCampo = partidaView.findViewById(R.id.tvNombreCampo);
                        TextView tvJugadoresEquipo1 = partidaView.findViewById(R.id.tvJugadoresEquipo1);
                        TextView tvJugadoresEquipo2 = partidaView.findViewById(R.id.tvJugadoresEquipo2);
                        TextView tvFecha = partidaView.findViewById(R.id.tvFecha);
                        Button btnSalirPartida = partidaView.findViewById(R.id.btnCv);

                        // Asignamos los valores obtenidos a los elementos del CardView
                        tvNombrePartida.setText(nombrePartida);
                        tvNombreCampo.setText(nombreCampo);
                        tvJugadoresEquipo1.setText(getResources().getString(R.string.equipo1) + ": " + jugadoresEquipo1.size() + " / " + maxJugadoresEquipo);
                        tvJugadoresEquipo2.setText(getResources().getString(R.string.equipo2) + ": " + jugadoresEquipo2.size() + " / " + maxJugadoresEquipo);
                        tvFecha.setText(fechaInicio + " - " + fechaFin);

                        // Comprobamos si la partida está en curso
                        if (fechaHoraActual.isAfter(fechaInicioDateTime) && fechaHoraActual.isBefore(fechaFinDateTime)) {
                            btnSalirPartida.setText(getResources().getString(R.string.enCurso));
                            btnSalirPartida.setTextColor(getResources().getColor(R.color.naranja));
                            btnSalirPartida.setEnabled(false);
                        } else { // Si no...
                            // Asignamos el escuchador del botón
                            btnSalirPartida.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // AlertDialog que permite al usuario darse de baja de la partida
                                    AlertDialog builder = new AlertDialog.Builder(getContext())
                                            .setTitle(nombrePartida)
                                            .setMessage(getResources().getString(R.string.quieresDarteDeBaja))
                                            .setPositiveButton(getResources().getString(R.string.si), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    salirDePartida(partidaId);
                                                }
                                            }).setNegativeButton(getResources().getString(R.string.no), null)
                                            .show();
                                }
                            });
                        }

                        tblPartidasActuales.addView(partidaView); // Añadimos el cardview a la lista
                        tblCamposSeguidos.refreshDrawableState(); // Refrescamos la vista
                    }
                }
            }
        });
    }

    // Método que para darse de baja de una partida
    private void salirDePartida(String partidaId) {
        // Obtenemos los documentos de la partida y del jugador
        DocumentReference partidaRef = mFirestore.collection("partidas").document(partidaId);
        DocumentReference usuarioRef = mFirestore.collection("usuarios").document(uid);

        // Borramos la partida de la lista de partidas del jugador
        usuarioRef.update("partidas", FieldValue.arrayRemove(partidaId))
                .addOnSuccessListener(aVoid -> {
                    // Borramos el id de cualquiera de las listas de jugadores de la partida
                    partidaRef.update("jugadoresEquipo1", FieldValue.arrayRemove(uid));
                    partidaRef.update("jugadoresEquipo2", FieldValue.arrayRemove(uid))
                            .addOnCompleteListener(task -> {
                                Toast.makeText(getContext(), getResources().getString(R.string.hasAbandonadoLaPartida), Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), getResources().getString(R.string.errorSalirPartida), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), getResources().getString(R.string.errorSalirPartida), Toast.LENGTH_SHORT).show();
                });

        cargarPartidasActuales();
    }

}