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
		if (mStartTime == -1) {
			mStartTime = particle.mTimeToLive - mDuration;
		}
		if (mStartTime <= milliseconds && milliseconds <= mStartTime + mDuration) {
			float interpolaterdValue = mInterpolator.getInterpolation((milliseconds - mStartTime)*1f/mDuration);
			int newAlphaValue = (int) (mInitialValue + mValueIncrement*interpolaterdValue);
			particle.mAlpha = newAlphaValue;
		}		
	}

}
