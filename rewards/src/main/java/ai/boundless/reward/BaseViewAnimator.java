/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 daimajia
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ai.boundless.reward;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.view.View;

/**
 * The type Base view animator.
 *
 * @param <T> the type parameter
 */
public abstract class BaseViewAnimator<T extends BaseViewAnimator<T>> {

  private AnimatorSet mAnimatorSet = new AnimatorSet();

  /**
   * Sets the animation target. Once the target is set, use {@link #start()} to begin animation.
   *
   * @param target The parent view for animation.
   * @return The object used for Constructor Chaining
   */
  public abstract T setTarget(View target);

  /**
   * Begins animation. If animation does not seem to start,.
   * make sure the target view is assigned using method {@link #setTarget(View)}.
   */
  public void start() {
    if (mAnimatorSet.isStarted()) {
      return;
    }
    mAnimatorSet.start();
  }

  /**
   * Returns the animator object if further customization is desired.
   *
   * @return The animator set object
   */
  public AnimatorSet getAnimator() {
    return mAnimatorSet;
  }

  /**
   * The animation duration.
   *
   * @return How long, in milliseconds, the animation will take.
   */
  public long getDuration() {
    return mAnimatorSet.getDuration();
  }

  /**
   * Set the animation duration.
   *
   * @param duration The time, in milliseconds, to complete animation.
   * @return The object used for Constructor Chaining
   */
  @SuppressWarnings("unchecked")
  public T setDuration(long duration) {
    mAnimatorSet.setDuration(duration);
    return (T) this;
  }

  /**
   * The time to begin animation after start() has been called.
   *
   * @return The delay time in milliseconds
   */
  public long getStartDelay() {
    return mAnimatorSet.getStartDelay();
  }

  /**
   * Set the time to delay animation after start() has been called.
   *
   * @param delay The delay time in milliseconds
   * @return The object used for Constructor Chaining
   */
  @SuppressWarnings("unchecked")
  public T setStartDelay(long delay) {
    mAnimatorSet.setStartDelay(delay);
    return (T) this;
  }

  /**
   * Returns true if the animation has been started and have not yet ended.
   * Animations will not be started until after its initial delay, set through {@link
   * #setStartDelay(long)}*.
   *
   * @return Whether the animation has been started and not yet ended.
   */
  public boolean isRunning() {
    return mAnimatorSet.isRunning();
  }

  /**
   * Cancels the animation causing the animation to stop in its tracks, sending an.
   * onAnimationCancel(Animator) to its listeners, followed by an onAnimationEnd(Animator) message.
   * This method must be called on the thread that is running the animation.
   */
  public void cancel() {
    mAnimatorSet.cancel();
  }

  /**
   * Adds a listener to the set of listeners that are sent events through the life of an animation,.
   * such as start, repeat, and end.
   *
   * @param listener The listener to be added to the current set of listeners for this
   *     animation.
   * @return The object used for Constructor Chaining
   */
  @SuppressWarnings("unchecked")
  public T addListener(Animator.AnimatorListener listener) {
    mAnimatorSet.addListener(listener);
    return (T) this;
  }

  /**
   * Removes all listeners from this object.
   */
  public void removeAllListener() {
    mAnimatorSet.removeAllListeners();
  }
}
