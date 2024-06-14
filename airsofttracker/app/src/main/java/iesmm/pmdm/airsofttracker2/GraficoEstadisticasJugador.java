package iesmm.pmdm.airsofttracker2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.core.content.ContextCompat;

public class GraficoEstadisticasJugador extends View {

    private Paint paintGanadas;  // Pintura para las partidas ganadas
    private Paint paintPerdidas;  // Pintura para las partidas perdidas
    private Paint paintTexto;  // Pintura para el texto
    private int totalPartidas;  // Total de partidas
    private int partidasGanadas;  // Número de partidas ganadas

    public GraficoEstadisticasJugador(Context context) {
        super(context);
        init(context);
    }

    public GraficoEstadisticasJugador(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GraficoEstadisticasJugador(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        // Configuración de la pintura para las partidas ganadas
        paintGanadas = new Paint();
        paintGanadas.setColor(ContextCompat.getColor(context, R.color.primario));
        paintGanadas.setStyle(Paint.Style.STROKE);
        paintGanadas.setStrokeWidth(100);

        // Configuración de la pintura para las partidas perdidas
        paintPerdidas = new Paint();
        paintPerdidas.setColor(ContextCompat.getColor(context, R.color.primario_oscuro));
        paintPerdidas.setStyle(Paint.Style.STROKE);
        paintPerdidas.setStrokeWidth(100);

        // Configuración de la pintura para el texto
        paintTexto = new Paint();
        paintTexto.setColor(ContextCompat.getColor(context, R.color.texto_primario));
        paintTexto.setTextSize(150);
        paintTexto.setTextAlign(Paint.Align.CENTER);
    }

    // Método para establecer el número de partidas totales y ganadas
    public void setPartidas(int totalPartidas, int partidasGanadas) {
        this.totalPartidas = totalPartidas;
        this.partidasGanadas = partidasGanadas;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Si no hay partidas, no dibujamos nada
        if (totalPartidas == 0) {
            return;
        }

        // Calculamos el centro y el radio del círculo
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        float radius = Math.min(centerX, centerY) - 50;
        RectF rect = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Calculamos los ángulos para las partidas ganadas y perdidas
        float anguloGanadas = (partidasGanadas / (float) totalPartidas) * 360;
        float anguloPerdidas = 360 - anguloGanadas;

        // Dibujamos los arcos para las partidas ganadas y perdidas
        canvas.drawArc(rect, -90, anguloGanadas, false, paintGanadas);
        canvas.drawArc(rect, -90 + anguloGanadas, anguloPerdidas, false, paintPerdidas);

        // Dibujamos el texto del total de partidas en negrita
        paintTexto.setFakeBoldText(true);
        canvas.drawText(String.valueOf(totalPartidas), centerX, centerY, paintTexto);

        // Dibujamos el texto "partidas jugadas" en tamaño normal
        paintTexto.setFakeBoldText(false);
        paintTexto.setTextSize(50);
        canvas.drawText(getResources().getString(R.string.partidasJugadas), centerX, (float) (getHeight() / 1.65), paintTexto);
    }
}
