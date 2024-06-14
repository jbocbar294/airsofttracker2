package iesmm.pmdm.airsofttracker2;

import android.annotation.SuppressLint;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
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
    private ScrollView svNuevasPartidas;
    private TextView tvNoPartidas;
    private Vibrator vibrator; // Objeto para que el dispositivo vibre

    public PlayerFragmentUnirsePartida() {
    }

    public static PlayerFragmentUnirsePartida newInstance() {
        return new PlayerFragmentUnirsePartida();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Instancia de FireBase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid(); // ID del usuario logeado
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jugador_unirse_partida, container, false);

        // Elementos del layout
        tblUnirsePartidas = view.findViewById(R.id.tblUnirsePartidas);
        svNuevasPartidas = view.findViewById(R.id.svNuevasPartidas);
        tvNoPartidas = view.findViewById(R.id.tvNoPartidas);

        cargarPartidas();

        tvNoPartidas.setVisibility(View.VISIBLE);
        tblUnirsePartidas.setVisibility(View.GONE);
        svNuevasPartidas.setVisibility(View.GONE);

        return view;
    }

    // Método que carga las partidas
    private void cargarPartidas() {
        DocumentReference userRef = mFirestore.collection("usuarios").document(uid); // Obtenemos el documento del usuario
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult(); // Lo almacenamos
                if (document.exists()) {
                    List<String> camposSeguidos = (List<String>) document.get("camposSeguidos"); // Obtenemos la lista de IDs campos seguidos
                    if (camposSeguidos != null && !camposSeguidos.isEmpty()) {
                        // Recorremos la lista de campos seguidos
                        for (String campoId : camposSeguidos) {
                            mFirestore.collection("campos").document(campoId).get() // Obtenemos el documento del campo
                                    .addOnCompleteListener(campoTask -> {
                                        if (campoTask.isSuccessful()) {
                                            DocumentSnapshot campoDocument = campoTask.getResult(); // Lo almacenamos
                                            if (campoDocument.exists()) {
                                                String nombreCampo = campoDocument.getString("nombre"); // Obtenemos el nombre del campo
                                                mFirestore.collection("partidas").whereEqualTo("idCampo", campoId).get() // Obtenemos el documento de la partida
                                                        .addOnCompleteListener(partidasTask -> {
                                                            if (partidasTask.isSuccessful()) {
                                                                // Recorremos la lista de partidas del campo
                                                                for (QueryDocumentSnapshot partidaDocument : partidasTask.getResult()) {
                                                                    nuevaFila(partidaDocument, nombreCampo);
                                                                }
                                                            } else {
                                                                Toast.makeText(getContext(), getResources().getString(R.string.errorCargarPartidas), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.errorDatosUsuario), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.errorDatosUsuario), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método que añade las partidas a la lista
    @SuppressLint("ResourceAsColor")
    private void nuevaFila(QueryDocumentSnapshot partidaDocument, String nombreCampo) {
        // Obtenemos la fecha de inicio y fin de la partida
        String fechaInicio = partidaDocument.getString("fechaInicio");
        String fechaFin = partidaDocument.getString("fechaFin");

        // Formato
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m");

        // Formateamos las fechas de inicio y fin
        LocalDateTime fechaInicioDateTime = LocalDateTime.parse(fechaInicio, formatter);
        LocalDateTime fechaFinDateTime = LocalDateTime.parse(fechaFin, formatter);

        // Formateamos la fecha actual
        String fechaActualStr = LocalDateTime.now().format(formatter);
        LocalDateTime fechaActual = LocalDateTime.parse(fechaActualStr, formatter);

        // Si la fecha actual es anterior a la fecha de inicio...
        if (fechaActual.isBefore(fechaInicioDateTime)) {
            // Obtenemos los datos de la partida
            String partidaId = partidaDocument.getId();
            String nombrePartida = partidaDocument.getString("nombre");
            Long maxJugadoresEquipo = partidaDocument.getLong("maxJugadoresPorEquipo");
            List<String> jugadoresEquipo1 = (List<String>) partidaDocument.get("jugadoresEquipo1");
            List<String> jugadoresEquipo2 = (List<String>) partidaDocument.get("jugadoresEquipo2");

            // CardView
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View partidaView = inflater.inflate(R.layout.item_partida_jugador, tblUnirsePartidas, false);

            // Elementos del layout
            TextView tvNombrePartida = partidaView.findViewById(R.id.tvNombrePartida);
            TextView tvNombreCampo = partidaView.findViewById(R.id.tvNombreCampo);
            TextView tvJugadoresEquipo1 = partidaView.findViewById(R.id.tvJugadoresEquipo1);
            TextView tvJugadoresEquipo2 = partidaView.findViewById(R.id.tvJugadoresEquipo2);
            TextView tvFecha = partidaView.findViewById(R.id.tvFecha);
            Button btnUnirse = partidaView.findViewById(R.id.btnCv);

            // Asignamos los datos al layout del CardView
            tvNombrePartida.setText(nombrePartida);
            tvNombreCampo.setText(nombreCampo);
            tvJugadoresEquipo1.setText(getResources().getString(R.string.equipo1) + ": " + jugadoresEquipo1.size() + " / " + maxJugadoresEquipo);
            tvJugadoresEquipo2.setText(getResources().getString(R.string.equipo2) + ": " + jugadoresEquipo2.size() + " / " + maxJugadoresEquipo);
            tvFecha.setText(fechaInicio + " - " + fechaFin);

            long[] vibraciones = {0, 50, 100, 50}; // Patrón de vibraciones

            if (fechaActual.isAfter(fechaInicioDateTime) && fechaActual.isBefore(fechaFinDateTime)) { // Si la partida está en progreso...
                btnUnirse.setAlpha(0.5f); // Bajamos la transparencia
                btnUnirse.setText(getResources().getString(R.string.enCurso)); // Cambiamos texto
                btnUnirse.setBackgroundResource(R.drawable.estilo_boton_transparente); // Cambiamos fondo
                btnUnirse.setTextColor(R.color.naranja); // Cambiamos el color del texto
                btnUnirse.setOnClickListener(new View.OnClickListener() { // Escuchador para las vibraciones
                    @Override
                    public void onClick(View v) {
                        vibrator.vibrate(vibraciones, -1);
                    }
                });
            } else if ((jugadoresEquipo1.size() + jugadoresEquipo2.size()) >= maxJugadoresEquipo * 2) { // Si las listas de jugadores están completas...
                btnUnirse.setAlpha(0.5f); // Bajamos la transparencia
                btnUnirse.setText(getResources().getString(R.string.partidaCompleta)); // Cambiamos texto
                btnUnirse.setOnClickListener(new View.OnClickListener() { // Escuchador para las vibraciones
                    @Override
                    public void onClick(View v) {
                        vibrator.vibrate(vibraciones, -1);
                    }
                });
            } else if (jugadoresEquipo1.contains(uid) || jugadoresEquipo2.contains(uid)) { // Si el jugador ya se ha unido
                btnUnirse.setAlpha(0.5f); // Bajamos la transparencia
                btnUnirse.setText(getResources().getString(R.string.yaTeHasUnido)); // Cambiamos texto
                btnUnirse.setOnClickListener(new View.OnClickListener() { // Escuchador para las vibraciones
                    @Override
                    public void onClick(View v) {
                        vibrator.vibrate(vibraciones, -1);
                    }
                });
            } else {
                btnUnirse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unirseAPartida(partidaId, nombrePartida);
                    }
                });
            }

            tblUnirsePartidas.addView(partidaView); // Añadimos el cardview a la lista

            // En este punto se ha añadido al menos un cardview, así que mostramos la lista
            tvNoPartidas.setVisibility(View.GONE);
            tblUnirsePartidas.setVisibility(View.VISIBLE);
            svNuevasPartidas.setVisibility(View.VISIBLE);
        } else {
            return;
        }


    }

    // Método que incluye al jugador en la partida
    private void unirseAPartida(String partidaId, String nombrePartida) {
        DocumentReference partidaRef = mFirestore.collection("partidas").document(partidaId); // Obtenemos el documento de la partida
        partidaRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot partidaDocument = task.getResult(); // Lo almacenamos
                if (partidaDocument.exists()) {
                    Long maxJugadoresEquipo = partidaDocument.getLong("maxJugadoresPorEquipo"); // Obtenemos el número máximo de jugadores por equipo
                    List<String> jugadoresEquipo1 = (List<String>) partidaDocument.get("jugadoresEquipo1"); // Lista de jugadores del equipo 1
                    List<String> jugadoresEquipo2 = (List<String>) partidaDocument.get("jugadoresEquipo2"); // Lista de jugadores del equipo 2

                    // AlertDialog donde el usuario podrá elegir equipo
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setTitle(nombrePartida)
                            .setMessage(getResources().getString(R.string.quieresUnirtePartida));

                    // Comprobamos que hay espacio en los equipos y añadimos el botón para cada uno en caso positivo
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

                    // Mostramos el AlertDialog
                    builder.setNeutralButton(getResources().getString(R.string.cancelar), null);
                    builder.show();
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.errorCargarPartidas), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.errorCargarPartidas), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método que se encarga de incluir al usuario dentro de la partida
    private void anyadirJugadorAEquipo(String partidaId, String equipo) {
        DocumentReference partidaRef = mFirestore.collection("partidas").document(partidaId); // Obtenemos el documento de la partida
        DocumentReference usuarioRef = mFirestore.collection("usuarios").document(uid); // Obtenemos el documento del usuario

        partidaRef.update(equipo, FieldValue.arrayUnion(uid)) // Actualizamos el documento de la partida pasándole el equipo donde debe incluir el usuario
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                usuarioRef.update("partidas", FieldValue.arrayUnion(partidaId));
                            }
                        });
    }
}
