package boundless.kit.rewards.animation.attention;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import boundless.kit.rewards.animation.BaseViewAnimator;

public class PulseAnimator extends BaseViewAnimator {

    private int count = 2;
    { setDuration(860); }
    private float scale = 1.4f;
    private float velocity = 5f;
    private float damping = 2f;

    public PulseAnimator() {
        super();
    }

    // does not use velocity or damping currently
    public PulseAnimator(int count, long duration, float scale, float velocity, float damping) {
        super();
        setCount(count);
        setDuration(duration);
        setScale(scale);
        this.velocity = velocity;
        this.damping = damping;
    }

    public PulseAnimator setCount(int count) {
        this.count = count;
        return this;
    }

    @Override
    public PulseAnimator setDuration(long duration) {
        super.setDuration(duration);
        return this;
    }

    public PulseAnimator setScale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public void prepare(View target) {
        float[] values = new float[count*2 + 1];
        for (int i = values.length - 1; i >= 0; i--) {
            values[i] = (i%2 == 0) ? 1f : scale;
        }

        getAnimatorAgent().setInterpolator(new AccelerateDecelerateInterpolator());
        getAnimatorAgent().playTogether(
                ObjectAnimator.ofFloat(target, "scaleY", values),
                ObjectAnimator.ofFloat(target, "scaleX", values)
        );
    }
}
