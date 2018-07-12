package boundless.kit.rewards.animation.overlay;

import android.graphics.Bitmap;
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
import boundless.kit.rewards.animation.overlay.particle.initializers.LifetimeInitializer;
import boundless.kit.rewards.animation.overlay.particle.initializers.ScaleInitializer;
import boundless.kit.rewards.animation.overlay.particle.initializers.XYAccelerationInitializer;
import boundless.kit.rewards.animation.overlay.particle.modifiers.XYAccelerationModifier;

public class Confetti extends BaseViewAnimator<Confetti> {

    ParticleSystem burstParticleSystem;
    ParticleSystem showerParticleSystem;
    ParticleSystem blurredShowerParticleSystem;

    float xPositionStart = 0f;
    float xPositionEnd = 1f;
    float yPositionStart = -0.2f;
    float yPositionEnd = 0f;
    ArrayList<ParticleSystem.DrawableParticleTemplate> content = new ArrayList<>();
    private ViewGroup target;
    { setDuration(5000); }
    int ratePerSecond = 300;
    long fadeIn = 250;
    long fadeOut = 500;

    public static Confetti demo(View target) {
        return new Confetti()
                .addConfetti(
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
                )
                .setTarget(target);
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
        if (target == null || content.size() == 0 || getAnimator().isRunning()) return;


        startBurst();
        startShower();
        startBlurredShower();
        getAnimator().playTogether(burstParticleSystem.getAnimator(), showerParticleSystem.getAnimator(), blurredShowerParticleSystem.getAnimator());
        super.start();
    }

    private void setBurst() {
        int birthRate = 12;
        long lifetime = 5000;
        long lifetimeRange = 1000;
        float velocity = 0.2f;
        float velocityRange = 0.001f;
        float yInitialAcceleration = -0.0001f;
        float yFinalAcceleration = 0.0004f;
        float shootingAngle = 90f;
        float shootingAngleRange = 45f;
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
                        -1,
                        new AnticipateInterpolator()))
                .setSpeedModuleAndAngleRange(
                        velocity - 0.5f * velocityRange,
                        velocity + 0.5f * velocityRange,
                        (int) (shootingAngle - 0.5 * shootingAngleRange),
                        (int) (shootingAngle + 0.5 * shootingAngleRange))
                .setFadeIn(fadeIn)
                .setFadeOut(fadeOut)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
                .setConcurrentBirths(birthRate)
                .setRandomParticleSelection(true)
                .setManuallyStartAnimator(true)
        ;
    }
    private void startBurst() {
        long duration = 800;
        if (burstParticleSystem == null) return;
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
        long lifetime = 3000;
        int birthRate = 20;
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
                .setFadeIn(fadeIn)
                .setFadeOut(fadeOut)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
                .setConcurrentBirths(birthRate)
                .setRandomParticleSelection(true)
                .setManuallyStartAnimator(true)
                .setStartDelay(800)
        ;
    }
    private void startShower() {
        long duration = Math.max(0, getDuration() - 800);
        if (showerParticleSystem == null) return;
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
        long lifetime = 3000;
        int birthRate = 3;
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
                blurredContent.add(new ParticleSystem.BlurredDrawableParticleTemplate(content.get(i), rand + 1, rand * 10));
            }
        }

        blurredShowerParticleSystem = new ParticleSystem(target, blurredContent, lifetime)
                .addInitializer(new XYAccelerationInitializer(0, yAcceleration))
                .addInitializer(new ScaleInitializer(scale - 0.5f * scaleRange, scale + 0.5f * scaleRange))
                .setSpeedModuleAndAngleRange(velocity - 0.5f * velocityRange, velocity + 0.5f * velocityRange, (int) shootingAngle, (int) shootingAngle)
                .setFadeIn(fadeIn)
                .setFadeOut(fadeOut)
                .setRotationSpeed(rotationSpeed - 0.5f * rotationSpeedRange, rotationSpeed + 0.5f * rotationSpeedRange)
                .setConcurrentBirths(birthRate)
                .setRandomParticleSelection(true)
                .setManuallyStartAnimator(true)
                .setStartDelay(800)
        ;
    }

    private void startBlurredShower() {
        long duration = Math.max(0, getDuration() - 800);
        if (blurredShowerParticleSystem == null) return;
        blurredShowerParticleSystem.emit(
                (int)(xPositionStart * target.getWidth()),
                (int)(xPositionEnd * target.getWidth()),
                (int)(yPositionStart * target.getHeight()),
                (int)(yPositionEnd * target.getHeight()),
                3,
                (int)duration
        );
    }


    /*
     * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
     */

    public Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}