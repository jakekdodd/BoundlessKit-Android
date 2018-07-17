package kit.boundless.reward.overlay;

import android.animation.Animator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.ColorUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import kit.boundless.reward.BaseViewAnimator;
import kit.boundless.reward.overlay.particle.ConfettoDrawable;
import kit.boundless.reward.overlay.particle.ParticleSystem;
import kit.boundless.reward.overlay.particle.initializers.AlphaInitializer;
import kit.boundless.reward.overlay.particle.initializers.LifetimeInitializer;
import kit.boundless.reward.overlay.particle.initializers.ScaleInitializer;
import kit.boundless.reward.overlay.particle.initializers.XYAccelerationInitializer;
import kit.boundless.reward.overlay.particle.modifiers.XYAccelerationModifier;

public class Confetti extends BaseViewAnimator<Confetti> {

    public static Confetti demo(View target) {
        return new Confetti().addConfetti(
                50,
                50,
                Arrays.asList(
                        ConfettoDrawable.Shape.RECTANGLE,
                        ConfettoDrawable.Shape.RECTANGLE,
                        ConfettoDrawable.Shape.SPIRAL,
                        ConfettoDrawable.Shape.CIRCLE
                ),
                Arrays.asList(
                        ColorUtils.setAlphaComponent(Color.parseColor("#4d81fb"), 204),
                        ColorUtils.setAlphaComponent(Color.parseColor("#4ac4fb"), 204),
                        ColorUtils.setAlphaComponent(Color.parseColor("#9243f9"), 204),
                        ColorUtils.setAlphaComponent(Color.parseColor("#fdc33b"), 204),
                        ColorUtils.setAlphaComponent(Color.parseColor("#f7332f"), 204)
                )
        ).setTarget(target);
    }

    ParticleSystem burstParticleSystem;
    ParticleSystem showerParticleSystem;
    ParticleSystem blurredShowerParticleSystem;

    long mDuration;
    float xPositionStart = 0f;
    float xPositionEnd = 1f;
    float yPositionStart = -0.2f;
    float yPositionEnd = 0f;
    ArrayList<ParticleSystem.DrawableParticleTemplate> content = new ArrayList<>();
    private ViewGroup target;
    { setDuration(2000); }
    int ratePerSecond = 600;

    @Override
    public Confetti setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    public Confetti setyPositionEnd(float yPositionEnd) {
        this.yPositionEnd = yPositionEnd;
        return this;
    }

    public Confetti setxPositionStart(float xPositionStart) {
        this.xPositionStart = xPositionStart;
        return this;
    }

    public Confetti setxPositionEnd(float xPositionEnd) {
        this.xPositionEnd = xPositionEnd;
        return this;
    }

    public Confetti setyPositionStart(float yPositionStart) {
        this.yPositionStart = yPositionStart;
        return this;
    }

    public Confetti setRatePerSecond(int ratePerSecond) {
        this.ratePerSecond = ratePerSecond;
        return this;
    }

    public Confetti addConfetti(int width, int height, List<ConfettoDrawable.Shape> shapes, List<Integer> colors) {
        for (ConfettoDrawable.Shape shape : shapes) {
            for (Integer color : colors) {
                addContent(new ConfettoDrawable(shape, width, height, color));
            }
        }
        return this;
    }

    public Confetti addContent(Drawable drawable) {
        return this.addContent(drawable, 10);
    }

    public Confetti addContent(Drawable drawable, int count) {
        this.content.add(new ParticleSystem.DrawableParticleTemplate(drawable, count * ratePerSecond));
        return this;
    }

    public Confetti setTarget(View target) {
        if (target instanceof ViewGroup) {
            this.target = (ViewGroup) target;
            setBurstTarget();
            setShowerTarget();
            setBlurredShowerTarget();
        }
        return this;
    }

