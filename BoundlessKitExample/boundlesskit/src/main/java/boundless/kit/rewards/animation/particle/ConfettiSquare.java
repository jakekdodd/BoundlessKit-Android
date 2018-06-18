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

public class ConfettiSquare extends Drawable {

    private int height;
    private int width;
    private Path path;
    private Paint strokePaint;

    public ConfettiSquare(int width, int height, int color) {
        super();
        this.height = height;
        this.width = width;
        Random random = new Random();
        int offset = (int) (width / (random.nextInt(7) + 1f));

        path = new Path();
        path.moveTo(offset, 0);
        path.lineTo(width, 0);
        path.lineTo(width - offset, height);
        path.lineTo(0, height);
        path.close();

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(color);
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
