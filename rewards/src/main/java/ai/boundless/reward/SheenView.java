package ai.boundless.reward;

import ai.boundless.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

/**
 * Displays a shiny effect over a view.
 * Place a SheenView in a RelativeLayout next to the view to sheen over (i.e.
 * kit.boundless.R.layout#sheen_layout_sample).
 * Custom attributes can be found at "kit.boundless.R.attr.SheenView".
 */
public class SheenView extends android.support.v7.widget.AppCompatImageView {

  private final Paint maskPaint;
  private final Paint imagePaint;
  /**
   * The Frames per second.
   */
  public int framesPerSecond = 30;
  /**
   * The Animation duration.
   */
  public long animationDuration = 3300;
  /**
   * The Animate right to left.
   */
  public boolean animateRightToLeft = false;
  /**
   * The Flip sheen image.
   */
  public boolean flipSheenImage = false;
  /**
   * The Interpolator.
   */
  public Interpolator interpolator = new AccelerateDecelerateInterpolator();
  /**
   * The Start time.
   */
  long startTime;
  /**
   * The Animate over view id.
   */
  int animateOverViewId = 0;
  private Bitmap mImage;
  private Bitmap mMask;

  /**
   * Instantiates a new Sheen view.
   *
   * @param context the context
   * @param attrs the attrs
   */
  public SheenView(Context context, AttributeSet attrs) {
    super(context, attrs);

    setVisibility(GONE);

    maskPaint = new Paint();
    imagePaint = new Paint();
    imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

    setLayerType(LAYER_TYPE_SOFTWARE, null);

    TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SheenView, 0, 0);
    try {
      framesPerSecond = attributes.getInt(R.styleable.SheenView_framesPerSecond, framesPerSecond);
      animationDuration =
          attributes.getInt(R.styleable.SheenView_animationDuration, (int) animationDuration);
      animateRightToLeft =
          attributes.getBoolean(R.styleable.SheenView_animateRightToLeft, animateRightToLeft);
      flipSheenImage =
          attributes.getBoolean(R.styleable.SheenView_imageHorizontalFlip, flipSheenImage);
      animateOverViewId =
          attributes.getResourceId(R.styleable.SheenView_animateOver, animateOverViewId);
    } finally {
      attributes.recycle();
    }
  }

  /**
   * Sets animate over view id.
   *
   * @param viewId The sibling view to apply the sheen effect over.
   */
  public void setAnimateOverViewId(int viewId) {
    animateOverViewId = viewId;
  }

  /**
   * Begins sheen animation.
   */
  public void start() {
    long now = System.currentTimeMillis();
    if (now >= startTime + animationDuration && updateLayout() && updateMask()) {
      this.startTime = now;
      setVisibility(VISIBLE);
      this.postInvalidate();
    }
  }

  /**
   * Update layout boolean.
   *
   * @return the boolean
   */
  protected boolean updateLayout() {
    if (!(getLayoutParams() instanceof RelativeLayout.LayoutParams)) {
      Log.e(
          "Boundless",
          "SheenView must be in a RelativeLayout. If using a different layout class, wrap "
              + "SheenView and the view to animate over in a relative layout."
      );
      return false;
    }

    if (((ViewGroup) getParent()).findViewById(animateOverViewId) == null) {
      Log.e(
          "Boundless",
          "View to animate over, with id:<" + animateOverViewId
              + ">, is not a sibling of SheenView. Must be a sibling."
      );
      return false;
    }

    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, animateOverViewId);
    layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, animateOverViewId);
    layoutParams.addRule(RelativeLayout.ALIGN_START, animateOverViewId);
    layoutParams.addRule(RelativeLayout.ALIGN_END, animateOverViewId);
    setLayoutParams(layoutParams);
    return true;
  }

  /**
   * Update mask boolean.
   *
   * @return the boolean
   */
  protected boolean updateMask() {
    View view = ((ViewGroup) getParent()).findViewById(animateOverViewId);
    if (view == null) {
      Log.e(
          "Boundless",
          "View to animate over, with id:<" + animateOverViewId
              + ">, is not a sibling of SheenView. Must be a sibling."
      );
      return false;
    }

    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Drawable bgDrawable = view.getBackground();
    if (bgDrawable != null) {
      bgDrawable.draw(canvas);
    }
    view.draw(canvas);

    mMask = Bitmap.createBitmap(bitmap);

    if (mMask == null) {
      return false;
    }

    setImage(getResources(), R.drawable.sheen);
    return true;
  }

  /**
   * Sets image.
   *
   * @param res the res
   * @param id the id
   */
  protected void setImage(Resources res, int id) {
    mImage = BitmapFactory.decodeResource(res, id);
    if (flipSheenImage) {
      mImage = horizontalFlip(mImage);
    }
    mImage = resize(mImage, mMask.getHeight());
  }

  private static Bitmap horizontalFlip(Bitmap src) {
    Matrix matrix = new Matrix();
    matrix.preScale(-1.0f, 1.0f);
    return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
  }

  private static Bitmap resize(Bitmap image, int maxHeight) {
    if (maxHeight > 0) {
      int width = image.getWidth();
      int height = image.getHeight();
      float ratio = (float) maxHeight / (float) height;

      int finalWidth = (int) (ratio * width);
      image = Bitmap.createScaledBitmap(image, finalWidth, maxHeight, true);
      return image;
    } else {
      return image;
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    long elapsedTime = System.currentTimeMillis() - startTime;

    if (elapsedTime < animationDuration) {
      this.postInvalidateDelayed(1000 / framesPerSecond);
      float interpolation = interpolator.getInterpolation(elapsedTime * 1f / animationDuration);
      float xPos = mImage.getWidth() * (animateRightToLeft
          ? (-2 * interpolation + 1)
          : (2 * interpolation - 1));

      canvas.save();
      canvas.drawBitmap(mMask, 0, 0, maskPaint);
      canvas.drawBitmap(mImage, xPos, 0, imagePaint);
      canvas.restore();
    } else {
      setVisibility(GONE);
    }
  }
}
