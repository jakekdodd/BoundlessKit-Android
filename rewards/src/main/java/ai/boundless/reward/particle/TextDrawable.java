package ai.boundless.reward.particle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.TypedValue;

/**
 * The type Text drawable.
 */
public class TextDrawable extends Drawable {

  private final Paint paint;
  private String text;
  private int mIntrinsicWidth;
  private int mIntrinsicHeight;

  /**
   * Instantiates a new Text drawable.
   *
   * @param context the context
   * @param text the text
   * @param textSize the text size
   * @param color the color
   */
  public TextDrawable(Context context, String text, float textSize, int color) {
    this.text = text;

    this.paint = new Paint();
    paint.setColor(color);
    paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
        textSize,
        context.getResources().getDisplayMetrics()
    ));
    paint.setAntiAlias(true);
    paint.setFakeBoldText(true);
    paint.setStyle(Paint.Style.FILL);
    paint.setTextAlign(Paint.Align.LEFT);
    mIntrinsicWidth = (int) (paint.measureText(text));
    mIntrinsicHeight = paint.getFontMetricsInt(null);
  }

  /**
   * Instantiates a new Text drawable.
   *
   * @param text the text
   */
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
    canvas.drawText(text, 0, text.length(), bounds.left, (int) (bounds.bottom * 0.8), paint);
  }

  @Override
  public void setAlpha(int alpha) {
    paint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter filter) {
    paint.setColorFilter(filter);
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
}
