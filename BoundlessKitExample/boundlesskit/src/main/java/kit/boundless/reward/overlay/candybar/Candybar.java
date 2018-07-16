package kit.boundless.reward.overlay.candybar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import kit.boundless.R;


public final class Candybar {

    public static final int DIRECTION_TOP = 0;
    public static final int DIRECTION_BOTTOM = 1;

    @IntDef({DIRECTION_TOP, DIRECTION_BOTTOM}) @Retention(RetentionPolicy.SOURCE)
    public @interface Direction { }

    private final ViewGroup mParent;
    private final Context mContext;
    private final CandybarLayout mView;
    private final int mDirection;
    private int mDuration;
    private Interpolator mInterpolator = new FastOutSlowInInterpolator();
    private boolean mDismissOnTap;
    private Callback mCallback;

    /**
     *
     * @param view View to display Candybar on top of.
     * @param direction Top or bottom of the screen (DIRECTION_TOP or DIRECTION_BOTTOM)
     * @param text Text copy for the body
     * @param duration Time, in milliseconds, to dismiss the Candybar after show() animation.
     */
    public Candybar(@NonNull View view, @Direction int direction, @NonNull CharSequence text, int duration) {
        mParent = findSuitableParent(view);
        mContext = mParent.getContext();
        mDirection = direction;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mView = (CandybarLayout) inflater.inflate((mDirection == DIRECTION_TOP) ? R.layout.candybar_top_layout : R.layout.candybar_bottom_layout, mParent, false);
        setText(text);
        setDuration(duration);
    }

    /**
     * Sets the icon image used on either the left or right of the Candybar
     *
     * @param drawable Drawable image to be used as icon
     * @param leftSide If true icon is on the left, if false the icon is on the right side.
     * @return The object used for Constructor Chaining
     */
    public Candybar setIcon(Drawable drawable, boolean leftSide) {
        if (drawable == null) return this;
        final TextView tv = mView.getMessageView();
        final Drawable[] compoundDrawables = tv.getCompoundDrawables();
        tv.setCompoundDrawables(leftSide ? drawable : compoundDrawables[0], compoundDrawables[1], leftSide ? compoundDrawables[2] : drawable, compoundDrawables[3]);
        return this;
    }

    /**
     * Sets the icon image used on either the left or right of the Candybar
     *
     * @param drawable Drawable image to be used as icon
     * @param sizeDp Size of the icon. Set to -1 for original size.
     * @param leftSide If true icon is on the left, if false the icon is on the right side.
     * @return The object used for Constructor Chaining
     */
    public Candybar setIcon(Drawable drawable, float sizeDp, boolean leftSide) {
        return this.setIcon(fitDrawable(drawable, (int) convertDpToPixel(sizeDp, mContext)), leftSide);
    }

    /**
     * Sets the size of the padding between the icon and the text.
     *
     * @param padding Size of the padding between the icon and the text.
     * @return The object used for Constructor Chaining
     */
    public Candybar setIconPadding(int padding) {
        final TextView tv = mView.getMessageView();
        tv.setCompoundDrawablePadding(padding);
        return this;
    }

    /**
     *
     * @param message The copy to display
     * @return The object used for Constructor Chaining
     */
    @NonNull
    public Candybar setText(@NonNull CharSequence message) {
        final TextView tv = mView.getMessageView();
        tv.setText(message);
        return this;
    }

    /**
     *
     * @param color The text color. Default is white.
     * @return The object used for Constructor Chaining
     */
    @NonNull
    public Candybar setTextColor(int color) {
        getTextView().setTextColor(color);
        return this;
    }

    /**
     *
     * @param size Set the text size to the given value, interpreted as "scaled pixel" units. This size is adjusted based on the current density and user font size preference.
     * @return The object used for Constructor Chaining
     */
    @NonNull
    public Candybar setTextSize(float size) {
        getTextView().setTextSize(size);
        return this;
    }

    /**
     *
     * @param color Set the background color for Candybar view
     * @return The object used for Constructor Chaining
     */
    @NonNull
    public Candybar setBackgroundColor(int color) {
        mView.setBackgroundColor(color);
        return this;
    }

    /**
     *
     * @param alpha Set the transparency for Candybar view
     * @return The object used for Constructor Chaining
     */
    @NonNull
    public Candybar setAlpha(float alpha) {
        mView.setAlpha(alpha);
        return this;
    }

    /**
     *
     * @param duration The time, in milliseconds, to wait after show() to dismiss() the view.
     *                 If set to -1, the view will wait until tapped to dismiss.
     * @return The object used for Constructor Chaining
     */
    @NonNull
    public Candybar setDuration(int duration) {
        mDuration = duration;
        return this;
    }

