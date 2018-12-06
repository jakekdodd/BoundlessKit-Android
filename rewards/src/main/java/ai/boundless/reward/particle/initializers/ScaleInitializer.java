package ai.boundless.reward.particle.initializers;

import java.util.Random;

import ai.boundless.reward.particle.Particle;

/**
 * The type Scale initializer.
 */
public class ScaleInitializer implements ParticleInitializer {

  private float mMaxScale;
  private float mMinScale;

  /**
   * Instantiates a new Scale initializer.
   *
   * @param minScale the min scale
   * @param maxScale the max scale
   */
  public ScaleInitializer(float minScale, float maxScale) {
    mMinScale = minScale;
    mMaxScale = maxScale;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    float scale = r.nextFloat() * (mMaxScale - mMinScale) + mMinScale;
    p.mScale = scale;
  }

}
