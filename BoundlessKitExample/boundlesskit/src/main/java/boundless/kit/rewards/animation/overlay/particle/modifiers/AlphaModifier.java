package boundless.kit.rewards.animation.overlay.particle.modifiers;

import android.view.animation.Interpolator;

import boundless.kit.rewards.animation.overlay.particle.Particle;

public class AlphaModifier implements ParticleModifier {

	private int mInitialValue;
	private int mFinalValue;
	private long mStartTime;
	private long mDuration;
	private float mValueIncrement;
	private Interpolator mInterpolator;

    /**
     *
     * @param initialValue Initial alpha value applied when modifier starts.
     * @param finalValue Final alpha value by the time modifier finishes.
     * @param startMillis Time to start modifying alpha. A value of -1 will start from (lifetimeEnd - duration).
     * @param duration Total time to modify alpha.
     * @param interpolator Rate at which alpha value will change.
     */
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
		long startTime = (mStartTime == -1) ? (particle.mTimeToLive - mDuration) : mStartTime;
        if (startTime <= milliseconds && milliseconds <= startTime + mDuration) {
            float interpolatedValue = mInterpolator.getInterpolation((milliseconds - startTime)*1f/mDuration);
            particle.mAlpha = (int) (mInitialValue + (mValueIncrement * interpolatedValue));
        }
	}

}
