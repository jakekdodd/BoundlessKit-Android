package ai.boundless.reward.particle;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import ai.boundless.reward.particle.modifiers.ParticleModifier;

/**
 * The type Particle.
 */
public class Particle {

  /**
   * The M current x.
   */
  public float mCurrentX;
  /**
   * The M current y.
   */
  public float mCurrentY;
  /**
   * The M scale.
   */
  public float mScale = 1f;
  /**
   * The M alpha.
   */
  public int mAlpha = 255;
  /**
   * The M initial rotation.
   */
  public float mInitialRotation = 0f;
  /**
   * The M rotation speed.
   */
  public float mRotationSpeed = 0f;
  /**
   * The M speed x.
   */
  public float mSpeedX = 0f;
  /**
   * The M speed y.
   */
  public float mSpeedY = 0f;
  /**
   * The M acceleration x.
   */
  public float mAccelerationX;
  /**
   * The M acceleration y.
   */
  public float mAccelerationY;
  /**
   * The M time to live.
   */
  public long mTimeToLive;
  /**
   * The M image.
   */
  protected Bitmap mImage;
  /**
   * The M starting millisecond.
   */
  protected long mStartingMillisecond;
  private Matrix mMatrix;
  private Paint mPaint;
  private float mInitialX;
  private float mInitialY;
  private float mRotation;
  private int mBitmapHalfWidth;
  private int mBitmapHalfHeight;

  private List<ParticleModifier> mModifiers;


  /**
   * Instantiates a new Particle.
   *
   * @param bitmap the bitmap
   */
  public Particle(Bitmap bitmap) {
    this();
    mImage = bitmap;
  }

  /**
   * Instantiates a new Particle.
   */
  protected Particle() {
    mMatrix = new Matrix();
    mPaint = new Paint();
  }

  /**
   * Init.
   */
  public void init() {
    mScale = 1;
    mAlpha = 255;
  }

  /**
   * Configure.
   *
   * @param timeToLive the time to live
   * @param emiterX the emiter x
   * @param emiterY the emiter y
   */
  public void configure(long timeToLive, float emiterX, float emiterY) {
    mBitmapHalfWidth = mImage.getWidth() / 2;
    mBitmapHalfHeight = mImage.getHeight() / 2;

    mInitialX = emiterX - mBitmapHalfWidth;
    mInitialY = emiterY - mBitmapHalfHeight;
    mCurrentX = mInitialX;
    mCurrentY = mInitialY;

    mTimeToLive = timeToLive;
  }

  /**
   * Update boolean.
   *
   * @param milliseconds the milliseconds
   * @return the boolean
   */
  public boolean update(long milliseconds) {
    long realMilliseconds = milliseconds - mStartingMillisecond;
    if (realMilliseconds > mTimeToLive) {
      return false;
    }
    mCurrentX = mInitialX + mSpeedX * realMilliseconds
        + mAccelerationX * realMilliseconds * realMilliseconds;
    mCurrentY = mInitialY + mSpeedY * realMilliseconds
        + mAccelerationY * realMilliseconds * realMilliseconds;
    mRotation = mInitialRotation + mRotationSpeed * realMilliseconds / 1000;
    for (int i = 0; i < mModifiers.size(); i++) {
      mModifiers.get(i).apply(this, realMilliseconds);
    }
    return true;
  }

  /**
   * Draw.
   *
   * @param c the c
   */
  public void draw(Canvas c) {
    mMatrix.reset();
    mMatrix.postRotate(mRotation, mBitmapHalfWidth, mBitmapHalfHeight);
    mMatrix.postScale(mScale, mScale, mBitmapHalfWidth, mBitmapHalfHeight);
    mMatrix.postTranslate(mCurrentX, mCurrentY);
    mPaint.setAlpha(mAlpha);
    c.drawBitmap(mImage, mMatrix, mPaint);
  }

  /**
   * Activate particle.
   *
   * @param startingMillisecond the starting millisecond
   * @param modifiers the modifiers
   * @return the particle
   */
  public Particle activate(long startingMillisecond, List<ParticleModifier> modifiers) {
    mStartingMillisecond = startingMillisecond;
    // We do store a reference to the list, there is no need to copy, since the modifiers do not
    // care about states
    mModifiers = modifiers;
    return this;
  }
}