    /**
     *
     * @param interpolator The rate of change used for animating the view in and out of the screen
     * @return The object used for Constructor Chaining
     */
    @NonNull
    public Candybar setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
        return this;
    }

    @NonNull
    public Candybar setHeight(int pixels) {
        getTextView().setHeight(pixels);
        return this;
    }

    @NonNull
    public Candybar setDismissOnTap(boolean dismissOnTap) {
        mDismissOnTap = dismissOnTap;
        return this;
    }

    /**
     * Animate the Candybar view onto the screen and reward the user.
     * After the determined duration, Candybar automatically dismisses itself.
     */
    public void show() {
        CandybarManager.getInstance().show(mDuration, mManagerCallback);
    }

    /**
     * Manually dismiss the Candybar. After animating out, the view removes itself from its parent.
     */
    public void dismiss() {
        CandybarManager.getInstance().dismiss(mManagerCallback, Callback.DISMISS_EVENT_MANUAL);
    }

    @NonNull
    public View getView() {
        return mView;
    }

    @NonNull
    public TextView getTextView() {
        return (TextView) mView.findViewById(R.id.candybar_text);
    }

    public boolean isShown() {
        return CandybarManager.getInstance().isCurrentOrNext(mManagerCallback);
    }

    /*
    Callback functionality. Use `candybar.setCallback(new Callback())` to set an event callback.
     */

    @NonNull
    public Candybar setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    public static abstract class Callback {
        public static final int DISMISS_EVENT_TAP = 1;
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        @IntDef({DISMISS_EVENT_TAP, DISMISS_EVENT_TIMEOUT, DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE}) @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent { }

        public void onShown(Candybar candybar) { }

        public void onDismissed(Candybar candybar, @DismissEvent int event) { }
    }

    /*
    Code handling Looper for animating
     */
    private static final Handler sHandler;
    private static final int MSG_SHOW = 0;
    private static final int MSG_DISMISS = 1;

    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((Candybar) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((Candybar) message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    /*
    Instance of CandybarManager that will ensure only one Candybar is shown at a time
     */
    private final CandybarManager.Callback mManagerCallback = new CandybarManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, Candybar.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, Candybar.this));
        }
    };

    /*
        Animation code
         */
    private int mAnimationDuration = 250;
    private int mAnimationFadeDuration = 180;

    /**
     *
     * @param mAnimationDuration The time taken to animate the Candybar background view onto the screen.
     * @return The object used for Constructor Chaining
     */
    public Candybar setmAnimationDuration(int mAnimationDuration) {
        this.mAnimationDuration = mAnimationDuration;
        return this;
    }

    /**
     *
     * @param mAnimationFadeDuration The time to animate the Candybar content (icon and text) onto the view.
     * @return The object used for Constructor Chaining
     */
    public Candybar setmAnimationFadeDuration(int mAnimationFadeDuration) {
        this.mAnimationFadeDuration = mAnimationFadeDuration;
        return this;
    }

    private void showView() {
        if (mView.getParent() == null) {
            mParent.addView(mView);
        }

        mView.setOnAttachStateChangeListener(new CandybarLayout.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (isShown()) {
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onViewHidden(Callback.DISMISS_EVENT_MANUAL);
                        }
                    });
                }
            }
        });

        if (ViewCompat.isLaidOut(mView)) {
            animateViewIn();
        } else {
            mView.setOnLayoutChangeListener(new CandybarLayout.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    animateViewIn();
                    mView.setOnLayoutChangeListener(null);
                }
            });
        }

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && mDismissOnTap) {
                    animateViewOut(Callback.DISMISS_EVENT_TAP);
                    view.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    private void animateViewIn() {
        float startingYTranslation = mView.getHeight() * ((mDirection == DIRECTION_TOP) ? -1 : 1);
        mView.setTranslationY(startingYTranslation);
        ViewCompat.animate(mView)
                .translationY(0f)
                .setInterpolator(mInterpolator)
                .setDuration(mAnimationDuration)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(View view) {
                        mView.animateChildrenIn(mAnimationDuration - mAnimationFadeDuration,
                                mAnimationFadeDuration);
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        if (mCallback != null) {
                            mCallback.onShown(Candybar.this);
                        }
                        CandybarManager.getInstance()
                                .onShown(mManagerCallback);
                    }
                })
                .start();
    }

    private void animateViewOut(final int event) {
        float endingYTranslation = mView.getHeight() * ((mDirection == DIRECTION_TOP) ? -1 : 1);
        ViewCompat.animate(mView)
                .translationY(endingYTranslation)
                .setInterpolator(mInterpolator)
                .setDuration(mAnimationDuration)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(View view) {
                        mView.animateChildrenOut(0, mAnimationFadeDuration);
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        onViewHidden(event);
                    }
                })
                .start();
    }

    private void hideView(int event) {
        if (mView.getVisibility() != View.VISIBLE) {
            onViewHidden(event);
        } else {
            animateViewOut(event);
        }
    }

    private void onViewHidden(int event) {
        CandybarManager.getInstance().onDismissed(mManagerCallback);
        if (mCallback != null) {
            mCallback.onDismissed(this, event);
        }
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
    }

    /*
    Helper methods for insertion node finding and view sizing
     */
    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                } else {
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);


        return fallback;
    }

    private Drawable fitDrawable(Drawable drawable, int sizePx) {
        if (drawable.getIntrinsicWidth() != sizePx || drawable.getIntrinsicHeight() != sizePx) {
            if (drawable instanceof BitmapDrawable) {
                drawable = new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(getBitmap(drawable), sizePx, sizePx, true));
            }
        }
        drawable.setBounds(0, 0, sizePx, sizePx);
        return drawable;
    }

    private static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
}