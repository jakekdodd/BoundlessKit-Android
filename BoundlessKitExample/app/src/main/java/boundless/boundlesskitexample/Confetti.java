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

    ParticleSystem particleSystem;

    float xPositionStart = 0f;
    float xPositionEnd = 1f;
    float yPositionStart = -0.2f;
    float yPositionEnd = -0.05f;
    ArrayList<ParticleSystem.ParticleTemplate> content = new ArrayList<>();
    private ViewGroup target;
    { setDuration(3000); }
    int ratePerSecond = 300;
    long lifetime = 2000;
    long lifetimeRange = 1000;
    long fadeIn = 250;
    long fadeOut = 500;
    float scale = 1f;
    float scaleChange = 1.2f;
    float velocity = 0.25f;
    float velocityRange = 0.001f;
    float xAcceleration = 0;
    float yAcceleration = 0;
    float shootingAngle = 90f;
    float shootingAngleRange = 45f;
    float rotationSpeed = 10f;
    float rotationSpeedRange = 130f;

    public Confetti addContent(Drawable drawable) {
        return this.addContent(drawable, 10);
    }

    public Confetti addContent(Drawable drawable, int count) {
        this.content.add(new ParticleSystem.ParticleTemplate(drawable, count * ratePerSecond));
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

        if (target == null || content.size() == 0) return;

        particleSystem = new ParticleSystem(target, content, 5000)
                .addInitializer(new LifetimeInitializer(lifetimeRange))
                .addInitializer(new XYAccelerationInitializer(xAcceleration, yAcceleration))
                .addModifier(new ScaleModifier(scale, scale + scaleChange, 0, lifetime, new LinearInterpolator()))
                .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange, velocity + 0.5f * velocityRange, (int) (shootingAngle - 0.5 * shootingAngleRange), (int) (shootingAngle + 0.5 * shootingAngleRange))
                .setFadeIn(fadeIn)
                .setFadeOut(fadeOut)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
        ;
        particleSystem.mConcurrentParticlesToActivate = 10;
        particleSystem.mRandomizeParticles = true;
        particleSystem.emit((int)(xPositionStart * target.getWidth()), (int)(xPositionEnd * target.getWidth()), (int)(yPositionStart * target.getHeight()), (int)(yPositionEnd * target.getHeight()), ratePerSecond, (int) getDuration());
    }

//    protected ArrayList<Particle> particlesForBurstPhase1
}