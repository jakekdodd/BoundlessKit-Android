package kit.boundless.reward.particle.initializers;

import java.util.Random;

import kit.boundless.reward.particle.Particle;

public class XYAccelerationInitializer implements ParticleInitializer {

    private float mXValue;
    private float mYValue;

    public XYAccelerationInitializer(float XValue, float YValue) {
        mXValue = XValue;
        mYValue = YValue;
    }

    @Override
    public void initParticle(Particle p, Random r) {
        p.mAccelerationX = mXValue;
        p.mAccelerationY = mYValue;
    }

}