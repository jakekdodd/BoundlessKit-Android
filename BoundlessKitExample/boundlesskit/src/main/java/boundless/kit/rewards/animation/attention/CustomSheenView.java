package boundless.kit.rewards.animation.attention;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import boundless.kit.R;

public class CustomSheenView extends android.support.v7.widget.AppCompatImageView {

    int framesPerSecond = 30;
    long animationDuration = 3000;
    long startTime;
    float mX = 0;

    private Bitmap mImage;
    private Bitmap mMask;
    private final Paint maskPaint;
    private final Paint imagePaint;

    public CustomSheenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        maskPaint = new Paint();
//        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        imagePaint = new Paint();
        imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setMask(View view) {
        if (view == null) { return; }
        view.buildDrawingCache();
        mMask = Bitmap.createBitmap(view.getDrawingCache());

        if (mMask != null) {
            setImage(view.getResources(), R.drawable.sheen);
            mX = -mMask.getWidth();
        }
    }

    private void setImage(Resources res, int id) {
        mImage = resize(BitmapFactory.decodeResource(res, id), mMask.getHeight());
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long elapsedTime = System.currentTimeMillis() - startTime;

        if(elapsedTime < animationDuration) {
            this.postInvalidateDelayed(1000 / framesPerSecond);
            mX += 10;

            canvas.save();
            canvas.drawBitmap(mMask, 0, 0, maskPaint);
            canvas.drawBitmap(mImage, mX, 0, imagePaint);
            canvas.restore();
        }
    }

    private static Bitmap resize(Bitmap image, int maxHeight) {
        if (maxHeight > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratio = (float) maxHeight / (float) height;

            int finalWidth = (int) (ratio * width);
            image = Bitmap.createScaledBitmap(image, finalWidth, maxHeight, true);
            return image;
        } else {
            return image;
        }
    }
}
