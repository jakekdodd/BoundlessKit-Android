package kit.boundless.reward;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * A pulse animation oscillates between the view's original scale and another magnitude.
 */
public class PulseAnimator extends BaseViewAnimator<PulseAnimator> {

  private int count = 2;
  private float scale = 1.4f;

  {
    setDuration(2500);
  }

  /**
   * Sets the number of scaling pulses.
   * A single pulse includes scaling from [1f, {@link #scale}, 1f].
   * To change the {@link #scale} value, use {@link #setScale(float)}.
   *
   * @param count The number of scaling pulses
   * @return The object used for Constructor Chaining
   */
  public PulseAnimator setCount(int count) {
    this.count = count;
    return this;
  }

  /**
   * Sets the scale for pulse.
   * If the scale is between 0 and 1f, this will be like zooming out.
   * If the scale is greater than 1f, this will be like zooming in.
   *
   * @param scale The factor to scale the view by.
   * @return The object used for Constructor Chaining
   */
  public PulseAnimator setScale(float scale) {
    this.scale = scale;
    return this;
  }

  @Override
  public PulseAnimator setTarget(View target) {
    float[] values = new float[count * 2 + 1];
    for (int i = values.length - 1; i >= 0; i--) {
      values[i] = (i % 2 == 0) ? 1f : scale;
    }

    getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
    getAnimator().playTogether(
        ObjectAnimator.ofFloat(target, "scaleY", values),
        ObjectAnimator.ofFloat(target, "scaleX", values)
    );

    return this;
  }
}
