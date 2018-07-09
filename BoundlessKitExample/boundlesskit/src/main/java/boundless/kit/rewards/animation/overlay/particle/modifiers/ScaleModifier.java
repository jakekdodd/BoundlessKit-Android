package boundless.kit.rewards.animation.overlay.particle.modifiers;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import boundless.kit.rewards.animation.overlay.particle.Particle;

public class ScaleModifier implements ParticleModifier {

	private float mInitialValue;
	private float mFinalValue;
	private long mEndTime;
	private long mStartTime;
	private long mDuration;
	private float mValueIncrement;
	private Interpolator mInterpolator;

	public ScaleModifier (float initialValue, float finalValue, long startMillis, long endMillis, Interpolator interpolator) {
		mInitialValue = initialValue;
		mFinalValue = finalValue;
		mStartTime = startMillis;
		mEndTime = endMillis;
		mDuration = mEndTime - mStartTime;
		mValueIncrement = mFinalValue-mInitialValue;
		mInterpolator = interpolator;
	}
	
	public ScaleModifier (float initialValue, float finalValue, long startMillis, long endMillis) {
		this (initialValue, finalValue, startMillis, endMillis, new LinearInterpolator());
	}
	
	@Override
	public void apply(Particle particle, long milliseconds) {
		if (milliseconds < mStartTime) {
			particle.mScale = mInitialValue;
		}
		else if (milliseconds > mEndTime) {
			particle.mScale = mFinalValue;
		}
		else {
			float interpolaterdValue = mInterpolator.getInterpolation((milliseconds - mStartTime) * 1f/mDuration);
			float newScale = mInitialValue + mValueIncrement*interpolaterdValue;
			particle.mScale = newScale;
		}
	}

}
