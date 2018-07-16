package boundless.kit.reward.overlay.particle.initializers;

import java.util.Random;

import boundless.kit.reward.overlay.particle.Particle;

public interface ParticleInitializer {

	void initParticle(Particle p, Random r);

}
