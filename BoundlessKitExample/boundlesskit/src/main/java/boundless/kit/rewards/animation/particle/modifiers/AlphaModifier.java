package boundless.kit.rewards.animation.particle.modifiers;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import boundless.kit.rewards.animation.particle.Particle;

public class AlphaModifier implements ParticleModifier {

	private int mInitialValue;
	private int mFinalValue;
	private long mStartTime;
	private long mEndTime;
	private float mDuration;
	private float mValueIncrement;
	private Interpolator mInterpolator;

	public AlphaModifier(int initialValue, int finalValue, long startMillis, long endMillis, Interpolator interpolator) {
		mInitialValue = initialValue;
		mFinalValue = finalValue;
		mStartTime = startMillis;
		mEndTime = endMillis;
		mDuration = mEndTime - mStartTime;
		mValueIncrement = mFinalValue-mInitialValue;
		mInterpolator = interpolator;
	}
	
	public AlphaModifier (int initialValue, int finalValue, long startMillis, long endMillis) {
		this(initialValue, finalValue, startMillis, endMillis, new LinearInterpolator());
	}

	@Override
	public void apply(Particle particle, long milliseconds) {
		if (mStartTime <= milliseconds && milliseconds <= mEndTime) {
			float interpolaterdValue = mInterpolator.getInterpolation((milliseconds- mStartTime)*1f/mDuration);
			int newAlphaValue = (int) (mInitialValue + mValueIncrement*interpolaterdValue);
			particle.mAlpha = newAlphaValue;
		}		
	}

}
