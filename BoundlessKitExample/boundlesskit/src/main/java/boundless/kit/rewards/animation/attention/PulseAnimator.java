package boundless.kit.rewards.animation.attention;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import boundless.kit.rewards.animation.BaseViewAnimator;

public class PulseAnimator extends BaseViewAnimator {

    public static class Builder {
        private int _count = 2;
        private long _duration = 860;
        private float _scale = 1.4f;
        private float _velocity = 5f;
        private float _damping = 2f;

        public Builder() { }

        public PulseAnimator build() {
            return new PulseAnimator(_count, _duration, _scale, _velocity, _damping);
        }

        public PulseAnimator.Builder count(int _count) {
            this._count = _count;
            return this;
        }

        public PulseAnimator.Builder duration(long _duration) {
            this._duration = _duration;
            return this;
        }

        public PulseAnimator.Builder scale(long _scale) {
            this._scale =  _scale;
            return this;
        }
    }

    private int count;
    private float scale;
    private float velocity;
    private float damping;

    // does not use velocity or damping currently
    public PulseAnimator(int count, long duration, float scale, float velocity, float damping) {
        super();
        this.count = count;
        setDuration(duration);
        this.scale = scale;
        this.velocity = velocity;
        this.damping = damping;
    }

    @Override
    public void prepare(View target) {
        float[] values = new float[count*2 + 1];
        for (int i = 0; i < values.length; i++) {
            values[i] = (i%2 == 0) ? 1f : scale;
        }

        getAnimatorAgent().setInterpolator(new AccelerateDecelerateInterpolator());
        getAnimatorAgent().playTogether(
                ObjectAnimator.ofFloat(target, "scaleY", values),
                ObjectAnimator.ofFloat(target, "scaleX", values)
        );
    }
}
