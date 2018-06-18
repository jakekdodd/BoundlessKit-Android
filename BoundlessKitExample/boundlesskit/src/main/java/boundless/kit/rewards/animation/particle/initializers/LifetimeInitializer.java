package boundless.kit.rewards.animation.particle.initializers;

import java.util.Random;

import boundless.kit.rewards.animation.particle.Particle;

public class LifetimeInitializer implements ParticleInitializer {

    private long mLifetimeRange;

    public LifetimeInitializer(long lifetimeRange) {
        mLifetimeRange = lifetimeRange;
    }

    public long getLifetimeRange() {
        return mLifetimeRange;
    }

    @Override
    public void initParticle(Particle p, Random r) {
        float subtractedRange = r.nextFloat() * mLifetimeRange;
        p.mTimeToLive -= subtractedRange;
    }
}
