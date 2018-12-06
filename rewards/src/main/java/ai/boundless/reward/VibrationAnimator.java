package ai.boundless.reward;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * A vibration animation is the combination of {@link PulseAnimator} and {@link ShimmyAnimator}.
 * First the view is scaled, then translated, then descaled.
 */
public class VibrationAnimator extends BaseViewAnimator<VibrationAnimator> {

  private float shimmyTranslation = 50;
  private int shimmyCount = 6;
  private long shimmyDuration = 1000;
  private boolean shimmyVertically = false;
  private float scale = 0.8f;
  private int scaleCount = 1;
  private long scaleDuration = 150;


  /**
   * Sets shimmy translation.
   *
   * @param translation The number of points to shimmy in a single direction.     To start
   *     animating in the opposite direction, pass a negative value.
   * @return The object used for Constructor Chaining
   */
  public VibrationAnimator setShimmyTranslation(long translation) {
    this.shimmyTranslation = translation;
    return this;
  }

  /**
   * Sets the number of shimmies that take place, after scaling.
   *
   * @param count The number of translation shimmies.
   * @return The object used for Constructor Chaining
   */
  public VibrationAnimator setShimmyCount(int count) {
    this.shimmyCount = count;
    return this;
  }

  /**
   * Sets the time taken to shimmy.
   * To set the pulse duration, use {@link #setScaleDuration(long)}.
   *
   * @param duration The time, in milliseconds, to complete all translations.
   * @return The object used for Constructor Chaining
   */
  public VibrationAnimator setShimmyDuration(long duration) {
    this.shimmyDuration = duration;
    return this;
  }

  /**
   * The default value of {@link #shimmyVertically} is false.
   *
   * @param shimmyVertically If true, translation will occur along the Y-axis. If false,
   *     translation will occur along the X-axis.
   * @return The object used for Constructor Chaining
   */
  public VibrationAnimator setShimmyVertically(boolean shimmyVertically) {
    this.shimmyVertically = shimmyVertically;
    return this;
  }

  /**
   * Sets the value for scaling. The view is scaled, translated, and then descaled.
   * If the scale is between 0 and 1f, this will be like zooming out.
   * If the scale is greater than 1f, this will be like zooming in.
   *
   * @param scale The factor to scale the view by.
   * @return The object used for Constructor Chaining
   */
  public VibrationAnimator setScale(float scale) {
    this.scale = scale;
    return this;
  }

  /**
   * Sets the number of scaling pulses before and after shimmy.
   * To change the {@link #scale} value, use {@link #setScale(float)}.
   *
   * @param count The number of scaling pulses.
   * @return The object used for Constructor Chaining
   */
  public VibrationAnimator setScaleCount(int count) {
    this.scaleCount = count;
    return this;
  }

  /**
   * Sets the time taken to scale.
   * To set the shimmy duration, use {@link #setShimmyDuration(long)}.
   *
   * @param duration The time, in milliseconds, to complete scaling pulses.
   * @return The object used for Constructor Chaining
   */
  public VibrationAnimator setScaleDuration(long duration) {
    this.scaleDuration = duration;
    return this;
  }

  @Override
  public VibrationAnimator setTarget(View target) {
    float[] values = new float[scaleCount * 2];
    float[] reversedValues = new float[scaleCount * 2];
    for (int i = reversedValues.length - 1; i >= 0; i--) {
      if (i % 2 == 0) {
        values[i] = 1f;
        reversedValues[i] = scale;
      } else {
        values[i] = scale;
        reversedValues[i] = 1f;
      }
    }
    AnimatorSet zoomSet = new AnimatorSet();
    AnimatorSet unzoomSet = new AnimatorSet();
    zoomSet.playTogether(
        ObjectAnimator.ofFloat(target, "scaleY", values),
        ObjectAnimator.ofFloat(target, "scaleX", values)
    );
    unzoomSet.playTogether(
        ObjectAnimator.ofFloat(target, "scaleY", reversedValues),
        ObjectAnimator.ofFloat(target, "scaleX", reversedValues)
    );

    ShimmyAnimator shimmyAnimator = new ShimmyAnimator().setCount(shimmyCount)
        .setTranslation(shimmyTranslation)
        .setVertically(shimmyVertically)
        .setTarget(target);

    zoomSet.setDuration(scaleDuration);
    shimmyAnimator.setStartDelay(zoomSet.getDuration() * 8 / 10);
    shimmyAnimator.setDuration(shimmyDuration);
    unzoomSet.setStartDelay(shimmyAnimator.getStartDelay() + shimmyAnimator.getDuration());
    unzoomSet.setDuration(scaleDuration);

    getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
    getAnimator().playTogether(zoomSet, shimmyAnimator.getAnimator(), unzoomSet);

    return this;
  }
}
