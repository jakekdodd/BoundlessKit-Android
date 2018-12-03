package kit.boundless.reward;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * A rotate animation rotates a view a number of times.
 */
public class RotationAnimator extends BaseViewAnimator<RotationAnimator> {

    private int count = 2;
    { setDuration(1000); }
    private boolean ccw = true;

    /**
     * Sets the number of rotations. A single rotation is 360 degrees.
     * To change the rotation direction, use {@link #setCounterClockwise(boolean)}.
     * @param count The number of rotations
     * @return The object used for Constructor Chaining
     */
    public RotationAnimator setCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * The default value is true.
     * @param ccw If true, the view rotates counterclockwise. If false, the view rotates clockwise.
     * @return The object used for Constructor Chaining
     */
    public RotationAnimator setCounterClockwise(boolean ccw) {
        this.ccw = ccw;
        return this;
    }

    /**
     * The default value is false.
     * @param cw If true, the view rotates clockwise. If false, the view rotates counterclockwise.
     * @return The object used for Constructor Chaining
     */
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
