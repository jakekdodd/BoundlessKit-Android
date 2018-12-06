package kit.boundless.reward.particle.initializers;

import java.util.Random;

import kit.boundless.reward.particle.Particle;

/**
 * The type Alpha initializer.
 */
public class AlphaInitializer implements ParticleInitializer {

  private int alpha;
  private int alphaRange;

  /**
   * Instantiates a new Alpha initializer.
   *
   * @param alpha the alpha
   * @param alphaRange the alpha range
   */
  public AlphaInitializer(int alpha, int alphaRange) {
    this.alpha = alpha;
    this.alphaRange = alphaRange;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    p.mAlpha = alpha + (r.nextBoolean() ? 1 : -1) * r.nextInt(alphaRange);
  }


}
