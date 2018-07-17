package kit.boundless.reward.particle.modifiers;

import kit.boundless.reward.particle.Particle;

public interface ParticleModifier {

	/**
	 * modifies the specific value of a particle given the current milliseconds
	 * @param particle
	 * @param milliseconds
	 */
	void apply(Particle particle, long milliseconds);

}
