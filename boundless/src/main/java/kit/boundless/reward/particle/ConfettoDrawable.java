package kit.boundless.reward.particle;

import java.util.Random;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The type Confetto drawable.
 */
public class ConfettoDrawable extends Drawable {

  private Shape shape;
  private int height;
  private int width;
  private Path path;
  private Paint strokePaint;

  /**
   * Instantiates a new Confetto drawable.
   *
   * @param shape the shape
   * @param width the width
   * @param height the height
   * @param color the color
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ConfettoDrawable(Shape shape, int width, int height, int color) {
    super();
    this.shape = shape;
    this.height = height;
    this.width = width;
    this.path = new Path();
    this.strokePaint = new Paint();
    this.strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    this.strokePaint.setColor(color);

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
        path.cubicTo(
            0.25f * width,
            height - halfLineWidth,
            0.75f * width,
            halfLineWidth,
            width - halfLineWidth,
            height - halfLineWidth
        );
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

  @Override
  public int getIntrinsicWidth() {
    return width;
  }

  @Override
  public int getIntrinsicHeight() {
    return height;
  }

  /**
   * The enum Shape.
   */
  public enum Shape {
    /**
     * Rectangle shape.
     */
    RECTANGLE, /**
     * Spiral shape.
     */
    SPIRAL, /**
     * Circle shape.
     */
    CIRCLE
  }
}
