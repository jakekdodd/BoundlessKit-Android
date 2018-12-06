package ai.boundless.reward.particle.initializers;

import java.util.Random;

import ai.boundless.reward.particle.Particle;

/**
 * The type Acceleration initializer.
 */
public class AccelerationInitializer implements ParticleInitializer {

  private float mMinValue;
  private float mMaxValue;
  private int mMinAngle;
  private int mMaxAngle;

  /**
   * Instantiates a new Acceleration initializer.
   *
   * @param minAcceleration the min acceleration
   * @param maxAcceleration the max acceleration
   * @param minAngle the min angle
   * @param maxAngle the max angle
   */
  public AccelerationInitializer(
      float minAcceleration, float maxAcceleration, int minAngle, int maxAngle) {
    mMinValue = minAcceleration;
    mMaxValue = maxAcceleration;
    mMinAngle = minAngle;
    mMaxAngle = maxAngle;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    float angle = mMinAngle;
    if (mMaxAngle != mMinAngle) {
      angle = r.nextInt(mMaxAngle - mMinAngle) + mMinAngle;
    }
    float angleInRads = (float) (angle * Math.PI / 180f);
    float value = r.nextFloat() * (mMaxValue - mMinValue) + mMinValue;
    p.mAccelerationX = (float) (value * Math.cos(angleInRads));
    p.mAccelerationY = (float) (value * Math.sin(angleInRads));
  }

}
