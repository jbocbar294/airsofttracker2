package iesmm.pmdm.airsofttracker2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class PlayerFragmentEstadisticas extends Fragment {

    public PlayerFragmentEstadisticas() {
        // Required empty public constructor
    }

    public static PlayerFragmentEstadisticas newInstance() {
        return new PlayerFragmentEstadisticas();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jugador_estadisticas, container, false);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText("Fragment Estadísticas");
        return view;
    }
}
