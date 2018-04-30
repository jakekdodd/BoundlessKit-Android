package boundless.kit.rewards.animation.attention;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

import boundless.kit.rewards.animation.BaseViewAnimator;

public class VibrationAnimator extends BaseViewAnimator {

    private int vibrateCount = 2;
    private long vibrateDuration = 5000;
    private float vibrateTranslation = 30;
    private long vibrateSpeed = 3;
    private float scale = 0.8f;
    private int scaleCount = 2;
    private long scaleDuration = 5300;
    private long scaleVeloctiy = 20;
    private long scaleDamping = 10;
    { setDuration(Math.max(vibrateDuration, scaleDuration)); }

    public VibrationAnimator() {
        super();
    }

    public VibrationAnimator(int vibrateCount, long vibrateDuration, long vibrateTranslation, long vibrateSpeed, float scale, int scaleCount, long scaleDuration, long scaleVeloctiy, long scaleDamping) {
        super();
        this.vibrateCount  = vibrateCount ;
        this.vibrateDuration = vibrateDuration;
        this.vibrateTranslation = vibrateTranslation;
        this.vibrateSpeed = vibrateSpeed;
        this.scale = scale;
        this.scaleCount = scaleCount;
        this.scaleDuration = scaleDuration;
        this.scaleVeloctiy = scaleVeloctiy;
        this.scaleDamping = scaleDamping;
//        setDuration(Math.max(vibrateDuration * vibrateCount, scaleDuration * scaleCount));
        setDuration(Math.max(this.vibrateDuration, this.scaleDuration));
    }

    @Override
    public void prepare(View target) {
        float x = target.getX();
        float y = target.getY();
        Path path = new Path();
        path.moveTo(x, y);
        float xMove = target.getWidth() * (vibrateTranslation/100f);
        Log.v("Test", "Target width:" + target.getWidth() + " transaltion:" + vibrateTranslation + " diff:" + (vibrateTranslation/100));
        for(int i = 0; i < vibrateCount; i++) {
            path.lineTo(x + xMove, y);
            path.lineTo(x - xMove, y);
            Log.v("Test", "Got point " + i + " with value:" + x + " diff:" + xMove);
        }
        path.lineTo(x, y);
        Animator shimmyAnimator = ObjectAnimator.ofFloat(target, View.X, View.Y, path);
//        shimmyAnimator.setDuration(vibrateDuration / vibrateSpeed);
//        setDuration(vibrateDuration / vibrateSpeed);


        float[] values = new float[scaleCount*2 + 1];
        for (int i = values.length - 1; i >= 0; i--) {
            values[i] = (i%2 == 0) ? 1f : scale;
        }

        setDuration(getDuration() / vibrateSpeed);
        Log.v("Test", "Got duration for vibrate:" + getDuration());

//        getAnimatorAgent().setInterpolator(new AccelerateDecelerateInterpolator());

        getAnimatorAgent().playTogether(
                ObjectAnimator.ofFloat(target, "scaleY", values),
                ObjectAnimator.ofFloat(target, "scaleX", values),
                shimmyAnimator
        );




//        float[] values = new float[11];
//        for (int i = 0; i < values.length; i++) {
//            values[i] = 1f;
//        }
//        for (int i = 0; i < scaleCount && (i*2 + 1)<values.length; i++) {
//            values[i*2 + 1] = scale;
//        }
//        Animator scaleXAnimator = ObjectAnimator.ofFloat(target, "scaleX", values)
//                .setDuration(scaleDuration * scaleCount);
//        Animator scaleYAnimator = ObjectAnimator.ofFloat(target, "scaleY", values)
//                .setDuration(scaleDuration * scaleCount);
//
////        setDuration(Math.max(shimmyAnimator.getDuration(), scaleXAnimator.getDuration()));
//        getAnimatorAgent().setInterpolator(new AccelerateDecelerateInterpolator());
//        getAnimatorAgent().playTogether(shimmyAnimator, scaleXAnimator, scaleYAnimator);
//
//
//
//
////        float x = target.getX();
////        float y = target.getY();
////        Path path = new Path();
////        path.moveTo(x, y);
////        float xMove = target.getWidth() * (vibrateTranslation/100);
////        for(int i = 0; i < vibrateCount; i++) {
////            path.lineTo(x + xMove, y);
////            path.lineTo(x - xMove, y);
////        }
////        path.lineTo(x, y);
////        Animator animator = ObjectAnimator.ofFloat(target, View.X, View.Y, path);
//////        setDuration(getDuration() / vibrateSpeed);
////        getAnimatorAgent().play(animator);
    }
}
