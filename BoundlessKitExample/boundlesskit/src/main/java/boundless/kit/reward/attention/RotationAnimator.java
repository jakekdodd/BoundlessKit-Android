package boundless.kit.reward.attention;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import boundless.kit.reward.BaseViewAnimator;

public class RotationAnimator extends BaseViewAnimator<RotationAnimator> {

    private int count = 2;
    { setDuration(10000); }
    private boolean ccw = true;

    public RotationAnimator setCount(int count) {
        this.count = count;
        return this;
    }

    public RotationAnimator setCounterClockwise(boolean ccw) {
        this.ccw = ccw;
        return this;
    }

    public RotationAnimator setClockwise(boolean cw) {
        return setCounterClockwise(!cw);
    }

    @Override
    public RotationAnimator setTarget(View target) {
        getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());

        getAnimator().play(
                ObjectAnimator.ofFloat(target, "rotation", 0, (ccw ? -360 : 360) * count)
        );
        return this;
    }
}
