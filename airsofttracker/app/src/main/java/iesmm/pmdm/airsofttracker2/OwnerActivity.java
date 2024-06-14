package iesmm.pmdm.airsofttracker2;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerActivity extends AppCompatActivity {

    private TextView tvUsuario, tvInfo;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private ImageButton btnInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_owner);

        sharedPreferences = getSharedPreferences("configuraciones", Context.MODE_PRIVATE);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvUsuario = findViewById(R.id.tv1);
        tvUsuario.setText(getResources().getString(R.string.campo));
        tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText(R.string.infoFragmentCampoDuenyo);
        btnInfo = findViewById(R.id.btnInfo);

        btnInfo.setOnClickListener(v -> {
            if (tvInfo.getVisibility() == View.GONE) {
                expandirInfo(true);
            } else {
                expandirInfo(false);
            }
        });

        // Muestra el círculo de carga durante 2 segundos
        cargando();
        verificarCampo();
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            setUpNavegacion();
        }, 700);


        setUpNavegacion();
    }

    private void verificarCampo() {
        String uid = mAuth.getCurrentUser().getUid();
        mFirestore.collection("campos").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // El campo existe, proceder con la configuración de navegación
                    new Handler().postDelayed(() -> {
                        quitarCargando();
                    }, 2000);
                } else {
                    // El campo no existe, mostrar mensaje y redirigir
                    quitarCargando();
                    // Redirigir a la actividad para crear un campo
                    Intent intent = new Intent(OwnerActivity.this, FieldCreateActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                // Error al realizar la consulta
                quitarCargando();
                Toast.makeText(OwnerActivity.this, "Error verificando el campo.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpNavegacion() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view_duenyo);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_duenyo);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            String title = item.getTitle().toString();
            tvUsuario.setText(title);

            if (title.trim().equals(getResources().getString(R.string.campo))) {
                navController.navigate(R.id.pageMiCampo);
                tvInfo.setText(getResources().getString(R.string.infoFragmentCampoDuenyo));
            } else if (title.trim().equals(getResources().getString(R.string.partidas))) {
                navController.navigate(R.id.pageMisPartidas);
                tvInfo.setText(getResources().getString(R.string.infoFragmentPartidasDuenyo));
            } else if (title.trim().equals(getResources().getString(R.string.perfil))) {
                navController.navigate(R.id.pagePerfil);
                tvInfo.setText(getResources().getString(R.string.infoFragmentPerfil));
            }
            return true;
        });
    }

    private void cargando() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.cargando));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void quitarCargando() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void expandirInfo(boolean expand) {
        float inicioTamanyo = expand ? 24 : 32;
        float finTamanyo = expand ? 32 : 24;
        float inicioAlpha = expand ? 0f : 1f;
        float finAlpha = expand ? 1f : 0f;

        // Animación del tamaño del texto
        ValueAnimator animatorTamanyo = ValueAnimator.ofFloat(inicioTamanyo, finTamanyo);
        animatorTamanyo.setDuration(300);
        animatorTamanyo.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            tvUsuario.setTextSize(TypedValue.COMPLEX_UNIT_DIP, animatedValue);
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
            new Handler().postDelayed(() -> tvInfo.setVisibility(View.GONE), 300);
        }
    }

}
