package kit.boundless.reward.particle.initializers;

import java.util.Random;

import kit.boundless.reward.particle.Particle;

public interface ParticleInitializer {

	void initParticle(Particle p, Random r);

}
