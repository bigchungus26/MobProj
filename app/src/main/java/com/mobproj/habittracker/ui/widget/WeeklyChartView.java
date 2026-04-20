package com.mobproj.habittracker.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.mobproj.habittracker.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeeklyChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private int[] counts = new int[7];
    private String[] labels = new String[7];
    private int maxValue = 1;

    public WeeklyChartView(Context context) {
        this(context, null);
    }

    public WeeklyChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeeklyChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int primary = MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorPrimary,
                ContextCompat.getColor(context, R.color.colorPrimary));
        int outline = MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOutline,
                ContextCompat.getColor(context, R.color.colorPrimary));
        int onSurfaceVariant = MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurfaceVariant,
                0xFF5A5C66);

        barPaint.setColor(primary);
        barPaint.setStyle(Paint.Style.FILL);
        emptyBarPaint.setColor(withAlpha(outline, 0x33));
        emptyBarPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(onSurfaceVariant);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(spToPx(12));

        valuePaint.setColor(onSurfaceVariant);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(spToPx(11));
    }

    public void setData(int[] counts) {
        if (counts == null || counts.length != 7) {
            this.counts = new int[7];
        } else {
            this.counts = counts;
        }
        int max = 1;
        for (int c : this.counts) {
            if (c > max) max = c;
        }
        this.maxValue = max;

        SimpleDateFormat dayFmt = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            labels[i] = dayFmt.format(cal.getTime()).substring(0, Math.min(3, dayFmt.format(cal.getTime()).length()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) dpToPx(140);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@androidx.annotation.NonNull Canvas canvas) {
        super.onDraw(canvas);
        float labelHeight = spToPx(16);
        float valueHeight = spToPx(14);
        float paddingX = dpToPx(8);
        float availableWidth = getWidth() - paddingX * 2;
        float barSlot = availableWidth / 7f;
        float barWidth = Math.min(dpToPx(28), barSlot * 0.7f);
        float chartTop = valueHeight + dpToPx(4);
        float chartBottom = getHeight() - labelHeight - dpToPx(4);
        float chartHeight = chartBottom - chartTop;
        float cornerRadius = dpToPx(4);

        for (int i = 0; i < 7; i++) {
            float centerX = paddingX + barSlot * i + barSlot / 2f;
            float left = centerX - barWidth / 2f;
            float right = centerX + barWidth / 2f;
            float ratio = maxValue == 0 ? 0f : (float) counts[i] / maxValue;
            float barHeight = chartHeight * ratio;
            float barTop = chartBottom - barHeight;

            rect.set(left, chartTop, right, chartBottom);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, emptyBarPaint);

            if (barHeight > 0f) {
                rect.set(left, barTop, right, chartBottom);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint);
            }

            if (counts[i] > 0) {
                canvas.drawText(String.valueOf(counts[i]),
                        centerX, barTop - dpToPx(4), valuePaint);
            }
            canvas.drawText(labels[i] != null ? labels[i] : "",
                    centerX, getHeight() - dpToPx(2), labelPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
