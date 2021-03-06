package ai.boundless.reward.particle.initializers;

import java.util.Random;

import ai.boundless.reward.particle.Particle;

/**
 * The interface Particle initializer.
 */
public interface ParticleInitializer {

  /**
   * Init particle.
   *
   * @param p the p
   * @param r the r
   */
  void initParticle(Particle p, Random r);

}
