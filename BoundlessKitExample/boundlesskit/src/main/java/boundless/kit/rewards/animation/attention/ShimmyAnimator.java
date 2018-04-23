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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.view.View;

import boundless.kit.rewards.animation.BaseViewAnimator;

public class ShimmyAnimator extends BaseViewAnimator {

    public static class Builder {
        private int _count = 2;
        private long _duration = 5000;
        private long _translation = 30;
        private long _speed = 3;

        public Builder() { }

        public ShimmyAnimator build() {
            return new ShimmyAnimator(_count, _duration, _translation, _speed);
        }

        public Builder count(int _count) {
            this._count = _count;
            return this;
        }

        public Builder duration(long _duration) {
            this._duration= _duration;
            return this;
        }
    }

    private int count;
    private float translation;
    private long speed;

    public ShimmyAnimator(int count, long duration, float translation, long speed) {
        super();
        this.count = count;
        setDuration(duration);
        this.translation = translation;
        this.speed = speed;
    }

    @Override
    public void prepare(View target) {
        float x = target.getX();
        float y = target.getY();
        Path path = new Path();
        path.moveTo(x, y);
        float xMove = target.getWidth() * (translation/100);
        for(int i = 0; i < count; i++) {
            path.lineTo(x + xMove, y);
            path.lineTo(x - xMove, y);
        }
        path.lineTo(x, y);
        Animator animator = ObjectAnimator.ofFloat(target, View.X, View.Y, path);
        setDuration(getDuration() / speed);
        getAnimatorAgent().play(animator);
    }
}
