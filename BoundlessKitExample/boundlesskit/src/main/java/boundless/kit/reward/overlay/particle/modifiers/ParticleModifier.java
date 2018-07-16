package boundless.kit.reward.overlay.particle.modifiers;

import boundless.kit.reward.overlay.particle.Particle;

public interface ParticleModifier {

	/**
	 * modifies the specific value of a particle given the current milliseconds
	 * @param particle
	 * @param milliseconds
	 */
	void apply(Particle particle, long milliseconds);

}
