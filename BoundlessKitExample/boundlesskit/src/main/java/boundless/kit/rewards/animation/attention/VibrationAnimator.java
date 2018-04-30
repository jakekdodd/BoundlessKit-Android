package boundless.kit.rewards.animation.attention;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import boundless.kit.rewards.animation.BaseViewAnimator;

public class VibrationAnimator extends BaseViewAnimator {

    private int vibrateCount = 6;
    private long vibrateDuration = 1000;
    private float vibrateTranslation = 10;
    private long vibrateSpeed = 1;
    private float scale = 0.8f;
    private int scaleCount = 1;
    private long scaleDuration = 300;
    private long scaleVeloctiy = 20;
    private long scaleDamping = 10;

    public VibrationAnimator() {
        super();
    }

    public VibrationAnimator(int vibrateCount, long vibrateDuration, long vibrateTranslation, long vibrateSpeed, float scale, int scaleCount, long scaleDuration, long scaleVeloctiy, long scaleDamping) {
        super();
        setVibrateCount(vibrateCount);
        setVibrateDuration(vibrateDuration);
        setVibrateTranslation(vibrateTranslation);
        setVibrateSpeed(vibrateSpeed);
        setScale(scale);
        setScaleCount(scaleCount);
        this.scaleVeloctiy = scaleVeloctiy;
        this.scaleDamping = scaleDamping;
    }

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

    public VibrationAnimator setVibrateSpeed(long speed) {
        this.vibrateSpeed = speed;
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
    public void prepare(View target) {
        float x = target.getX();
        float y = target.getY();
        Path path = new Path();
        path.moveTo(x, y);
        float xMove = target.getWidth() * (vibrateTranslation/100f);
        for(int i = 0; i < vibrateCount; i++) {
            path.lineTo(x + xMove, y);
            path.lineTo(x - xMove, y);
        }
        path.lineTo(x, y);
        Animator shimmyAnimator = ObjectAnimator.ofFloat(target, View.X, View.Y, path);

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


        zoomSet.setDuration(scaleDuration/2);
        shimmyAnimator.setStartDelay(zoomSet.getDuration() * 8 / 10);
        shimmyAnimator.setDuration(vibrateDuration / vibrateSpeed);
        unzoomSet.setStartDelay(shimmyAnimator.getStartDelay() + shimmyAnimator.getDuration());
        unzoomSet.setDuration(scaleDuration/2);

        getAnimatorAgent().setInterpolator(new AccelerateDecelerateInterpolator());
        getAnimatorAgent().playTogether(
                zoomSet,
                shimmyAnimator,
                unzoomSet
        );
    }
}
