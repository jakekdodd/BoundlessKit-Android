package boundless.boundlesskitexample;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

import boundless.kit.rewards.animation.BaseViewAnimator;
import boundless.kit.rewards.animation.overlay.particle.ParticleSystem;
import boundless.kit.rewards.animation.overlay.particle.initializers.LifetimeInitializer;
import boundless.kit.rewards.animation.overlay.particle.initializers.XYAccelerationInitializer;
import boundless.kit.rewards.animation.overlay.particle.modifiers.ScaleModifier;

public class Confetti extends BaseViewAnimator<Confetti> {

    ParticleSystem burstParticleSystem;
    ParticleSystem showerParticleSystem;

    private float xPositionStart = 0f;
    private float xPositionEnd = 1f;
    private float yPositionStart = -0.2f;
    private float yPositionEnd = -0.05f;
//    private float xPositionStart = 0;
//    private float xPositionEnd = 1;
//    private float yPositionStart = -0.05f;
//    private float yPositionEnd = -0.01f;
    public ArrayList<Drawable> burstContent = new ArrayList<>();
    public ArrayList<Drawable> showerContent = new ArrayList<>();
    private ViewGroup target;
    { setDuration(3000); }
    private long lifetime = 2000;
    private long lifetimeRange = 1000;
    private long fadeIn = 250;
    private long fadeOut = 500;
    private int ratePerSecond = 2000;
    private float scale = 1f;
    private float scaleChange = 1.2f;
    private float velocity = 0.25f;
    private float velocityRange = 0.001f;
    private float xAcceleration = 0;
    private float yAcceleration = 0;
    private float shootingAngle = 90f;
    private float shootingAngleRange = 45f;
    private float rotationSpeed = 10f;
    private float rotationSpeedRange = 130f;

    public Confetti setxPosition(int xPosition) {
        this.xPositionStart = xPosition;
        this.xPositionEnd = xPosition;
        return this;
    }

    public Confetti setyPosition(int yPosition) {
        this.yPositionStart = yPosition;
        this.yPositionEnd = yPosition;
        return this;
    }

    public Confetti setxPositionStart(int xPositionStart) {
        this.xPositionStart = xPositionStart;
        return this;
    }

    public Confetti setxPositionEnd(int xPositionEnd) {
        this.xPositionEnd = xPositionEnd;
        return this;
    }

    public Confetti setyPositionStart(int yPositionStart) {
        this.yPositionStart = yPositionStart;
        return this;
    }

    public Confetti setyPositionEnd(int yPositionEnd) {
        this.yPositionEnd = yPositionEnd;
        return this;
    }

    public Confetti setLifetime(long lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    /**
     *
     * @param lifetimeRange a random value between 0 and lifetimeRange will be subtracted from each particle's lifetime
     * @return this for chaining
     */
    public Confetti setLifetimeRange(long lifetimeRange) {
        this.lifetimeRange = lifetimeRange;
        return this;
    }

    public Confetti setFadeIn(long fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public Confetti setFadeOut(long fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    public Confetti setRatePerSecond(int ratePerSecond) {
        this.ratePerSecond = ratePerSecond;
        return this;
    }

    public Confetti setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public Confetti setScaleChange(float scaleChange) {
        this.scaleChange = scaleChange;
        return this;
    }

    public Confetti setVelocity(float velocity) {
        this.velocity = velocity;
        return this;
    }

    public Confetti setVelocityRange(float velocityRange) {
        this.velocityRange = velocityRange;
        return this;
    }

    public Confetti setXAcceleration(float xAcceleration) {
        this.xAcceleration = xAcceleration;
        return this;
    }

    public Confetti setYAcceleration(float yAcceleration) {
        this.yAcceleration = yAcceleration;
        return this;
    }

    public Confetti setShootingAngle(float shootingAngle) {
        this.shootingAngle = shootingAngle;
        return this;
    }

    public Confetti setShootingAngleRange(float shootingAngleRange) {
        this.shootingAngleRange = shootingAngleRange;
        return this;
    }

    public Confetti setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public Confetti setRotationSpeedRange(float rotationSpeedRange) {
        this.rotationSpeedRange = rotationSpeedRange;
        return this;
    }

    public Confetti addBurstContent(Drawable drawable) {
        this.burstContent.add(drawable);
        return this;
    }

    public Confetti setTarget(View target) {
        if (target instanceof ViewGroup) {
            this.target = (ViewGroup) target;
        }
        return this;
    }

    @Override
    public void start() {
        super.start();

        if (target == null || burstContent.size() == 0) return;

        ArrayList<ParticleSystem.ParticleTemplate> templates = new ArrayList<>();
        for (int i = 0; i < burstContent.size(); i++) {
            templates.add(new ParticleSystem.ParticleTemplate(burstContent.get(i), (int)(getDuration() / 2)));
        }
        burstParticleSystem = new ParticleSystem(target, templates, 5000)
                .addInitializer(new LifetimeInitializer(lifetimeRange))
                .addInitializer(new XYAccelerationInitializer(xAcceleration, yAcceleration))
                .addModifier(new ScaleModifier(scale, scale + scaleChange, 0, lifetime, new LinearInterpolator()))
                .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange, velocity + 0.5f * velocityRange, (int) (shootingAngle - 0.5 * shootingAngleRange), (int) (shootingAngle + 0.5 * shootingAngleRange))
                .setFadeIn(fadeIn)
                .setFadeOut(fadeOut)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
        ;
        burstParticleSystem.mConcurrentParticlesToActivate = 10;
        burstParticleSystem.mRandomizeParticles = true;
        burstParticleSystem.emit((int)(xPositionStart * target.getWidth()), (int)(xPositionEnd * target.getWidth()), (int)(yPositionStart * target.getHeight()), (int)(yPositionEnd * target.getHeight()), ratePerSecond, (int) getDuration());
//        burstParticleSystem.oneShot(target, 3000);

//
//        if (target != null && content != null) {
//            burstParticleSystem = new ParticleSystem(target, burstContent, 5000);
//            particleSystem = new ParticleSystem(target, (int) (getDuration() * ratePerSecond), lifetime, content.toArray(new Drawable[0]))
//                    .addInitializer(new LifetimeInitializer(lifetimeRange))
//                    .addInitializer(new XYAccelerationInitializer(xAcceleration, yAcceleration))
//                    .addModifier(new ScaleModifier(scale, scale + scaleChange, 0, lifetime, new LinearInterpolator()))
//                    .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange, velocity + 0.5f * velocityRange, (int) (shootingAngle - 0.5 * shootingAngleRange), (int) (shootingAngle + 0.5 * shootingAngleRange))
//                    .setFadeIn(fadeIn)
//                    .setFadeOut(fadeOut)
//                    .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
//            ;
//            particleSystem.emit(xPositionStart, xPositionEnd, yPositionStart, yPositionEnd, ratePerSecond, (int) getDuration());
//        }
    }

//    protected ArrayList<Particle> particlesForBurstPhase1
}