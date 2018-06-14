package boundless.kit.rewards.animation.attention;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import boundless.kit.R;

public class CustomSheenView extends android.support.v7.widget.AppCompatImageView {

    int framesPerSecond = 30;
    long animationDuration = 3300;
    long startTime;
    Interpolator interpolator = new AccelerateDecelerateInterpolator();

    int sheenContainerViewId;
    private Bitmap mImage;
    private Bitmap mMask;
    private final Paint maskPaint;
    private final Paint imagePaint;

    public CustomSheenView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setVisibility(GONE);

        maskPaint = new Paint();
//        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        imagePaint = new Paint();
        imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.boundlessStyle, 0, 0);
        try {
            sheenContainerViewId = ta.getResourceId(R.styleable.boundlessStyle_sheenContainerView, 0);
        } finally {
            ta.recycle();
        }
    }

    public void setMask(View view) {
        view.buildDrawingCache();
        mMask = Bitmap.createBitmap(view.getDrawingCache());

        if (mMask != null) {
            setImage(getResources(), R.drawable.sheen);
        }
    }

    private void setImage(Resources res, int id) {
        mImage = resize(BitmapFactory.decodeResource(res, id), mMask.getHeight());
    }

    public void start() {
        long now = System.currentTimeMillis();
        if (now >= startTime + animationDuration) {
            if ( !(getParent() instanceof View) || ((View) getParent()).findViewById(sheenContainerViewId) == null) {
                Log.e("Boundless", "SheenView does not have a parent view, or has an invalid or missing 'sheenContainerView' attribute");
                return;
            }
            setMask(((View) getParent()).findViewById(sheenContainerViewId));
            this.startTime = now;
            setVisibility(VISIBLE);
            this.postInvalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long elapsedTime = System.currentTimeMillis() - startTime;

        if(elapsedTime < animationDuration) {
            this.postInvalidateDelayed(1000 / framesPerSecond);
            float interpolation = interpolator.getInterpolation(elapsedTime * 1f/animationDuration);
            float xPos = mImage.getWidth() * (2*interpolation - 1);

            canvas.save();
            canvas.drawBitmap(mMask, 0, 0, maskPaint);
            canvas.drawBitmap(mImage, xPos, 0, imagePaint);
            canvas.restore();
        } else {
            setVisibility(GONE);
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
