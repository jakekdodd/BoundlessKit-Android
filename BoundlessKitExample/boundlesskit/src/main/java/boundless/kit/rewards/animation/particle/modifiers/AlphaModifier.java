package boundless.kit.rewards.animation.particle.modifiers;

import android.view.animation.Interpolator;

import boundless.kit.rewards.animation.particle.Particle;

public class AlphaModifier implements ParticleModifier {

	private int mInitialValue;
	private int mFinalValue;
	private long mStartTime;
	private long mDuration;
	private float mValueIncrement;
	private Interpolator mInterpolator;

	public AlphaModifier(int initialValue, int finalValue, long startMillis, long duration, Interpolator interpolator) {
		mInitialValue = initialValue;
		mFinalValue = finalValue;
		mStartTime = startMillis;
		mDuration = duration;
		mValueIncrement = mFinalValue-mInitialValue;
		mInterpolator = interpolator;
	}

	@Override
	public void apply(Particle particle, long milliseconds) {
		if (mStartTime == -1 ) {
		    long startTime = particle.mTimeToLive - mDuration;
            if (startTime <= milliseconds && milliseconds <= startTime + mDuration) {
                float interpolatedValue = mInterpolator.getInterpolation((milliseconds - startTime)*1f/mDuration);
                particle.mAlpha = (int) (mInitialValue + (mValueIncrement * interpolatedValue));
            }
		} else {
            if (mStartTime <= milliseconds && milliseconds <= mStartTime + mDuration) {
                float interpolatedValue = mInterpolator.getInterpolation((milliseconds - mStartTime) * 1f / mDuration);
                particle.mAlpha = (int) (mInitialValue + (mValueIncrement * interpolatedValue));
            }
        }
	}

}
