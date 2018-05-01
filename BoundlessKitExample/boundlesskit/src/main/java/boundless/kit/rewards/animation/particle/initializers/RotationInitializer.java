package boundless.kit.rewards.animation.particle.initializers;

import boundless.kit.rewards.animation.particle.Particle;

import java.util.Random;

public class RotationInitializer implements ParticleInitializer {

	private int mMinAngle;
	private int mMaxAngle;

	public RotationInitializer(int minAngle, int maxAngle) {
		mMinAngle = minAngle;
		mMaxAngle = maxAngle;
	}

	@Override
	public void initParticle(Particle p, Random r) {
		p.mInitialRotation = (mMinAngle == mMaxAngle) ? mMinAngle : r.nextInt(mMaxAngle - mMinAngle) + mMinAngle;
	}

}
