package iesmm.pmdm.airsofttracker2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class PlayerFragmentEstadisticas extends Fragment {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private GraficoEstadisticasJugador grafico;
    private TextView tvPartidasGanadas;
    private TextView tvPartidasPerdidas;
    private TextView tvPorcentajeVictorias;

    public PlayerFragmentEstadisticas() {
        // Required empty public constructor
    }

    public static PlayerFragmentEstadisticas newInstance() {
        return new PlayerFragmentEstadisticas();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jugador_estadisticas, container, false);

        // Elementos del gráfico
        grafico = view.findViewById(R.id.graficoEstadisticas);
        tvPartidasGanadas = view.findViewById(R.id.tvPartidasGanadas);
        tvPartidasPerdidas = view.findViewById(R.id.tvPartidasPerdidas);
        tvPorcentajeVictorias = view.findViewById(R.id.tvPorcentajeVictorias);

        // Instancias de FireBase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid(); // Id del usuario logeado

        // Obtenemos el documento del usuario
        DocumentReference docRef = mFirestore.collection("usuarios").document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult(); // Lo almacenamos
                if (document.exists()) {
                    // Estadísticas que mostraremos en pantalla
                    long partidasGanadas = document.getLong("partidasGanadas");
                    ArrayList<String> partidas = (ArrayList<String>) document.get("partidas");
                    long totalPartidas = partidas.size();
                    long partidasPerdidas = totalPartidas - partidasGanadas;
                    double porcentajeVictorias = ((double) partidasGanadas / totalPartidas) * 100;

                    // Asignamos valores
                    grafico.setPartidas((int) totalPartidas, (int) partidasGanadas);

                    tvPartidasGanadas.setText(getResources().getString(R.string.victorias) + partidasGanadas);
                    tvPartidasPerdidas.setText(getResources().getString(R.string.derrotas) + partidasPerdidas);

                    // Cambiamos el color del textview en funcion del porcentaje de victorias
                    if (porcentajeVictorias >= 50) {
                        tvPorcentajeVictorias.setTextColor(ContextCompat.getColor(getContext(), R.color.verde));
                    } else {
                        tvPorcentajeVictorias.setTextColor(ContextCompat.getColor(getContext(), R.color.rojo));
                    }
                    tvPorcentajeVictorias.setText(String.format("%.1f", porcentajeVictorias) + " %");
                }
            }
        });

        return view;
    }
}
