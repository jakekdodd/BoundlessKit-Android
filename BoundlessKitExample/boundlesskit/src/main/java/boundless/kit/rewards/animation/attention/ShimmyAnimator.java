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

public class ShimmyAnimator extends BaseViewAnimator {

    public static class Builder {
        private int _count = 2;
        private long _duration = 3000;
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
    private float speed;

    public ShimmyAnimator(int count, long duration, float translation, float speed) {
        super();
        this.count = count;
        setDuration(duration);
        this.translation = translation;
        this.speed = speed;
    }

    @Override
    public void prepare(View target) {
        float width = target.getWidth();
        float one = (float) (width / 100.0);
//        getAnimatorAgent().playTogether(
//                ObjectAnimator.ofFloat(target, "translationX", 0 * one, -25 * one, 20 * one, -15 * one, 10 * one, -5 * one, 0 * one, 0)
//
////                Glider.glide(Skill.ElasticEaseIn, 1200, ObjectAnimator.ofFloat(target, "translationY", 0, 100));
////
////                ObjectAnimator.ofFloat(target, "translationX", floats.toArray(new Float[floats.size()]))
//
//        );

//        ArrayList<Animator> animators = new ArrayList<>();
//        if (count > 0) {
//            animators.add(ObjectAnimator.ofFloat(target, "translationX", 0, translation, -translation));
//            for(int i = 1; i < count; i++) {
//                animators.add(ObjectAnimator.ofFloat(target, "translationX", -translation, translation, -translation));
//            }
//            animators.add(ObjectAnimator.ofFloat(target, "translationX", -translation, 0));
//        }
//        getAnimatorAgent().playSequentially(animators);
//        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
//        interpolator.getInterpolation(0.1f);
//        getAnimatorAgent().setInterpolator(new AccelerateDecelerateInterpolator());

//        getAnimatorAgent().play(Glider.glide(Skill.CubicEaseInOut, getDuration(), ObjectAnimator.ofFloat(target, "translationX", 1f, 1.5f)));
//        getAnimatorAgent().playSequentially(
//                Glider.glide(Skill.ElasticEaseIn, getDuration()/2, ObjectAnimator.ofFloat(target, "translationX", translation))
////                Glider.glide(Skill.ElasticEaseOut, getDuration()/2, ObjectAnimator.ofFloat(target, "translationX", -translation))
//        );


//        getAnimatorAgent().setInterpolator(PathInterpolatorCompat.create(0, 0, translation, 0));

        float x = target.getX();
        float y = target.getY();
        Path path = new Path();
        path.moveTo(x, y);
        for(int i = 0; i < count; i++) {
            path.lineTo(x + translation, y);
            path.lineTo(x - translation, y);
        }
        path.lineTo(x, y);
        getAnimatorAgent().play(ObjectAnimator.ofFloat(target, View.X, View.Y, path));


//        ArrayList<ArrayList<Float>> floats = new ArrayList<>();
//        floats.add(new ArrayList<Float>(){{add(0f);}});
//        for(int i = 0; i < count; i++) {
//            floats.add(new ArrayList<Float>(){{add(translation);}});
//            floats.add(new ArrayList<Float>(){{add(-translation);}});
//        }
//        floats.add(new ArrayList<Float>(){{add(0f);}});
//        getAnimatorAgent().play(ObjectAnimator.ofMultiFloat(target, "translationX", floats.stream().map(a -> a.stream().map(f -> f.floatValue())).toArray()));
    }
}
