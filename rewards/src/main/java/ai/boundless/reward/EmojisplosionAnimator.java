package ai.boundless.reward;

import ai.boundless.reward.particle.ParticleSystem;
import ai.boundless.reward.particle.TextDrawable;
import ai.boundless.reward.particle.initializers.LifetimeInitializer;
import ai.boundless.reward.particle.initializers.XyAccelerationInitializer;
import ai.boundless.reward.particle.modifiers.ScaleModifier;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

/**
 * An emoji explosion animation that explodes or shoots emojis to delight and reinforce users.
 */
public class EmojisplosionAnimator extends BaseViewAnimator<EmojisplosionAnimator> {

  ParticleSystem particleSystem;

  private int xPositionMin = 50;
  private int xPositionMax = 50;
  private int yPositionMin = 50;
  private int yPositionMax = 50;
  private Drawable content;
  private ViewGroup target;
  private long mDuration;
  private boolean explosion = true;
  private long lifetime = 2000;
  private long lifetimeRange = 1000;
  private long fadeIn = 100;
  private long fadeOut = 200;
  private int ratePerSecond = 6;
  private float scale = 1f;
  private float scaleChange = 1.6f;
  private float velocity = 0;
  private float velocityRange = 0;
  private float xAcceleration = 0;
  private float yAcceleration = 0;
  private float shootingAngle = 180f;
  private float shootingAngleRange = 360f;
  private float rotationSpeed = 10f;
  private float rotationSpeedRange = 130f;

  {
    setDuration(1000);
  }

  /**
   * Sets a single X coordinate for particle emitting.
   *
   * @param xPosition An x coordinate
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setxPosition(int xPosition) {
    this.xPositionMin = xPosition;
    this.xPositionMax = xPosition;
    return this;
  }

  /**
   * Sets a single Y coordinate for particle emitting.
   *
   * @param yPosition An x coordinate
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setyPosition(int yPosition) {
    this.yPositionMin = yPosition;
    this.yPositionMax = yPosition;
    return this;
  }

  /**
   * Sets position min.
   *
   * @param xPositionMin The lower bound X coordinate for particle emitting.
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setxPositionMin(int xPositionMin) {
    this.xPositionMin = xPositionMin;
    return this;
  }

  /**
   * Sets position max.
   *
   * @param xPositionMax The upper bound X coordinate for particle emitting.
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setxPositionMax(int xPositionMax) {
    this.xPositionMax = xPositionMax;
    return this;
  }

  /**
   * Sets position min.
   *
   * @param yPositionMin The lower bound Y coordinate for particle emitting.
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setyPositionMin(int yPositionMin) {
    this.yPositionMin = yPositionMin;
    return this;
  }

  /**
   * Sets position max.
   *
   * @param yPositionMax The upper bound Y coordinate for particle emitting.
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setyPositionMax(int yPositionMax) {
    this.yPositionMax = yPositionMax;
    return this;
  }

  /**
   * Sets content.
   *
   * @param content A custom drawable to use instead of text.
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setContent(Drawable content) {
    this.content = content;
    return this;
  }

  /**
   * To use emoji combined with text, use a string like ("Great!" + "\uD83D\uDE00").
   *
   * @param context Context
   * @param text Emoji string to use as an emoji particle
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setContent(Context context, String text) {
    return setContent(context, text, 24, Color.BLACK);
  }

  /**
   * Sets content.
   *
   * @param context Context
   * @param text Emoji string to use as an emoji particle (i.e. "\uD83C\uDFB2")
   * @param textSize Text size
   * @param color Text color
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setContent(Context context, String text, float textSize, int color) {
    this.content = new TextDrawable(context, text, textSize, color);
    return this;
  }

  /**
   * Sets lifetime.
   *
   * @param lifetime The time an emoji particle is displayed on screen measured in milliseconds
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setLifetime(long lifetime) {
    this.lifetime = lifetime;
    return this;
  }

  /**
   * Sets whether to include an explosion effect.
   *
   * Explode the screen with emoji particles!.
   * When set true, the number of emojis is determined by {@link #setRatePerSecond(int)} * {@link
   * #setDuration(long)}/1000
   *
   * @param explosion If true shoots out all emojis at once, most similar to an explosion. If
   *     false, emojis are shot at {@link #ratePerSecond}.
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setExplosion(boolean explosion) {
    this.explosion = explosion;
    return this;
  }

  /**
   * Sets the lifetime range.
   *
   * A random value between 0 and lifetimeRange will be subtracted from each particle's lifetime.
   *
   * @param lifetimeRange Time in milliseconds
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setLifetimeRange(long lifetimeRange) {
    this.lifetimeRange = lifetimeRange;
    return this;
  }

  /**
   * Sets the fade in duration.
   *
   * @param fadeIn Time in milliseconds to fade in an emoji particle
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setFadeIn(long fadeIn) {
    this.fadeIn = fadeIn;
    return this;
  }

  /**
   * Sets the fade out duration.
   *
   * @param fadeOut Time in milliseconds to fade out an emoji particle
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setFadeOut(long fadeOut) {
    this.fadeOut = fadeOut;
    return this;
  }

  /**
   * Sets the rate of emojis per second.
   *
   * If only one emoji is preferred:.
   * {@link #setRatePerSecond(int)} to 1,
   * {@link #setDuration(long)} to 1000,
   * and finally {@link #setLifetime(long)} to desired time
   *
   * @param ratePerSecond How many emoji particles to emit in a second
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setRatePerSecond(int ratePerSecond) {
    this.ratePerSecond = ratePerSecond;
    return this;
  }

  /**
   * Sets the scale.
   *
   * @param scale The initial scale for an emoji particle
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setScale(float scale) {
    this.scale = scale;
    return this;
  }

  /**
   * Sets the scale range.
   *
   * A positive value will give an exploding effect, while a negative value will give an imploding.
   * effect.
   *
   * @param scaleChange The magnitude by which an emoji particle scale will change by the end of
   *     its {@link #lifetime}. Can be positive or negative
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setScaleChange(float scaleChange) {
    this.scaleChange = scaleChange;
    return this;
  }

  /**
   * Sets the velocity.
   *
   * To set direction, use {@link #setShootingAngle(float)}.
   * To set acceleration, use {@link #setXAcceleration(float)}.
   *
   * @param velocity Initial velocity of an emoji particle measured in density pixels per second
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setVelocity(float velocity) {
    this.velocity = velocity;
    return this;
  }

  /**
   * Sets the velocity range.
   *
   * @param velocityRange The magnitude by which the initial velocity of an emoji particle can
   *     vary measured in density pixels per second
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setVelocityRange(float velocityRange) {
    this.velocityRange = velocityRange;
    return this;
  }

  /**
   * Sets the x-axis acceleration.
   *
   * @param xAcceleration The x acceleration of an emoji particle measured in density pixels per
   *     second by second
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setXAcceleration(float xAcceleration) {
    this.xAcceleration = xAcceleration;
    return this;
  }

  /**
   * Sets the y-axis acceleration.
   *
   * @param yAcceleration The y acceleration of an emoji particle measured in density pixels per
   *     second by second
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setYAcceleration(float yAcceleration) {
    this.yAcceleration = yAcceleration;
    return this;
  }

  /**
   * Sets the shooting angle.
   *
   * Angles are in degrees and non negative meaning:.
   * 0 is to the right, 90 is to the bottom, 180 is left, 270 is right.
   *
   * To shoot in all directions, {@link #setShootingAngle(float)} to '180' and {@link
   * #setShootingAngleRange(float)} to '360'.
   *
   * @param shootingAngle The initial direction to shoot emojis measured in degrees
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setShootingAngle(float shootingAngle) {
    this.shootingAngle = shootingAngle;
    return this;
  }

  /**
   * Sets the shooting angle range.
   *
   * Angles are in degrees and non negative meaning:.
   * 0 is to the right, 90 is to the bottom, 180 is left, 270 is right.
   *
   * To shoot in all directions, {@link #setShootingAngle(float)} to '180' and {@link
   * #setShootingAngleRange(float)} to '360'.
   *
   * @param shootingAngleRange The range or width an emoji particle can be shot, with {@link
   *     #shootingAngle} in the center, measured in degrees
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setShootingAngleRange(float shootingAngleRange) {
    this.shootingAngleRange = shootingAngleRange;
    return this;
  }

  /**
   * Sets the rotation speed.
   *
   * @param rotationSpeed The rotation speed of an emoji particle, can be negative or positive
   *     and measured in degrees per second
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setRotationSpeed(float rotationSpeed) {
    this.rotationSpeed = rotationSpeed;
    return this;
  }

  /**
   * Sets the rotation speed range.
   *
   * @param rotationSpeedRange The magnitude by which the initial rotation of an emoji particle
   *     can vary measured in degrees per second
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setRotationSpeedRange(float rotationSpeedRange) {
    this.rotationSpeedRange = rotationSpeedRange;
    return this;
  }

  /**
   * Sets the animation target. Once the target is set, use {@link #start()} to begin animation.
   * Note: Target here should be a layout.
   *
   * @param target The parent layout for animation.
   * @return The object used for Constructor Chaining
   */
  public EmojisplosionAnimator setTarget(View target) {
    if (target instanceof ViewGroup) {
      this.target = (ViewGroup) target;
    }
    return this;
  }

