package boundless.kit.rewards.animation.attention;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import boundless.kit.rewards.animation.BaseViewAnimator;

public class VibrationAnimator extends BaseViewAnimator<VibrationAnimator> {

    private int vibrateCount = 6;
    private long vibrateDuration = 333;
    private float vibrateTranslation = 50;
    private boolean vibrateVertically = false;
    private float scale = 0.8f;
    private int scaleCount = 1;
    private long scaleDuration = 300;

    public VibrationAnimator setVibrateCount(int count) {
        this.vibrateCount = count;
        return this;
    }

    public VibrationAnimator setVibrateDuration(long duration) {
        this.vibrateDuration = duration;
        return this;
    }

    public VibrationAnimator setVibrateTranslation(long translation) {
        this.vibrateTranslation = translation;
        return this;
    }

    public VibrationAnimator setVibrateVertically(boolean vibrateVertically) {
        this.vibrateVertically = vibrateVertically;
        return this;
    }

    public VibrationAnimator setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public VibrationAnimator setScaleCount(int count) {
        this.scaleCount = count;
        return this;
    }

    public VibrationAnimator setScaleDuration(long duration) {
        this.scaleDuration = duration;
        return this;
    }

    @Override
    public VibrationAnimator setTarget(View target) {
        float[] values = new float[scaleCount * 2];
        float[] reversedValues = new float[scaleCount * 2];
        for (int i = reversedValues.length - 1; i >= 0; i--) {
            if (i%2 == 0) {
                values[i] = 1f;
                reversedValues[i] = scale;
            } else {
                values[i] = scale;
                reversedValues[i] = 1f;
            }
        }
        AnimatorSet zoomSet = new AnimatorSet();
        AnimatorSet unzoomSet = new AnimatorSet();
        zoomSet.playTogether(
                ObjectAnimator.ofFloat(target, "scaleY", values),
                ObjectAnimator.ofFloat(target, "scaleX", values)
        );
        unzoomSet.playTogether(
                ObjectAnimator.ofFloat(target, "scaleY", reversedValues),
                ObjectAnimator.ofFloat(target, "scaleX", reversedValues)
        );

        ShimmyAnimator shimmyAnimator = new ShimmyAnimator()
                .setCount(vibrateCount)
                .setTranslation(vibrateTranslation)
                .setVertically(vibrateVertically)
                .setTarget(target);

        zoomSet.setDuration(scaleDuration/2);
        shimmyAnimator.setStartDelay(zoomSet.getDuration() * 8 / 10);
        shimmyAnimator.setDuration(vibrateDuration);
        unzoomSet.setStartDelay(shimmyAnimator.getStartDelay() + shimmyAnimator.getDuration());
        unzoomSet.setDuration(scaleDuration/2);

        getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
        getAnimator().playTogether(
                zoomSet,
                shimmyAnimator.getAnimator(),
                unzoomSet
        );

        return this;
    }
}
