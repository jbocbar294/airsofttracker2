package iesmm.pmdm.airsofttracker2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlayerFragmentUnirsePartida extends Fragment {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uid;

    private LinearLayout tblUnirsePartidas;
    private Vibrator vibrator;

    public PlayerFragmentUnirsePartida() {
    }

    public static PlayerFragmentUnirsePartida newInstance() {
        return new PlayerFragmentUnirsePartida();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jugador_unirse_partida, container, false);
        tblUnirsePartidas = view.findViewById(R.id.tblUnirsePartidas);

        cargarPartidas();

        return view;
    }

    private void cargarPartidas() {
        DocumentReference userRef = mFirestore.collection("usuarios").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> camposSeguidos = (List<String>) document.get("camposSeguidos");
                    if (camposSeguidos != null && !camposSeguidos.isEmpty()) {
                        for (String campoId : camposSeguidos) {
                            mFirestore.collection("campos").document(campoId).get()
                                    .addOnCompleteListener(campoTask -> {
                                        if (campoTask.isSuccessful()) {
                                            DocumentSnapshot campoDocument = campoTask.getResult();
                                            if (campoDocument.exists()) {
                                                String nombreCampo = campoDocument.getString("nombre");
                                                mFirestore.collection("partidas").whereEqualTo("idCampo", campoId).get()
                                                        .addOnCompleteListener(partidasTask -> {
                                                            if (partidasTask.isSuccessful()) {
                                                                for (QueryDocumentSnapshot partidaDocument : partidasTask.getResult()) {
                                                                    nuevaFila(partidaDocument, nombreCampo);
                                                                }
                                                            } else {
                                                                Toast.makeText(getContext(), "Error al cargar partidas", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(getContext(), "No se encontró el campo", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "No sigues a ningún campo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No se encontraron datos de usuario", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void nuevaFila(QueryDocumentSnapshot partidaDocument, String nombreCampo) {
        String fechaInicio = partidaDocument.getString("fechaInicio");
        String fechaFin = partidaDocument.getString("fechaFin");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m");
        LocalDateTime fechaInicioDateTime = LocalDateTime.parse(fechaInicio, formatter);
        LocalDateTime fechaFinDateTime = LocalDateTime.parse(fechaFin, formatter);
        LocalDateTime ahora = LocalDateTime.now();

        if (ahora.isAfter(fechaInicioDateTime)) {
            return;
        } else {
            String partidaId = partidaDocument.getId();
            String nombrePartida = partidaDocument.getString("nombre");
            Long maxJugadoresEquipo = partidaDocument.getLong("maxJugadoresPorEquipo");
            List<String> jugadoresEquipo1 = (List<String>) partidaDocument.get("jugadoresEquipo1");
            List<String> jugadoresEquipo2 = (List<String>) partidaDocument.get("jugadoresEquipo2");

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View partidaView = inflater.inflate(R.layout.cardview_partida, tblUnirsePartidas, false);

            TextView tvNombrePartida = partidaView.findViewById(R.id.tvNombrePartida);
            TextView tvNombreCampo = partidaView.findViewById(R.id.tvNombreCampo);
            TextView tvJugadoresEquipo1 = partidaView.findViewById(R.id.tvJugadoresEquipo1);
            TextView tvJugadoresEquipo2 = partidaView.findViewById(R.id.tvJugadoresEquipo2);
            TextView tvFecha = partidaView.findViewById(R.id.tvFecha);

            tvNombrePartida.setText(nombrePartida);
            tvNombreCampo.setText(nombreCampo);
            tvJugadoresEquipo1.setText("Equipo 1: " + jugadoresEquipo1.size() + " / " + maxJugadoresEquipo);
            tvJugadoresEquipo2.setText("Equipo 2: " + jugadoresEquipo2.size() + " / " + maxJugadoresEquipo);
            tvFecha.setText(fechaInicio + " - " + fechaFin);

            Button btnUnirse = partidaView.findViewById(R.id.btnCv);
            long[] vibraciones = {0, 50, 100, 50};

            if (ahora.isAfter(fechaInicioDateTime) && ahora.isBefore(fechaFinDateTime)) {
                btnUnirse.setAlpha(0.5f);
                btnUnirse.setText("En curso");
                btnUnirse.setOnClickListener(v -> vibrator.vibrate(vibraciones, -1));
            } else if ((jugadoresEquipo1.size() + jugadoresEquipo2.size()) >= maxJugadoresEquipo * 2) {
                btnUnirse.setAlpha(0.5f);
                btnUnirse.setText("Partida completa");
                btnUnirse.setOnClickListener(v -> vibrator.vibrate(vibraciones, -1));
            } else if (jugadoresEquipo1.contains(uid) || jugadoresEquipo2.contains(uid)) {
                btnUnirse.setAlpha(0.5f);
                btnUnirse.setText("Ya te has unido");
                btnUnirse.setOnClickListener(v -> vibrator.vibrate(vibraciones, -1));
            } else {
                btnUnirse.setOnClickListener(v -> unirseAPartida(partidaId, nombrePartida));
            }

            tblUnirsePartidas.addView(partidaView);
        }


    }

    private void unirseAPartida(String partidaId, String nombrePartida) {
        DocumentReference partidaRef = mFirestore.collection("partidas").document(partidaId);
        partidaRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot partidaDocument = task.getResult();
                if (partidaDocument.exists()) {
                    Long maxJugadoresEquipo = partidaDocument.getLong("maxJugadoresPorEquipo");
                    List<String> jugadoresEquipo1 = (List<String>) partidaDocument.get("jugadoresEquipo1");
                    List<String> jugadoresEquipo2 = (List<String>) partidaDocument.get("jugadoresEquipo2");

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setTitle(nombrePartida)
                            .setMessage("¿Quieres unirte a la partida?");

                    if (jugadoresEquipo1.size() < maxJugadoresEquipo) {
                        builder.setPositiveButton("Equipo 1", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                anyadirJugadorAEquipo(partidaId, "jugadoresEquipo1");
                            }
                        });
                    }
                    if (jugadoresEquipo2.size() < maxJugadoresEquipo) {
                        builder.setNegativeButton("Equipo 2", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                anyadirJugadorAEquipo(partidaId, "jugadoresEquipo2");
                            }
                        });

                    }

                    builder.setNeutralButton("Cancelar", null);
                    builder.show();

                } else {
                    Toast.makeText(getContext(), "No se encontraron datos de la partida", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error al obtener datos de la partida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void anyadirJugadorAEquipo(String partidaId, String equipo) {
        DocumentReference partidaRef = mFirestore.collection("partidas").document(partidaId);
        DocumentReference usuarioRef = mFirestore.collection("usuarios").document(uid);

        partidaRef.update(equipo, FieldValue.arrayUnion(uid))
                .addOnSuccessListener(aVoid -> {
                    usuarioRef.update("partidas", FieldValue.arrayUnion(partidaId));
                });

        getFragmentManager().popBackStack();
    }
}
