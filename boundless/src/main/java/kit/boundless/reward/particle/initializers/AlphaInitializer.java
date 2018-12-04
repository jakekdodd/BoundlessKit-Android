package kit.boundless.reward.particle.initializers;

import java.util.Random;

import kit.boundless.reward.particle.Particle;

public class AlphaInitializer implements ParticleInitializer {

    private int alpha;
    private int alphaRange;

    public AlphaInitializer(int alpha, int alphaRange) {
        this.alpha = alpha;
        this.alphaRange = alphaRange;
    }

    @Override
    public void initParticle(Particle p, Random r) {
        p.mAlpha = alpha + (r.nextBoolean() ? 1 : -1) * r.nextInt(alphaRange);
    }


}
