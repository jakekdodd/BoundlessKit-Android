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

package boundless.kit.rewards.animation.attention;

import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.view.View;

import boundless.kit.rewards.animation.BaseViewAnimator;

public class ShimmyAnimator extends BaseViewAnimator<ShimmyAnimator> {

    private int count = 2;
    { setDuration(5000); }
    private float translation = 30;

    public ShimmyAnimator setCount(int count) {
        this.count = count;
        return this;
    }

    public ShimmyAnimator setTranslation(float translation) {
        this.translation = translation;
        return this;
    }

    @Override
    public void prepare(View target) {
        float x = target.getX();
        float y = target.getY();
        Path path = new Path();
        path.moveTo(x, y);
        float xMove = target.getWidth() * (translation/100f);
        for(int i = 0; i < count; i++) {
            path.lineTo(x + xMove, y);
            path.lineTo(x - xMove, y);
        }
        path.lineTo(x, y);

        getAnimator().play(
                ObjectAnimator.ofFloat(target, View.X, View.Y, path)
        );
    }
}
