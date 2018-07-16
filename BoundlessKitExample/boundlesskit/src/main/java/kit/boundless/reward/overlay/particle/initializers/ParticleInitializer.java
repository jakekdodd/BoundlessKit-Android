package kit.boundless.reward.overlay.particle.initializers;

import java.util.Random;

import kit.boundless.reward.overlay.particle.Particle;

public interface ParticleInitializer {

	void initParticle(Particle p, Random r);

}
