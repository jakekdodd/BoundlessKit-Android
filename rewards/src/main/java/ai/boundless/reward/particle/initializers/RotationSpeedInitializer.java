package ai.boundless.reward.particle.initializers;

import java.util.Random;

import ai.boundless.reward.particle.Particle;

/**
 * The type Rotation speed initializer.
 */
public class RotationSpeedInitializer implements ParticleInitializer {

  private float mMinRotationSpeed;
  private float mMaxRotationSpeed;

  /**
   * Instantiates a new Rotation speed initializer.
   *
   * @param minRotationSpeed the min rotation speed
   * @param maxRotationSpeed the max rotation speed
   */
  public RotationSpeedInitializer(float minRotationSpeed, float maxRotationSpeed) {
    mMinRotationSpeed = minRotationSpeed;
    mMaxRotationSpeed = maxRotationSpeed;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    float rotationSpeed =
        r.nextFloat() * (mMaxRotationSpeed - mMinRotationSpeed) + mMinRotationSpeed;
    p.mRotationSpeed = rotationSpeed;
  }

}