  @Override
  public void start() {
    if (target != null && content != null) {
      particleSystem = new ParticleSystem(target,
          (int) (getDuration() * ratePerSecond),
          content,
          lifetime
      ).addInitializer(new LifetimeInitializer(lifetimeRange))
          .addInitializer(new XyAccelerationInitializer(xAcceleration / 1000f / 1000f,
              yAcceleration / 1000f / 1000f
          ))
          .addModifier(new ScaleModifier(scale,
              scale + scaleChange,
              0,
              lifetime,
              new LinearInterpolator()
          ))
          .setSpeedModuleAndAngleRange((velocity - 0.5f * velocityRange) / 1000f,
              (velocity + 0.5f * velocityRange) / 1000f,
              (int) (shootingAngle - 0.5 * shootingAngleRange),
              (int) (shootingAngle + 0.5 * shootingAngleRange)
          )
          .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange,
              rotationSpeed + 0.5f * rotationSpeedRange
          );
      if (fadeIn > 0) {
        particleSystem.setFadeIn(fadeIn);
      }
      if (fadeOut > 0) {
        particleSystem.setFadeOut(fadeOut);
      }
      particleSystem.setManuallyStartAnimator(true);
      if (explosion) {
        particleSystem.oneShot(target, (int) (getDuration() / 1000 * ratePerSecond));
      } else {
        particleSystem.emit(xPositionMin,
            xPositionMax,
            yPositionMin,
            yPositionMax,
            ratePerSecond,
            (int) getDuration()
        );
      }
      getAnimator().play(particleSystem.getAnimator());
    }
    super.start();
  }

  @Override
  public long getDuration() {
    return mDuration;
  }

  @Override
  public EmojisplosionAnimator setDuration(long duration) {
    mDuration = duration;
    return this;
  }
}
