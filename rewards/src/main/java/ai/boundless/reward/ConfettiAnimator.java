package ai.boundless.reward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ai.boundless.reward.particle.ConfettoDrawable;
import ai.boundless.reward.particle.ParticleSystem;
import ai.boundless.reward.particle.initializers.AlphaInitializer;
import ai.boundless.reward.particle.initializers.LifetimeInitializer;
import ai.boundless.reward.particle.initializers.ScaleInitializer;
import ai.boundless.reward.particle.initializers.XyAccelerationInitializer;
import ai.boundless.reward.particle.modifiers.XyAccelerationModifier;
import android.animation.Animator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.ColorUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;

/**
 * The type Confetti animator.
 */
public class ConfettiAnimator extends BaseViewAnimator<ConfettiAnimator> {

  /**
   * The Burst particle system.
   */
  ParticleSystem burstParticleSystem;
  /**
   * The Shower particle system.
   */
  ParticleSystem showerParticleSystem;
  /**
   * The Blurred shower particle system.
   */
  ParticleSystem blurredShowerParticleSystem;
  /**
   * The M duration.
   */
  long mDuration;
  /**
   * The X position start.
   */
  float xPositionStart = 0f;
  /**
   * The X position end.
   */
  float xPositionEnd = 1f;
  /**
   * The Y position start.
   */
  float yPositionStart = -0.2f;
  /**
   * The Y position end.
   */
  float yPositionEnd = 0f;
  /**
   * The Content.
   */
  ArrayList<ParticleSystem.DrawableParticleTemplate> content = new ArrayList<>();
  /**
   * The Rate per second.
   */
  int ratePerSecond = 600;
  private ViewGroup target;

  {
    setDuration(2000);
  }

  /**
   * Demo confetti animator.
   *
   * @param target the target
   * @return the confetti animator
   */
  public static ConfettiAnimator demo(View target) {
    return new ConfettiAnimator().addConfetti(50,
        50,
        Arrays.asList(ConfettoDrawable.Shape.RECTANGLE,
            ConfettoDrawable.Shape.RECTANGLE,
            ConfettoDrawable.Shape.SPIRAL,
            ConfettoDrawable.Shape.CIRCLE
        ),
        Arrays.asList(ColorUtils.setAlphaComponent(Color.parseColor("#4d81fb"), 204),
            ColorUtils.setAlphaComponent(Color.parseColor("#4ac4fb"), 204),
            ColorUtils.setAlphaComponent(Color.parseColor("#9243f9"), 204),
            ColorUtils.setAlphaComponent(Color.parseColor("#fdc33b"), 204),
            ColorUtils.setAlphaComponent(Color.parseColor("#f7332f"), 204)
        )
    ).setTarget(target);
  }

  /**
   * Sets the animation target. Once the target is set, use {@link #start()} to begin animation.
   * Note: This method may be intensive, so call it a few seconds before start() if possible.
   *
   * @param target The parent view for animation.
   * @return The object used for Constructor Chaining
   */
  public ConfettiAnimator setTarget(View target) {
    if (target instanceof ViewGroup) {
      this.target = (ViewGroup) target;
      setBurstTarget();
      setShowerTarget();
      setBlurredShowerTarget();
    }
    return this;
  }

  /**
   * Add confetti confetti animator.
   *
   * @param width the width
   * @param height the height
   * @param shapes the shapes
   * @param colors the colors
   * @return the confetti animator
   */
  public ConfettiAnimator addConfetti(
      int width, int height, List<ConfettoDrawable.Shape> shapes, List<Integer> colors) {
    for (ConfettoDrawable.Shape shape : shapes) {
      for (Integer color : colors) {
        addContent(new ConfettoDrawable(shape, width, height, color));
      }
    }
    return this;
  }

