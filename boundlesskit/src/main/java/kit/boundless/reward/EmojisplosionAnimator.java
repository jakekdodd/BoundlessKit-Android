package kit.boundless.reward;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import kit.boundless.reward.particle.ParticleSystem;
import kit.boundless.reward.particle.TextDrawable;
import kit.boundless.reward.particle.initializers.LifetimeInitializer;
import kit.boundless.reward.particle.initializers.XYAccelerationInitializer;
import kit.boundless.reward.particle.modifiers.ScaleModifier;

public class EmojisplosionAnimator extends BaseViewAnimator<EmojisplosionAnimator> {

    ParticleSystem particleSystem;

    private int xPositionMin = 50;
    private int xPositionMax = 50;
    private int yPositionMin = 50;
    private int yPositionMax = 50;
    private Drawable content;
    private ViewGroup target;
    { setDuration(3000); }
    private long mDuration;
    private long lifetime = 2000;
    private long lifetimeRange = 1000;
    private long fadeIn = 500;
    private long fadeOut = 500;
    private int ratePerSecond = 3;
    private float scale = 1f;
    private float scaleChange = 1.2f;
    private float velocity = 0.00f;
    private float velocityRange = 0.001f;
    private float xAcceleration = 0.00002f;
    private float yAcceleration = -0.0005f;
    private float shootingAngle = -90f;
    private float shootingAngleRange = 45f;
    private float rotationSpeed = 10f;
    private float rotationSpeedRange = 130f;

    @Override
    public EmojisplosionAnimator setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    public EmojisplosionAnimator setxPosition(int xPosition) {
        this.xPositionMin = xPosition;
        this.xPositionMax = xPosition;
        return this;
    }

    public EmojisplosionAnimator setyPosition(int yPosition) {
        this.yPositionMin = yPosition;
        this.yPositionMax = yPosition;
        return this;
    }

    public EmojisplosionAnimator setxPositionMin(int xPositionMin) {
        this.xPositionMin = xPositionMin;
        return this;
    }

    public EmojisplosionAnimator setxPositionMax(int xPositionMax) {
        this.xPositionMax = xPositionMax;
        return this;
    }

    public EmojisplosionAnimator setyPositionMin(int yPositionMin) {
        this.yPositionMin = yPositionMin;
        return this;
    }

    public EmojisplosionAnimator setyPositionMax(int yPositionMax) {
        this.yPositionMax = yPositionMax;
        return this;
    }

    public EmojisplosionAnimator setContent(Drawable content) {
        this.content = content;
        return this;
    }

    public EmojisplosionAnimator setContent(Context context, String text) {
        return setContent(context, text, 24, Color.BLACK);
    }

    public EmojisplosionAnimator setContent(Context context, String text, float textSize, int color) {
        this.content = new TextDrawable(context, text, textSize, color);
        return this;
    }

    public EmojisplosionAnimator setLifetime(long lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    /**
     *
     * @param lifetimeRange a random value between 0 and lifetimeRange will be subtracted from each particle's lifetime
     * @return this for chaining
     */
    public EmojisplosionAnimator setLifetimeRange(long lifetimeRange) {
        this.lifetimeRange = lifetimeRange;
        return this;
    }

    public EmojisplosionAnimator setFadeIn(long fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public EmojisplosionAnimator setFadeOut(long fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    public EmojisplosionAnimator setRatePerSecond(int ratePerSecond) {
        this.ratePerSecond = ratePerSecond;
        return this;
    }

    public EmojisplosionAnimator setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public EmojisplosionAnimator setScaleChange(float scaleChange) {
        this.scaleChange = scaleChange;
        return this;
    }

    public EmojisplosionAnimator setVelocity(float velocity) {
        this.velocity = velocity;
        return this;
    }

    public EmojisplosionAnimator setVelocityRange(float velocityRange) {
        this.velocityRange = velocityRange;
        return this;
    }

    public EmojisplosionAnimator setXAcceleration(float xAcceleration) {
        this.xAcceleration = xAcceleration;
        return this;
    }

    public EmojisplosionAnimator setYAcceleration(float yAcceleration) {
        this.yAcceleration = yAcceleration;
        return this;
    }

    public EmojisplosionAnimator setShootingAngle(float shootingAngle) {
        this.shootingAngle = shootingAngle;
        return this;
    }

    public EmojisplosionAnimator setShootingAngleRange(float shootingAngleRange) {
        this.shootingAngleRange = shootingAngleRange;
        return this;
    }

    public EmojisplosionAnimator setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public EmojisplosionAnimator setRotationSpeedRange(float rotationSpeedRange) {
        this.rotationSpeedRange = rotationSpeedRange;
        return this;
    }

    public EmojisplosionAnimator setTarget(View target) {
        if (target instanceof ViewGroup) {
            this.target = (ViewGroup) target;
        }
        return this;
    }

    @Override
    public void start() {
        if (target != null && content != null) {
            particleSystem = new ParticleSystem(target, (int) (getDuration() * ratePerSecond), content, lifetime)
                    .addInitializer(new LifetimeInitializer(lifetimeRange))
                    .addInitializer(new XYAccelerationInitializer(xAcceleration, yAcceleration))
                    .addModifier(new ScaleModifier(scale, scale + scaleChange, 0, lifetime, new LinearInterpolator()))
                    .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange, velocity + 0.5f * velocityRange, (int) (shootingAngle - 0.5 * shootingAngleRange), (int) (shootingAngle + 0.5 * shootingAngleRange))
                    .setFadeIn(fadeIn)
                    .setFadeOut(fadeOut)
                    .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
            ;
            particleSystem.setManuallyStartAnimator(true);
            particleSystem.emit(xPositionMin, xPositionMax, yPositionMin, yPositionMax, ratePerSecond, (int) getDuration());
            getAnimator().play(particleSystem.getAnimator());
        }
        super.start();
    }
}
