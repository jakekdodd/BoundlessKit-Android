package boundless.kit.rewards.animation.particle;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Random;

public class ConfettoDrawable extends Drawable {

    public enum Shape {
        RECTANGLE, SPIRAL, CIRCLE
    }

    private Shape shape;
    private int height;
    private int width;
    private Path path;
    private Paint strokePaint;

    public ConfettoDrawable(Shape shape, int width, int height, final int color) {
        super();
        this.shape = shape;
        this.height = height;
        this.width = width;
        strokePaint = new Paint() {{
            setFlags(Paint.ANTI_ALIAS_FLAG);
            setColor(color);
        }};
        path = new Path();

        switch (shape) {
            case RECTANGLE:
                Random random = new Random();
                int offset = (int) (width / (random.nextInt(7) + 1f));
                path.moveTo(offset, 0);
                path.lineTo(width, 0);
                path.lineTo(width - offset, height);
                path.lineTo(0, height);
                path.close();
                break;

            case SPIRAL:
                float lineWidth = width / 8f;
                float halfLineWidth = lineWidth / 2f;
                strokePaint.setStrokeWidth(lineWidth);
                strokePaint.setStyle(Paint.Style.STROKE);
                path.moveTo(halfLineWidth, halfLineWidth);
                path.cubicTo(0.25f * width, height - halfLineWidth, 0.75f * width, halfLineWidth, width - halfLineWidth, height - halfLineWidth);
                break;

            case CIRCLE:
                path.addOval(0, 0, width, height, Path.Direction.CW);
                break;
        }

    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPath(path, strokePaint);
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public void setAlpha(int i) {
        if (0 <= i && i <= 255) {
            strokePaint.setAlpha(i);
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
