package boundless.kit.rewards.animation.overlay;

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

import boundless.kit.rewards.animation.BaseViewAnimator;
import boundless.kit.rewards.animation.overlay.particle.ConfettoDrawable;
import boundless.kit.rewards.animation.overlay.particle.ParticleSystem;
import boundless.kit.rewards.animation.overlay.particle.initializers.AlphaInitializer;
import boundless.kit.rewards.animation.overlay.particle.initializers.LifetimeInitializer;
import boundless.kit.rewards.animation.overlay.particle.initializers.ScaleInitializer;
import boundless.kit.rewards.animation.overlay.particle.initializers.XYAccelerationInitializer;
import boundless.kit.rewards.animation.overlay.particle.modifiers.XYAccelerationModifier;

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
    boolean burstPhaseEnabled = true;
    ParticleSystem showerParticleSystem;
    boolean showerPhaseEnabled = true;
    ParticleSystem blurredShowerParticleSystem;
    boolean outOfFocusPhaseEnabled = true;

    float xPositionStart = 0f;
    float xPositionEnd = 1f;
    float yPositionStart = -0.2f;
    float yPositionEnd = 0f;
    ArrayList<ParticleSystem.DrawableParticleTemplate> content = new ArrayList<>();
    private ViewGroup target;
    { setDuration(2000); }
    int ratePerSecond = 600;


    public Confetti setBurstPhaseEnabled(boolean burstPhaseEnabled) {
        this.burstPhaseEnabled = burstPhaseEnabled;
        return this;
    }

    public Confetti setShowerPhaseEnabled(boolean showerPhaseEnabled) {
        this.showerPhaseEnabled = showerPhaseEnabled;
        return this;
    }

    public Confetti setOutOfFocusPhaseEnabled(boolean outOfFocusPhaseEnabled) {
        this.outOfFocusPhaseEnabled = outOfFocusPhaseEnabled;
        return this;
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
            setBurst();
            setShower();
            setBlurredShower();
        }
        return this;
    }

    @Override
    public void start() {
        if (target == null || content.size() == 0) return;


        startBurst();
        startShower();
        startBlurredShower();
        super.start();
    }

    private void setBurst() {
        if (!burstPhaseEnabled) return;
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
    private void startBurst() {
        if (!burstPhaseEnabled || burstParticleSystem == null || burstParticleSystem.isRunning())
            return;
        long duration = 800;
        burstParticleSystem.emit(
                (int)(xPositionStart * target.getWidth()),
                (int)(xPositionEnd * target.getWidth()),
                (int)(yPositionStart * target.getHeight()),
                (int)(yPositionEnd * target.getHeight()),
                ratePerSecond,
                (int)duration
        );
    }

    private void setShower() {
        if (!showerPhaseEnabled) return;
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
    private void startShower() {
        if (!showerPhaseEnabled || showerParticleSystem == null || showerParticleSystem.isRunning())
            return;
        long duration = Math.max(0, getDuration() - 800);
        showerParticleSystem.emit(
                (int)(xPositionStart * target.getWidth()),
                (int)(xPositionEnd * target.getWidth()),
                (int)(yPositionStart * target.getHeight()),
                (int)(yPositionEnd * target.getHeight()),
                ratePerSecond,
                (int)duration
        );
    }

    private void setBlurredShower() {
        if (!outOfFocusPhaseEnabled) return;
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
    private void startBlurredShower() {
        if (!outOfFocusPhaseEnabled || blurredShowerParticleSystem == null || blurredShowerParticleSystem.isRunning())
            return;
        long duration = Math.max(0, getDuration() - 800);
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