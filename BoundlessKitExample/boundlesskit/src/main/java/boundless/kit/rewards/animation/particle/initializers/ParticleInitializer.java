package boundless.kit.rewards.animation.particle.initializers;

import java.util.Random;

import boundless.kit.rewards.animation.particle.Particle;

public interface ParticleInitializer {

	void initParticle(Particle p, Random r);

}
