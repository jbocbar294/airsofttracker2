package iesmm.pmdm.airsofttracker2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlayerActivity extends AppCompatActivity {

    private String usuario;
    private TextView tvUsuario;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_player);

        sharedPreferences = getSharedPreferences("configuraciones", Context.MODE_PRIVATE);

        tvUsuario = findViewById(R.id.tv1);
        usuario = sharedPreferences.getString("usuario", "");
        tvUsuario.setText(usuario);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            setUpNavegacion();
        }, 2000);
    }

    private void setUpNavegacion() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view_jugador);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_jugador);
        if (navHostFragment != null) {
            NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());
        }
    }


}
