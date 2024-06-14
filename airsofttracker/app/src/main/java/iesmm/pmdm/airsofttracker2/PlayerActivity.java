package iesmm.pmdm.airsofttracker2;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PlayerActivity extends AppCompatActivity {

    private TextView tvUsuario, tvInfo;
    private ProgressDialog progressDialog;
    private ImageButton btnInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_player);

        // Asignar objetos a las vistas
        tvUsuario = findViewById(R.id.tv1);
        tvUsuario.setText(getResources().getString(R.string.partidas));
        tvInfo = findViewById(R.id.tvInfoJugador);
        tvInfo.setText(R.string.infoFragmentsPartidasJugador);
        btnInfo = findViewById(R.id.btnInfoJugador);

        // Configuración del botón de información
        btnInfo.setOnClickListener(v -> {
            if (tvInfo.getVisibility() == View.GONE) {
                expandirInfo(true);
            } else {
                expandirInfo(false);
            }
        });

        // Configuración del ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.cargando));
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Configurar navegación
        setUpNavegacion();

        // Ocultar ProgressDialog después de 2 segundos
        new Handler().postDelayed(() -> progressDialog.dismiss(), 2000);
    }

    // Método para configurar la navegación
    private void setUpNavegacion() {
        // Configuración del BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view_jugador);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_jugador);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Configuración del escuchador de selección de ítems
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                String title = item.getTitle().toString();
                tvUsuario.setText(title);

                if (title.trim().equals(getResources().getString(R.string.partidas))) {
                    navController.navigate(R.id.pageMisPartidasJugador);
                    tvInfo.setText(getResources().getString(R.string.infoFragmentsPartidasJugador));
                } else if (title.trim().equals(getResources().getString(R.string.estadisticas))) {
                    navController.navigate(R.id.pageEstadisticas);
                    tvInfo.setText(getResources().getString(R.string.infoFragmentsEstadisticas));
                } else if (title.trim().equals(getResources().getString(R.string.perfil))) {
                    navController.navigate(R.id.pagePerfil);
                    tvInfo.setText(getResources().getString(R.string.infoFragmentPerfil));
                }
                return true;
            }
        });
    }

    // Método para expandir o contraer la información
    private void expandirInfo(boolean expand) {
        float inicioTamanyo = expand ? 24 : 32;
        float finTamanyo = expand ? 32 : 24;
        float inicioAlpha = expand ? 0f : 1f;
        float finAlpha = expand ? 1f : 0f;

        // Animación del tamaño del texto
        ValueAnimator animatorTamanyo = ValueAnimator.ofFloat(inicioTamanyo, finTamanyo);
        animatorTamanyo.setDuration(300);
        animatorTamanyo.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                tvUsuario.setTextSize(TypedValue.COMPLEX_UNIT_DIP, animatedValue);
            }
        });

        // Animación de la opacidad del texto de información
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(tvInfo, "alpha", inicioAlpha, finAlpha);

        // Animación de la rotación de la flecha
        float startRotation = expand ? 0f : 180f;
        float endRotation = expand ? 180f : 0f;
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(btnInfo, "rotation", startRotation, endRotation);

        // Configurar y ejecutar el conjunto de animadores
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorTamanyo, alphaAnimator, rotationAnimator);
        animatorSet.setDuration(300);
        animatorSet.start();

        // Mostrar u ocultar el texto de información
        if (expand) {
            tvInfo.setVisibility(View.VISIBLE);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvInfo.setVisibility(View.GONE);
                }
            }, 300);
        }
    }
}