  private void setBurstTarget() {
    long lifetime = 5000;
    long lifetimeRange = 1000;
    float velocity = 0.2f;
    float velocityRange = 0.001f;
    float yInitialAcceleration = 0.000001f;
    float yFinalAcceleration = 0.0008f;
    float shootingAngle = 90f;
    float shootingAngleRange = 80f;
    float rotationSpeed = 0;
    float rotationSpeedRange = 240f;
    float scale = 1f;
    float scaleRange = 0.8f;

    burstParticleSystem =
        new ParticleSystem(target, content, lifetime).addInitializer(new LifetimeInitializer(
            lifetimeRange))
            .addInitializer(new ScaleInitializer(scale - 0.5f * scaleRange,
                scale + 0.5f * scaleRange
            ))
            .addModifier(new XyAccelerationModifier(0,
                yInitialAcceleration,
                0,
                yFinalAcceleration,
                0,
                lifetime - lifetimeRange,
                new AnticipateInterpolator()
            ))
            .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange,
                velocity + 0.5f * velocityRange,
                (int) (shootingAngle - 0.5 * shootingAngleRange),
                (int) (shootingAngle + 0.5 * shootingAngleRange)
            )
            .setFadeIn(200)
            .setFadeOut(100)
            .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange,
                rotationSpeed + 0.5f * rotationSpeedRange
            )
            .setRandomParticleSelection(true);
  }

  private void setShowerTarget() {
    long lifetime = 3000;
    long lifetimeRange = 1000;
    float velocity = 0.2f;
    float velocityRange = 0.05f;
    float yAcceleration = 0.0004f;
    float shootingAngle = 90f;
    float shootingAngleRange = 45f;
    float rotationSpeed = 0;
    float rotationSpeedRange = 240f;
    float scale = 1f;
    float scaleRange = 0.8f;

    showerParticleSystem =
        new ParticleSystem(target, content, lifetime).addInitializer(new LifetimeInitializer(
            lifetimeRange))
            .addInitializer(new XyAccelerationInitializer(0, yAcceleration))
            .addInitializer(new ScaleInitializer(scale - 0.5f * scaleRange,
                scale + 0.5f * scaleRange
            ))
            .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange,
                velocity + 0.5f * velocityRange,
                (int) (shootingAngle - 0.5 * shootingAngleRange),
                (int) (shootingAngle + 0.5 * shootingAngleRange)
            )
            .setFadeIn(200)
            .setFadeOut(100)
            .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange,
                rotationSpeed + 0.5f * rotationSpeedRange
            )
            .setRandomParticleSelection(true)
            .setStartDelay(800);
  }

  private void setBlurredShowerTarget() {
    long lifetime = 3000;
    float velocity = 0.3f;
    float velocityRange = 0.15f;
    float yAcceleration = 0.0004f;
    float shootingAngle = 90f;
    float rotationSpeed = 0;
    float rotationSpeedRange = 240f;
    float scale = 4f;
    float scaleRange = 0.8f;

    Random random = new Random();
    ArrayList<ParticleSystem.BlurredDrawableParticleTemplate> blurredContent = new ArrayList<>();
    for (int i = 0; i < content.size(); i++) {
      int rand = random.nextInt(2);
      if (rand > 0) {
        blurredContent.add(new ParticleSystem.BlurredDrawableParticleTemplate(content.get(i)
            .drawable,
            1,
            rand + 1,
            rand * 4
        ));
      }
    }

    blurredShowerParticleSystem =
        new ParticleSystem(target, blurredContent, lifetime).addInitializer(new AlphaInitializer(76,
            38
        ))
            .addInitializer(new XyAccelerationInitializer(0, yAcceleration))
            .addInitializer(new ScaleInitializer(scale - 0.5f * scaleRange,
                scale + 0.5f * scaleRange
            ))
            .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange,
                velocity + 0.5f * velocityRange,
                (int) shootingAngle,
                (int) shootingAngle
            )
            .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange,
                rotationSpeed + 0.5f * rotationSpeedRange
            )
            .setFadeIn(200)
            .setFadeOut(100)
            .setRandomParticleSelection(true)
            .setStartDelay(800);
  }

  /**
   * Add content confetti animator.
   *
   * @param drawable the drawable
   * @return the confetti animator
   */
  public ConfettiAnimator addContent(Drawable drawable) {
    return this.addContent(drawable, 10);
  }

  /**
   * Add content confetti animator.
   *
   * @param drawable the drawable
   * @param count the count
   * @return the confetti animator
   */
  public ConfettiAnimator addContent(Drawable drawable, int count) {
    this.content.add(new ParticleSystem.DrawableParticleTemplate(drawable, count * ratePerSecond));
    return this;
  }

  @Override
  public void start() {
    if (getAnimator().isStarted() || target == null || content.size() == 0) {
      return;
    }

    setBurstEmitter();
    setShowerEmitter();
    setBlurredShowerEmitter();

    List<Animator> list = new ArrayList<>();
    if (burstParticleSystem != null) {
      list.add(burstParticleSystem.getAnimator());
    }
    if (showerParticleSystem != null) {
      list.add(showerParticleSystem.getAnimator());
    }
    if (blurredShowerParticleSystem != null) {
      list.add(blurredShowerParticleSystem.getAnimator());
    }
    getAnimator().playTogether(list);

    super.start();
  }

  @Override
  public long getDuration() {
    return mDuration;
  }

  @Override
  public ConfettiAnimator setDuration(long duration) {
    mDuration = duration;
    return this;
  }

  private void setBurstEmitter() {
    if (burstParticleSystem == null || burstParticleSystem.isRunning()) {
      return;
    }
    long duration = 800;
    burstParticleSystem.setManuallyStartAnimator(true);
    burstParticleSystem.emit((int) (xPositionStart * target.getWidth()),
        (int) (xPositionEnd * target.getWidth()),
        (int) (yPositionStart * target.getHeight()),
        (int) (yPositionEnd * target.getHeight()),
        ratePerSecond,
        (int) duration
    );
  }

  private void setShowerEmitter() {
    if (showerParticleSystem == null || showerParticleSystem.isRunning()) {
      return;
    }
    long duration = Math.max(0, getDuration() - 800);
    showerParticleSystem.setManuallyStartAnimator(true);
    showerParticleSystem.emit((int) (xPositionStart * target.getWidth()),
        (int) (xPositionEnd * target.getWidth()),
        (int) (yPositionStart * target.getHeight()),
        (int) (yPositionEnd * target.getHeight()),
        ratePerSecond,
        (int) duration
    );
  }

  private void setBlurredShowerEmitter() {
    if (blurredShowerParticleSystem == null || blurredShowerParticleSystem.isRunning()) {
      return;
    }
    long duration = Math.max(0, getDuration() - 800);
    blurredShowerParticleSystem.setManuallyStartAnimator(true);
    blurredShowerParticleSystem.emit((int) (xPositionStart * target.getWidth()),
        (int) (xPositionEnd * target.getWidth()),
        (int) (yPositionStart * target.getHeight()),
        (int) (yPositionEnd * target.getHeight()),
        3,
        (int) duration
    );
  }

  /**
   * Sets position end.
   *
   * @param yPositionEnd the y position end
   * @return the position end
   */
  public ConfettiAnimator setyPositionEnd(float yPositionEnd) {
    this.yPositionEnd = yPositionEnd;
    return this;
  }

  /**
   * Sets position start.
   *
   * @param xPositionStart the x position start
   * @return the position start
   */
  public ConfettiAnimator setxPositionStart(float xPositionStart) {
    this.xPositionStart = xPositionStart;
    return this;
  }

  /**
   * Sets position end.
   *
   * @param xPositionEnd the x position end
   * @return the position end
   */
  public ConfettiAnimator setxPositionEnd(float xPositionEnd) {
    this.xPositionEnd = xPositionEnd;
    return this;
  }

  /**
   * Sets position start.
   *
   * @param yPositionStart the y position start
   * @return the position start
   */
  public ConfettiAnimator setyPositionStart(float yPositionStart) {
    this.yPositionStart = yPositionStart;
    return this;
  }

  /**
   * Sets rate per second.
   *
   * @param ratePerSecond the rate per second
   * @return the rate per second
   */
  public ConfettiAnimator setRatePerSecond(int ratePerSecond) {
    this.ratePerSecond = ratePerSecond;
    return this;
  }

}
