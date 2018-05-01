package boundless.kit.rewards.animation.particle;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

public class Emojisplosion {

    private int x = 0;
    private int y = 0;
    private Drawable content;

    public Emojisplosion(int x, int y, Drawable content) {
        this.x = x;
        this.y = y;
        this.content = content;
    }

    public void prepare(ViewGroup target) {
//        target.add
        new ParticleSystem(target, 2, content, 2000)
                .setSpeedRange(0.2f, 0.5f)
                .oneShot(target, 2);
    }

}
