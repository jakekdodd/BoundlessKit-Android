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

package boundless.kit.rewards.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.view.View;

public abstract class BaseViewAnimator<T extends BaseViewAnimator<T>> {

    private AnimatorSet mAnimatorSet = new AnimatorSet();

    public void animate(View target) {
        setTarget(target);
        start();
    }

    public abstract T setTarget(View target);

    /**
     * start to animate
     */
    public void start() {
        mAnimatorSet.start();
    }

    public void restart() {
        mAnimatorSet = mAnimatorSet.clone();
        start();
    }

    /**
     * reset the view to default status
     *
     * @param target
     */
    public void reset(View target) {
        target.setAlpha(1);
        target.setScaleX(1);
        target.setScaleY(1);
        target.setTranslationX(0);
        target.setTranslationY(0);
        target.setRotation(0);
        target.setRotationY(0);
        target.setRotationX(0);
    }


    public AnimatorSet getAnimator() {
        return mAnimatorSet;
    }

    public long getDuration() {
        return mAnimatorSet.getDuration();
    }

    @SuppressWarnings("unchecked")
    public T setDuration(long duration) {
        mAnimatorSet.setDuration(duration);
        return (T)this;
    }

    public long getStartDelay() {
        return mAnimatorSet.getStartDelay();
    }

    @SuppressWarnings("unchecked")
    public T setStartDelay(long delay) {
        mAnimatorSet.setStartDelay(delay);
        return (T)this;
    }

    public boolean isStarted() {
        return mAnimatorSet.isStarted();
    }

    public boolean isRunning() {
        return mAnimatorSet.isRunning();
    }

    public void cancel() {
        mAnimatorSet.cancel();
    }

    @SuppressWarnings("unchecked")
    public T addListener(Animator.AnimatorListener l) {
        mAnimatorSet.addListener(l);
        return (T)this;
    }

    public void removeAllListener() {
        mAnimatorSet.removeAllListeners();
    }
}
