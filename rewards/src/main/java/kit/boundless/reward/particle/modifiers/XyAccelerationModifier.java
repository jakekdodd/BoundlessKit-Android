package kit.boundless.reward.particle.modifiers;

import android.view.animation.Interpolator;
import kit.boundless.reward.particle.Particle;

/**
 * The type Xy acceleration modifier.
 */
public class XyAccelerationModifier implements ParticleModifier {

  /**
   * The Enabled.
   */
  public boolean enabled = true;
  /**
   * The M y initial value.
   */
  public float mYInitialValue;
  /**
   * The M y final value.
   */
  public float mYFinalValue;
  /**
   * The M y value increment.
   */
  public float mYValueIncrement;
  private float mXInitialValue;
  private float mXFinalValue;
  private long mStartTime;
  private long mDuration;
  private float mXValueIncrement;
  private Interpolator mInterpolator;

  /**
   * Instantiates a new Xy acceleration modifier.
   *
   * @param xInitialValue the x initial value
   * @param yInitialValue the y initial value
   * @param xFinalValue the x final value
   * @param yFinalValue the y final value
   * @param startMillis the start millis
   * @param duration the duration
   * @param interpolator the interpolator
   */
  public XyAccelerationModifier(
      float xInitialValue,
      float yInitialValue,
      float xFinalValue,
      float yFinalValue,
      long startMillis,
      long duration,
      Interpolator interpolator) {
    mXInitialValue = xInitialValue;
    mYInitialValue = yInitialValue;
    mXFinalValue = xFinalValue;
    mYFinalValue = yFinalValue;
    mStartTime = startMillis;
    mDuration = duration;
    mXValueIncrement = mXFinalValue - mXInitialValue;
    mYValueIncrement = mYFinalValue - mYInitialValue;
    mInterpolator = interpolator;
  }

  @Override
  public void apply(Particle particle, long milliseconds) {
    if (!enabled) {
      return;
    }
    if (milliseconds < mStartTime) {
      particle.mAccelerationX = mXInitialValue;
      particle.mAccelerationY = mYInitialValue;
    } else if (mDuration != -1 && milliseconds > (mStartTime + mDuration)) {
      particle.mAccelerationX = mXFinalValue;
      particle.mAccelerationY = mYFinalValue;
    } else {
      float interpolatedValue = mInterpolator.getInterpolation(
          (milliseconds - mStartTime) * 1f / (mDuration == -1 ? particle.mTimeToLive : mDuration));

      particle.mAccelerationX = mXInitialValue + mXValueIncrement * interpolatedValue;
      particle.mAccelerationY = mYInitialValue + mYValueIncrement * interpolatedValue;
    }
  }

}
