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
    private float scaleRange = 0.2f;
    private long lifetime = 3000;
    private long lifetimeRange = 500;
    private long fadeIn = 1000;
    private long fadeOut = 1000;
    private int quantity = 1;
    private int bursts = 3000;
    private float velocity = 0.003f;
    private float xAcceleration = 0.00002f;
    private float yAcceleration = -0.00005f;
    private float angle = -90f;
    private float range = 45f;
    private float spin = 10f;

    public Emojisplosion() {}

    public Emojisplosion(int x, int y, Drawable content, float scale, float scaleRange, long lifetime, long lifetimeRange, long fadeIn, long fadeOut, int quantity, int bursts, float velocity, float xAcceleration, float yAcceleration, float angle, float range, float spin) {
        this.x = x;
        this.y = y;
        this.content = content;
        this.scale = scale;
        this.scaleRange = scaleRange;
        this.lifetime = lifetime;
        this.lifetimeRange = lifetimeRange;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.quantity = quantity;
        this.bursts = bursts;
        this.velocity = velocity;
        this.xAcceleration = xAcceleration;
        this.yAcceleration = yAcceleration;
        this.angle = angle;
        this.range = range;
        this.spin = spin;
    }

    public void prepare(View target) {
        if (content == null) {
            return;
        }
//        target.add
        ParticleSystem particleSystem = new ParticleSystem((ViewGroup)target, bursts * quantity, content, lifetime)
                .addModifier(new ScaleModifier(scale, scale + scaleRange, 0, lifetime, new LinearInterpolator()))
                .setSpeedModuleAndAngleRange(velocity, velocity, (int)(angle + 0.5 * range), (int)(angle - 0.5 * range))
                .addInitializer(new XYAccelerationInitializer(xAcceleration, yAcceleration))
                .setFadeOut(fadeOut)
                .setFadeIn(fadeIn)
                .setRotationSpeedRange(spin, spin)
        ;
        Log.v("Test", "Width:" + (target.getHeight()/2));
        particleSystem.emit(x, y, quantity, bursts);
    }

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

    public Emojisplosion setScaleRange(float scaleRange) {
        this.scaleRange = scaleRange;
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

    public Emojisplosion setBursts(int bursts) {
        this.bursts = bursts;
        return this;
    }

    public Emojisplosion setVelocity(float velocity) {
        this.velocity = velocity;
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

    public Emojisplosion setAngle(float angle) {
        this.angle = angle;
        return this;
    }

    public Emojisplosion setRange(float range) {
        this.range = range;
        return this;
    }

    public Emojisplosion setSpin(float spin) {
        this.spin = spin;
        return this;
    }

}
