package kit.boundless.reward.particle;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * The type Particle field.
 */
class ParticleField extends View {

  private ArrayList<Particle> mParticles;

  /**
   * Instantiates a new Particle field.
   *
   * @param context the context
   * @param attrs the attrs
   * @param defStyle the def style
   */
  public ParticleField(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  /**
   * Instantiates a new Particle field.
   *
   * @param context the context
   * @param attrs the attrs
   */
  public ParticleField(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Instantiates a new Particle field.
   *
   * @param context the context
   */
  public ParticleField(Context context) {
    super(context);
  }

  /**
   * Sets particles.
   *
   * @param particles the particles
   */
  public void setParticles(ArrayList<Particle> particles) {
    mParticles = particles;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    // Draw all the particles
    synchronized (mParticles) {
      for (int i = 0; i < mParticles.size(); i++) {
        mParticles.get(i).draw(canvas);
      }
    }
  }
}
