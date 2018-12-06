package kit.boundless.reward.particle.modifiers;

import kit.boundless.reward.particle.Particle;

/**
 * The interface Particle modifier.
 */
public interface ParticleModifier {

  /**
   * Apply.
   *
   * @param particle the particle
   * @param milliseconds the milliseconds
   */
  /*
   * modifies the specific value of a particle given the current milliseconds
   */
  void apply(Particle particle, long milliseconds);

}
