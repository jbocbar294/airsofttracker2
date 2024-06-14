package iesmm.pmdm.airsofttracker2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OwnerFragmentAddPartida extends Fragment {

    private EditText etNombrePartida, etMaxJugadores, etFechaInicio, etFechaFin;
    private Button btnGuardarPartida;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String uidCampo;
    private SimpleDateFormat dateTimeFormat;

    public OwnerFragmentAddPartida() {
        // Required empty public constructor
    }

    public static OwnerFragmentAddPartida newInstance() {
        return new OwnerFragmentAddPartida();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Instancia de Firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uidCampo = mAuth.getCurrentUser().getUid(); // Obtener el UID del usuario actual
        dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // Formato de fecha y hora
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_duenyo_anyadir_partida, container, false);

        // Asignar objetos
        etNombrePartida = view.findViewById(R.id.etNombrePartida);
        etMaxJugadores = view.findViewById(R.id.etMaxJugadores);
        etFechaInicio = view.findViewById(R.id.etFechaInicio);
        etFechaFin = view.findViewById(R.id.etFechaFin);
        btnGuardarPartida = view.findViewById(R.id.btnGuardarPartida);

        // Configuración del DateTimePicker para la fecha de inicio
        etFechaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTimePicker(etFechaInicio);
            }
        });

        // Configuración del DateTimePicker para la fecha de fin
        etFechaFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTimePicker(etFechaFin);
            }
        });

        // Escuchador del botón para guardar la nueva partida
        btnGuardarPartida.setOnClickListener(v -> registrarNuevaPartida());

        return view;
    }

    // Método para mostrar el DateTimePicker
    private void dateTimePicker(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (timeView, hourOfDay, minute1) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year1, month1, dayOfMonth, hourOfDay, minute1);
                String formattedDate = dateTimeFormat.format(selectedDate.getTime());
                editText.setText(formattedDate); // Asignar la fecha seleccionada al EditText
            }, hour, minute, true);
            timePickerDialog.show();
        }, year, month, day);

        datePickerDialog.show();
    }

    // Método para registrar una nueva partida
    private void registrarNuevaPartida() {
        String nombre = etNombrePartida.getText().toString();
        String maxJugadoresStr = etMaxJugadores.getText().toString();
        String fechaInicioStr = etFechaInicio.getText().toString();
        String fechaFinStr = etFechaFin.getText().toString();

        // Verificar que todos los campos estén completos
        if (nombre.isEmpty() || maxJugadoresStr.isEmpty() || fechaInicioStr.isEmpty() || fechaFinStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Date fechaInicio = dateTimeFormat.parse(fechaInicioStr);
            Date fechaFin = dateTimeFormat.parse(fechaFinStr);

            // Verificar que la fecha de inicio sea anterior a la fecha de fin
            if (fechaInicio.after(fechaFin)) {
                Toast.makeText(getContext(), "La fecha de inicio debe ser antes que la fecha de fin", Toast.LENGTH_SHORT).show();
                return;
            }

            int maxJugadores = Integer.parseInt(maxJugadoresStr);
            String idCampo = uidCampo;

            // Crear un mapa con los datos de la partida
            Map<String, Object> partida = new HashMap<>();
            partida.put("nombre", nombre);
            partida.put("maxJugadoresPorEquipo", maxJugadores);
            partida.put("fechaInicio", fechaInicioStr);
            partida.put("fechaFin", fechaFinStr);
            partida.put("idCampo", idCampo);
            partida.put("jugadoresEquipo1", new ArrayList<String>());
            partida.put("jugadoresEquipo2", new ArrayList<String>());
            partida.put("ganador", "");

            // Guardar la partida en Firestore
            mFirestore.collection("partidas").add(partida)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Partida guardada", Toast.LENGTH_SHORT).show();
                        NavController navController = Navigation.findNavController(getView());
                        navController.navigate(R.id.action_pageAddPartida_to_pageMisPartidas); // Navegar a la pantalla de partidas
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al guardar la partida", Toast.LENGTH_SHORT).show();
                    });

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Formato de fecha incorrecto", Toast.LENGTH_SHORT).show();
        }
    }
}
