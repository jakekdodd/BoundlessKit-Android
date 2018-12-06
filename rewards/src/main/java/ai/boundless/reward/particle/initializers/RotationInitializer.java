package ai.boundless.reward.particle.initializers;

import java.util.Random;

import ai.boundless.reward.particle.Particle;

/**
 * The type Rotation initializer.
 */
public class RotationInitializer implements ParticleInitializer {

  private int mMinAngle;
  private int mMaxAngle;

  /**
   * Instantiates a new Rotation initializer.
   *
   * @param minAngle the min angle
   * @param maxAngle the max angle
   */
  public RotationInitializer(int minAngle, int maxAngle) {
    mMinAngle = minAngle;
    mMaxAngle = maxAngle;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    p.mInitialRotation =
        (mMinAngle == mMaxAngle) ? mMinAngle : r.nextInt(mMaxAngle - mMinAngle) + mMinAngle;
  }

}
