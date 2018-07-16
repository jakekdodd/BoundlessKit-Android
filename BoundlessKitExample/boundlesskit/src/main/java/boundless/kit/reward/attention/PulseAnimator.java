package boundless.kit.reward.attention;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import boundless.kit.reward.BaseViewAnimator;

public class PulseAnimator extends BaseViewAnimator<PulseAnimator> {

    private int count = 2;
    { setDuration(2500); }
    private float scale = 1.4f;

    public PulseAnimator setCount(int count) {
        this.count = count;
        return this;
    }

    public PulseAnimator setScale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public PulseAnimator setTarget(View target) {
        float[] values = new float[count*2 + 1];
        for (int i = values.length - 1; i >= 0; i--) {
            values[i] = (i%2 == 0) ? 1f : scale;
        }

        getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
        getAnimator().playTogether(
                ObjectAnimator.ofFloat(target, "scaleY", values),
                ObjectAnimator.ofFloat(target, "scaleX", values)
        );

        return this;
    }
}
