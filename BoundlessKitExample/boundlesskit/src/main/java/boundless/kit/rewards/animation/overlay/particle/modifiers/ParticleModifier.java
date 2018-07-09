package boundless.kit.rewards.animation.overlay.particle.modifiers;

import boundless.kit.rewards.animation.overlay.particle.Particle;

public interface ParticleModifier {

	/**
	 * modifies the specific value of a particle given the current milliseconds
	 * @param particle
	 * @param milliseconds
	 */
	void apply(Particle particle, long milliseconds);

}
