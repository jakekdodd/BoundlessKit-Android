package boundless.kit.rewards.animation.overlay;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import boundless.kit.R;

public class SheenView extends android.support.v7.widget.AppCompatImageView {

    public int framesPerSecond = 30;
    public long animationDuration = 3300;
    public boolean animateRightToLeft = false;
    public boolean flipSheenImage = false;
    public Interpolator interpolator = new AccelerateDecelerateInterpolator();

    long startTime;
    int animateOverViewId = 0;
    private Bitmap mImage;
    private Bitmap mMask;
    private final Paint maskPaint;
    private final Paint imagePaint;



    public SheenView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setVisibility(GONE);

        maskPaint = new Paint();
        imagePaint = new Paint();
        imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SheenView, 0, 0);
        try {
            framesPerSecond = attributes.getInt(R.styleable.SheenView_framesPerSecond, framesPerSecond);
            animationDuration = attributes.getInt(R.styleable.SheenView_animationDuration, (int)animationDuration);
            animateRightToLeft = attributes.getBoolean(R.styleable.SheenView_animateRightToLeft, animateRightToLeft);
            flipSheenImage = attributes.getBoolean(R.styleable.SheenView_imageHorizontalFlip, flipSheenImage);
            animateOverViewId = attributes.getResourceId(R.styleable.SheenView_animateOver, animateOverViewId);
        } finally {
            attributes.recycle();
        }
    }

    public void setAnimateOverViewId(int viewId) {
        animateOverViewId = viewId;
    }

    public void start() {
        long now = System.currentTimeMillis();
        if (now >= startTime + animationDuration && updateLayout() && updateMask()) {
            this.startTime = now;
            setVisibility(VISIBLE);
            this.postInvalidate();
        }
    }

    protected boolean updateLayout() {
        if (!(getLayoutParams() instanceof RelativeLayout.LayoutParams)) {
            Log.e("Boundless", "SheenView must be in a RelativeLayout. If using a different layout class, wrap SheenView and the view to animate over in a relative layout.");
            return false;
        }

        if (((ViewGroup)getParent()).findViewById(animateOverViewId) == null) {
            Log.e("Boundless", "View to animate over, with id:<" + animateOverViewId + ">, is not a sibling of SheenView. Must be a sibling.");
            return false;
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_TOP, animateOverViewId);
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, animateOverViewId);
        layoutParams.addRule(RelativeLayout.ALIGN_START, animateOverViewId);
        layoutParams.addRule(RelativeLayout.ALIGN_END, animateOverViewId);
        setLayoutParams(layoutParams);
        return true;
    }

    protected boolean updateMask() {
        View view = ((ViewGroup)getParent()).findViewById(animateOverViewId);
        if (view == null) {
            Log.e("Boundless", "View to animate over, with id:<" + animateOverViewId + ">, is not a sibling of SheenView. Must be a sibling.");
            return false;
        }

        view.buildDrawingCache();
        mMask = Bitmap.createBitmap(view.getDrawingCache());

        if (mMask == null) {
            return false;
        }

        setImage(getResources(), R.drawable.sheen);
        return true;
    }

    protected void setImage(Resources res, int id) {
        mImage = BitmapFactory.decodeResource(res, id);
        if (flipSheenImage) {
            mImage = horizontalFlip(mImage);
        }
        mImage = resize(mImage, mMask.getHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long elapsedTime = System.currentTimeMillis() - startTime;

        if(elapsedTime < animationDuration) {
            this.postInvalidateDelayed(1000 / framesPerSecond);
            float interpolation = interpolator.getInterpolation(elapsedTime * 1f/animationDuration);
            float xPos = mImage.getWidth() * (animateRightToLeft ? (-2*interpolation + 1) : (2*interpolation - 1));

            canvas.save();
            canvas.drawBitmap(mMask,0, 0, maskPaint);
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

    private static Bitmap horizontalFlip(Bitmap src) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }
}