    @Override
    public void start() {
        if (getAnimator().isStarted() || target == null || content.size() == 0) return;

        setBurstEmitter();
        setShowerEmitter();
        setBlurredShowerEmitter();

        List<Animator> list = new ArrayList<>();
        if (burstParticleSystem != null) list.add(burstParticleSystem.getAnimator());
        if (showerParticleSystem!= null) list.add(showerParticleSystem.getAnimator());
        if (blurredShowerParticleSystem != null) list.add(blurredShowerParticleSystem.getAnimator());
        getAnimator().playTogether(list);

        super.start();
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

        burstParticleSystem = new ParticleSystem(target, content, lifetime)
                .addInitializer(new LifetimeInitializer(lifetimeRange))
                .addInitializer(new ScaleInitializer(scale - 0.5f * scaleRange, scale + 0.5f * scaleRange))
                .addModifier(new XYAccelerationModifier(
                        0,
                        yInitialAcceleration,
                        0,
                        yFinalAcceleration,
                        0,
                        lifetime - lifetimeRange,
                        new AnticipateInterpolator()))
                .setSpeedModuleAndAngleRange(
                        velocity - 0.5f * velocityRange,
                        velocity + 0.5f * velocityRange,
                        (int) (shootingAngle - 0.5 * shootingAngleRange),
                        (int) (shootingAngle + 0.5 * shootingAngleRange))
                .setFadeIn(200)
                .setFadeOut(100)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
                .setRandomParticleSelection(true)
        ;
    }
    private void setBurstEmitter() {
        if (burstParticleSystem == null || burstParticleSystem.isRunning())
            return;
        long duration = 800;
        burstParticleSystem.setManuallyStartAnimator(true);
        burstParticleSystem.emit(
                (int)(xPositionStart * target.getWidth()),
                (int)(xPositionEnd * target.getWidth()),
                (int)(yPositionStart * target.getHeight()),
                (int)(yPositionEnd * target.getHeight()),
                ratePerSecond,
                (int)duration
        );
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

        showerParticleSystem = new ParticleSystem(target, content, lifetime)
                .addInitializer(new LifetimeInitializer(lifetimeRange))
                .addInitializer(new XYAccelerationInitializer(0, yAcceleration))
                .addInitializer(new ScaleInitializer(scale - 0.5f * scaleRange, scale + 0.5f * scaleRange))
                .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange, velocity + 0.5f * velocityRange, (int) (shootingAngle - 0.5 * shootingAngleRange), (int) (shootingAngle + 0.5 * shootingAngleRange))
                .setFadeIn(200)
                .setFadeOut(100)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
                .setRandomParticleSelection(true)
                .setStartDelay(800)
        ;
    }
    private void setShowerEmitter() {
        if (showerParticleSystem == null || showerParticleSystem.isRunning())
            return;
        long duration = Math.max(0, getDuration() - 800);
        showerParticleSystem.setManuallyStartAnimator(true);
        showerParticleSystem.emit(
                (int)(xPositionStart * target.getWidth()),
                (int)(xPositionEnd * target.getWidth()),
                (int)(yPositionStart * target.getHeight()),
                (int)(yPositionEnd * target.getHeight()),
                ratePerSecond,
                (int)duration
        );
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
                blurredContent.add(new ParticleSystem.BlurredDrawableParticleTemplate(content.get(i).drawable, 1,rand + 1, rand * 4));
            }
        }

        blurredShowerParticleSystem = new ParticleSystem(target, blurredContent, lifetime)
                .addInitializer(new AlphaInitializer(76, 38))
                .addInitializer(new XYAccelerationInitializer(0, yAcceleration))
                .addInitializer(new ScaleInitializer(scale - 0.5f * scaleRange, scale + 0.5f * scaleRange))
                .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange, velocity + 0.5f * velocityRange, (int) shootingAngle, (int) shootingAngle)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
                .setFadeIn(200)
                .setFadeOut(100)
                .setRandomParticleSelection(true)
                .setStartDelay(800)
        ;
    }
    private void setBlurredShowerEmitter() {
        if (blurredShowerParticleSystem == null || blurredShowerParticleSystem.isRunning())
            return;
        long duration = Math.max(0, getDuration() - 800);
        blurredShowerParticleSystem.setManuallyStartAnimator(true);
        blurredShowerParticleSystem.emit(
                (int)(xPositionStart * target.getWidth()),
                (int)(xPositionEnd * target.getWidth()),
                (int)(yPositionStart * target.getHeight()),
                (int)(yPositionEnd * target.getHeight()),
                3,
                (int)duration
        );
    }

}