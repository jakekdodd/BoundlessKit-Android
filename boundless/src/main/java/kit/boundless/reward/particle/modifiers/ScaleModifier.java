package kit.boundless.reward.particle.modifiers;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import kit.boundless.reward.particle.Particle;

/**
 * The type Scale modifier.
 */
public class ScaleModifier implements ParticleModifier {

  private float mInitialValue;
  private float mFinalValue;
  private long mEndTime;
  private long mStartTime;
  private long mDuration;
  private float mValueIncrement;
  private Interpolator mInterpolator;

  /**
   * Instantiates a new Scale modifier.
   *
   * @param initialValue the initial value
   * @param finalValue the final value
   * @param startMillis the start millis
   * @param endMillis the end millis
   */
  public ScaleModifier(float initialValue, float finalValue, long startMillis, long endMillis) {
    this(initialValue, finalValue, startMillis, endMillis, new LinearInterpolator());
  }

  /**
   * Instantiates a new Scale modifier.
   *
   * @param initialValue the initial value
   * @param finalValue the final value
   * @param startMillis the start millis
   * @param endMillis the end millis
   * @param interpolator the interpolator
   */
  public ScaleModifier(
      float initialValue,
      float finalValue,
      long startMillis,
      long endMillis,
      Interpolator interpolator) {
    mInitialValue = initialValue;
    mFinalValue = finalValue;
    mStartTime = startMillis;
    mEndTime = endMillis;
    mDuration = mEndTime - mStartTime;
    mValueIncrement = mFinalValue - mInitialValue;
    mInterpolator = interpolator;
  }

  @Override
  public void apply(Particle particle, long milliseconds) {
    if (milliseconds < mStartTime) {
      particle.mScale = mInitialValue;
    } else if (milliseconds > mEndTime) {
      particle.mScale = mFinalValue;
    } else {
      float interpolaterdValue =
          mInterpolator.getInterpolation((milliseconds - mStartTime) * 1f / mDuration);
      float newScale = mInitialValue + mValueIncrement * interpolaterdValue;
      particle.mScale = newScale;
    }
  }

}
