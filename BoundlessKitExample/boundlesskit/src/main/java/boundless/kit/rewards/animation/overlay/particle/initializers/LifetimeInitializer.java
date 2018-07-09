package boundless.kit.rewards.animation.overlay.particle.initializers;

import java.util.Random;

import boundless.kit.rewards.animation.overlay.particle.Particle;

public class LifetimeInitializer implements ParticleInitializer {

    private long mLifetimeRange;

    /**
     *
     * @param lifetimeRange The lifetime will be reduced by a random amount up to this value.
     *                      A negative value will randomly extend the lifetime up to this value,
     *                      but modifiers that depend on lifetime (like fade out using AlphaModifier) may not work properly.
     */
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
