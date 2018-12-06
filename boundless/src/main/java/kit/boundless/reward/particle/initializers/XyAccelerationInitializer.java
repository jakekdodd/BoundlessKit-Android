package kit.boundless.reward.particle.initializers;

import java.util.Random;

import kit.boundless.reward.particle.Particle;

/**
 * The type Xy acceleration initializer.
 */
public class XyAccelerationInitializer implements ParticleInitializer {

  private float mXValue;
  private float mYValue;

  /**
   * Instantiates a new Xy acceleration initializer.
   *
   * @param xValue the x value
   * @param yValue the y value
   */
  public XyAccelerationInitializer(float xValue, float yValue) {
    mXValue = xValue;
    mYValue = yValue;
  }

  @Override
  public void initParticle(Particle p, Random r) {
    p.mAccelerationX = mXValue;
    p.mAccelerationY = mYValue;
  }

}
