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

    private void setImage(Resources res, int id) {
        mImage = resize(BitmapFactory.decodeResource(res, id), mMask.getHeight());
//        mImage = BitmapFactory.decodeResource(res, id);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(mMask.getWidth(), mMask.getHeight());
//    }

    public void setMask(View view) {
        view.buildDrawingCache();
        mMask = Bitmap.createBitmap(view.getDrawingCache());

        setImage(view.getResources(), R.drawable.sheen);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);



        canvas.save();
        canvas.drawBitmap(mMask, 0, 0, maskPaint);
        canvas.drawBitmap(mImage, 0, 0, imagePaint);
        canvas.restore();
    }

    private static Bitmap resize(Bitmap image, int maxHeight) {
        if (maxHeight > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratio = (float) maxHeight / (float) height;

            int finalWidth = (int) ((float)ratio * width);
            image = Bitmap.createScaledBitmap(image, finalWidth, maxHeight, true);
            return image;
        } else {
            return image;
        }
    }
}
