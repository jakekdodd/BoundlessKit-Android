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

package kit.boundless.reward;

import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.view.View;

/**
 * A shimmy animation changes a view's X or Y coordinate from [0, {@link #translation}, -{@link
 * #translation}*, 0] a number of times.
 * To change the {@link #translation} value, use {@link #setTranslation(float)}.
 * To change the orientation use {@link #setVertically(boolean)} or {@link
 * #setHorizontally(boolean)}*.
 * To change initial animation direction, use a negative value for {@link #setTranslation(float)}.
 */
public class ShimmyAnimator extends BaseViewAnimator<ShimmyAnimator> {

  private int count = 2;
  private float translation = 30;
  private boolean vertically = true;

  {
    setDuration(1200);
  }

  /**
   * Sets the number of shimmy translations.
   * A single shimmy is a cycle where the view is moved from [0, {@link #translation}, -{@link
   * #translation}*, 0].
   *
   * @param count The number of shimmies. Default value is 2.
   * @return The object used for Constructor Chaining
   */
  public ShimmyAnimator setCount(int count) {
    this.count = count;
    return this;
  }

  /**
   * Sets translation.
   *
   * @param translation The number of points to move in a single direction.     To start
   *     animating in the opposite direction, pass a negative value.
   * @return The object used for Constructor Chaining
   */
  public ShimmyAnimator setTranslation(float translation) {
    this.translation = translation;
    return this;
  }

  /**
   * By default, {@link #vertically} is true.
   *
   * @param horizontally If true, translation will occur along the X-axis. If false, translation
   *     will occur along the Y-axis.
   * @return The object used for Constructor Chaining
   */
  public ShimmyAnimator setHorizontally(boolean horizontally) {
    return setVertically(!horizontally);
  }

  /**
   * By default, {@link #vertically} is true.
   *
   * @param vertically If true, translation will occur along the Y-axis. If false, translation
   *     will occur along the X-axis.
   * @return The object used for Constructor Chaining
   */
  public ShimmyAnimator setVertically(boolean vertically) {
    this.vertically = vertically;
    return this;
  }

  @Override
  public ShimmyAnimator setTarget(View target) {
    float startingX = target.getX();
    float startingY = target.getY();
    float firstX = (vertically) ? startingX : (startingX + translation);
    float firstY = (vertically) ? (startingY + translation) : startingY;
    float secondX = (vertically) ? startingX : (startingX - translation);
    float secondY = (vertically) ? (startingY - translation) : startingY;

    Path path = new Path();
    path.moveTo(startingX, startingY);
    for (int i = 0; i < count; i++) {
      path.lineTo(firstX, firstY);
      path.lineTo(secondX, secondY);
    }
    path.lineTo(startingX, startingY);

    getAnimator().play(ObjectAnimator.ofFloat(target, View.X, View.Y, path));

    return this;
  }
}
