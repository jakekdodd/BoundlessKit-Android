package boundless.kit.rewards.animation.particle;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import boundless.kit.rewards.animation.BaseViewAnimator;

public class Emojisplosion extends BaseViewAnimator {

    private int x = 0;
    private int y = 0;
    private Drawable content;
    private float scale = 0.6f;
    private float scaleRange = 0.2f;
    private long lifetime = 3000;
    private long lifetimeRange = 500;
    private float fadeout = -0.2f;
    private float quantity = 6f;
    private int bursts = 1;
    private float velocity = -50f;
    private float xAcceleration = 0f;
    private float yAcceleration = -150f;
    private float angle = -90f;
    private float range = 45f;
    private float spin = 0f;

    public Emojisplosion() {}

    public Emojisplosion(int x, int y, Drawable content, float scale, float scaleRange, long lifetime, long lifetimeRange, float fadeout, float quantity, int bursts, float velocity, float xAcceleration, float yAcceleration, float angle, float range, float spin) {
        this.x = x;
        this.y = y;
        this.content = content;
        this.scale = scale;
        this.scaleRange = scaleRange;
        this.lifetime = lifetime;
        this.lifetimeRange = lifetimeRange;
        this.fadeout = fadeout;
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
        new ParticleSystem((ViewGroup)target, 3, content, 2000)
                .setSpeedRange(0.1f, 0.1f)
                .oneShot(target, 2);
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

    public Emojisplosion setStringContent(Context context, String content, float textSize, int color) {
        this.content = new TextDrawable(context, content, textSize, color);
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

    public Emojisplosion setFadeout(float fadeout) {
        this.fadeout = fadeout;
        return this;
    }

    public Emojisplosion setQuantity(float quantity) {
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
