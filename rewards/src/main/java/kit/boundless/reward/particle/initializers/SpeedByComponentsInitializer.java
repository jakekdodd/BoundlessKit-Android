package kit.boundless.reward.particle.initializers;

import java.util.Random;

import kit.boundless.reward.particle.Particle;

/**
 * The type Speed by components initializer.
 */
public class SpeedByComponentsInitializer implements ParticleInitializer {

  private float mMinSpeedX;
  private float mMaxSpeedX;
  private float mMinSpeedY;
  private float mMaxSpeedY;

  /**
   * Instantiates a new Speed by components initializer.
   *
   * @param speedMinX the speed min x
   * @param speedMaxX the speed max x
   * @param speedMinY the speed min y
   * @param speedMaxY the speed max y
   */
  public SpeedByComponentsInitializer(
      float speedMinX, float speedMaxX, float speedMinY, float speedMaxY) {
    mMinSpeedX = speedMinX;
    mMaxSpeedX = speedMaxX;
    mMinSpeedY = speedMinY;
    mMaxSpeedY = speedMaxY;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    p.mSpeedX = r.nextFloat() * (mMaxSpeedX - mMinSpeedX) + mMinSpeedX;
    p.mSpeedY = r.nextFloat() * (mMaxSpeedY - mMinSpeedY) + mMinSpeedY;
  }

}
