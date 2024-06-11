package iesmm.pmdm.airsofttracker2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OwnerActivity extends AppCompatActivity {

    private String usuario;
    private TextView tvUsuario;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_owner);

        sharedPreferences = getSharedPreferences("configuraciones", Context.MODE_PRIVATE);

        tvUsuario = findViewById(R.id.tv1);
        usuario = sharedPreferences.getString("usuario", "");
        tvUsuario.setText(usuario);

        // Muestra el círculo de carga durante 2 segundos
        cargando();
        new Handler().postDelayed(() -> {
            quitarCargando();
            setUpNavegacion();
        }, 2000);
    }

    private void setUpNavegacion() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view_duenyo);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_duenyo);
        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());
    }

    private void cargando() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void quitarCargando() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
