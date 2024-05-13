package iesmm.pmdm.airsofttracker2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsuario;
    String usuario, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        tvUsuario = findViewById(R.id.tv1);
        tvUsuario.setText(usuario);

        // Recibir datos del Intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            usuario = bundle.getString("usuario");
            uid = bundle.getString("uid");

            // Mostrar el valor recibido en un TextView
            TextView textView = findViewById(R.id.tv1);
            textView.setText(usuario);
        }

    }
}