package boundless.kit.rewards.animation.particle;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import boundless.kit.rewards.animation.BaseViewAnimator;
import boundless.kit.rewards.animation.particle.initializers.XYAccelerationInitializer;
import boundless.kit.rewards.animation.particle.modifiers.ScaleModifier;

public class Emojisplosion extends BaseViewAnimator {

    private int x = 50;
    private int y = 50;
    private Drawable content;
    private float scale = 0.6f;
    private float scaleChange = 1.2f;
    private long lifetime = 3000;
    private long lifetimeRange = 500;
    private long fadeIn = 1000;
    private long fadeOut = 1000;
    private int quantity = 1;
    private int duration = 3000;
    private float velocity = 0.003f;

    private float velocityRange = 0.001f;
    private float xAcceleration = 0.00002f;
    private float yAcceleration = -0.00005f;
    private float shootingAngle = -90f;
    private float shootingAngleRange = 45f;
    private float rotationSpeed = 10f;
    private float rotationSpeedRange = 130f;

    public Emojisplosion setX(int x) {
        this.x = x;
        return this;
    }

    public Emojisplosion setY(int y) {
        this.y = y;
        return this;
    }

    public Emojisplosion setContent(Drawable content) {
        this.content = content;
        return this;
    }

    public Emojisplosion setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public Emojisplosion setScaleChange(float scaleChange) {
        this.scaleChange = scaleChange;
        return this;
    }

    public Emojisplosion setLifetime(long lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public Emojisplosion setLifetimeRange(long lifetimeRange) {
        this.lifetimeRange = lifetimeRange;
        return this;
    }

    public Emojisplosion setFadeOut(long fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    public Emojisplosion setFadeIn(long fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public Emojisplosion setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public Emojisplosion setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public Emojisplosion setVelocity(float velocity) {
        this.velocity = velocity;
        return this;
    }

    public Emojisplosion setVelocityRange(float velocityRange) {
        this.velocityRange = velocityRange;
        return this;
    }

    public Emojisplosion setXAcceleration(float xAcceleration) {
        this.xAcceleration = xAcceleration;
        return this;
    }

    public Emojisplosion setYAcceleration(float yAcceleration) {
        this.yAcceleration = yAcceleration;
        return this;
    }

    public Emojisplosion setShootingAngle(float shootingAngle) {
        this.shootingAngle = shootingAngle;
        return this;
    }

    public Emojisplosion setShootingAngleRange(float shootingAngleRange) {
        this.shootingAngleRange = shootingAngleRange;
        return this;
    }

    public Emojisplosion setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public Emojisplosion setRotationSpeedRange(float rotationSpeedRange) {
        this.rotationSpeedRange = rotationSpeedRange;
        return this;
    }

    public Emojisplosion setTarget(View target) {
        if (content == null) {
            return this;
        }

        ParticleSystem particleSystem = new ParticleSystem((ViewGroup)target, duration * quantity, content, lifetime)
                .addModifier(new ScaleModifier(scale, scale + scaleChange, 0, lifetime, new LinearInterpolator()))
                .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange, velocity + 0.5f * velocityRange, (int)(shootingAngle - 0.5 * shootingAngleRange), (int)(shootingAngle + 0.5 * shootingAngleRange))
                .addInitializer(new XYAccelerationInitializer(xAcceleration, yAcceleration))
                .setFadeOut(fadeOut)
                .setFadeIn(fadeIn)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
                ;
        Log.v("Test", "Width:" + (target.getHeight()/2));
        particleSystem.emit(x, y, quantity, duration);

        return this;
    }
}
