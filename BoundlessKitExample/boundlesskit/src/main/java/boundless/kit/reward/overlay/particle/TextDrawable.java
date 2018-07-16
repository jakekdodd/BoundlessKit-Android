package boundless.kit.reward.overlay.particle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.TypedValue;

public class TextDrawable extends Drawable {

    private String text;
    private int mIntrinsicWidth;
    private int mIntrinsicHeight;
    private final Paint paint;

    public TextDrawable(Context context, String text, float textSize, int color) {
        this.text = text;

        this.paint = new Paint();
        paint.setColor(color);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                textSize, context.getResources().getDisplayMetrics()));
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        mIntrinsicWidth = (int) (paint.measureText(text));
        mIntrinsicHeight = paint.getFontMetricsInt(null);
    }

    public TextDrawable(String text) {
        this.text = text;

        this.paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        mIntrinsicWidth = (int) (paint.measureText(text));
        mIntrinsicHeight = paint.getFontMetricsInt(null);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        canvas.drawText(text, 0, text.length(), bounds.left, bounds.bottom, paint);
    }
    @Override
    public int getOpacity() {
        return paint.getAlpha();
    }
    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }
    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }
    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }
    @Override
    public void setColorFilter(ColorFilter filter) {
        paint.setColorFilter(filter);
    }
}