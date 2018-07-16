package boundless.kit.reward.overlay.particle.modifiers;

import android.view.animation.Interpolator;

import boundless.kit.reward.overlay.particle.Particle;

public class XYAccelerationModifier implements ParticleModifier {

    public boolean enabled = true;
    private float mXInitialValue;
    public float mYInitialValue;
    private float mXFinalValue;
    public float mYFinalValue;
    private long mStartTime;
    private long mDuration;
    private float mXValueIncrement;
    public float mYValueIncrement;
    private Interpolator mInterpolator;

    public XYAccelerationModifier(float XInitialValue, float YInitialValue, float XFinalValue, float YFinalValue, long startMillis, long duration, Interpolator interpolator) {
        mXInitialValue = XInitialValue;
        mYInitialValue = YInitialValue;
        mXFinalValue = XFinalValue;
        mYFinalValue = YFinalValue;
        mStartTime = startMillis;
        mDuration = duration;
        mXValueIncrement = mXFinalValue-mXInitialValue;
        mYValueIncrement = mYFinalValue-mYInitialValue;
        mInterpolator = interpolator;
    }

    @Override
    public void apply(Particle particle, long milliseconds) {
        if (!enabled) return;
        if (milliseconds < mStartTime) {
            particle.mAccelerationX = mXInitialValue;
            particle.mAccelerationY = mYInitialValue;
        } else if (mDuration != -1 && milliseconds > (mStartTime + mDuration)) {
            particle.mAccelerationX = mXFinalValue;
            particle.mAccelerationY = mYFinalValue;
        } else {
            float interpolatedValue = mInterpolator.getInterpolation((milliseconds - mStartTime) * 1f/(mDuration == -1 ? particle.mTimeToLive : mDuration));

            particle.mAccelerationX = mXInitialValue + mXValueIncrement*interpolatedValue;
            particle.mAccelerationY = mYInitialValue + mYValueIncrement*interpolatedValue;
        }
    }

}
