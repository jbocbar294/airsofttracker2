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

    private Paint paintGanadas;
    private Paint paintPerdidas;
    private Paint paintTexto;
    private int totalPartidas;
    private int partidasGanadas;

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
        paintGanadas = new Paint();
        paintGanadas.setColor(ContextCompat.getColor(context, R.color.primario));
        paintGanadas.setStyle(Paint.Style.STROKE);
        paintGanadas.setStrokeWidth(100);

        paintPerdidas = new Paint();
        paintPerdidas.setColor(ContextCompat.getColor(context, R.color.primario_oscuro));
        paintPerdidas.setStyle(Paint.Style.STROKE);
        paintPerdidas.setStrokeWidth(100);

        paintTexto = new Paint();
        paintTexto.setColor(ContextCompat.getColor(context, R.color.texto_primario));
        paintTexto.setTextSize(150);
        paintTexto.setTextAlign(Paint.Align.CENTER);
    }

    public void setPartidas(int totalPartidas, int partidasGanadas) {
        this.totalPartidas = totalPartidas;
        this.partidasGanadas = partidasGanadas;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (totalPartidas == 0) {
            return;
        }

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        float radius = Math.min(centerX, centerY) - 50;
        RectF rect = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        float anguloGanadas = (partidasGanadas / (float) totalPartidas) * 360;
        float anguloPerdidas = 360 - anguloGanadas;

        canvas.drawArc(rect, -90, anguloGanadas, false, paintGanadas);
        canvas.drawArc(rect, -90 + anguloGanadas, anguloPerdidas, false, paintPerdidas);

        // Set text to bold for totalPartidas
        paintTexto.setFakeBoldText(true);
        canvas.drawText(String.valueOf(totalPartidas), centerX, centerY, paintTexto);

        // Reset text to normal for "partidas jugadas"
        paintTexto.setFakeBoldText(false);
        paintTexto.setTextSize(50);
        canvas.drawText(getResources().getString(R.string.partidasJugadas), centerX, (float) (getHeight()/1.65), paintTexto);
    }
}
