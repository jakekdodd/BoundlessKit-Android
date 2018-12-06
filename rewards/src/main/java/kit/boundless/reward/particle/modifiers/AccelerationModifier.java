package kit.boundless.reward.particle.modifiers;

import kit.boundless.reward.particle.Particle;

/**
 * The type Acceleration modifier.
 */
public class AccelerationModifier implements ParticleModifier {

  private float mVelocityX;
  private float mVelocityY;

  /**
   * Instantiates a new Acceleration modifier.
   *
   * @param velocity the velocity
   * @param angle the angle
   */
  public AccelerationModifier(float velocity, float angle) {
    float velocityAngleInRads = (float) (angle * Math.PI / 180f);
    mVelocityX = (float) (velocity * Math.cos(velocityAngleInRads));
    mVelocityY = (float) (velocity * Math.sin(velocityAngleInRads));
  }

  @Override
  public void apply(Particle particle, long milliseconds) {
    particle.mCurrentX += mVelocityX * milliseconds * milliseconds;
    particle.mCurrentY += mVelocityY * milliseconds * milliseconds;
  }

}
