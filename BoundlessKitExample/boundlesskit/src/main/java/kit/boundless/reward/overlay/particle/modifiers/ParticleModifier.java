package kit.boundless.reward.overlay.particle.modifiers;

import kit.boundless.reward.overlay.particle.Particle;

public interface ParticleModifier {

	/**
	 * modifies the specific value of a particle given the current milliseconds
	 * @param particle
	 * @param milliseconds
	 */
	void apply(Particle particle, long milliseconds);

}
