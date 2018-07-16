package boundless.kit.reward.overlay.candybar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import boundless.kit.R;

public class CandybarLayout extends LinearLayout {
    private TextView mMessageView;

    private int mMaxWidth;
    private int mMaxInlineActionWidth;

    interface OnLayoutChangeListener {
        void onLayoutChange(View view, int left, int top, int right, int bottom);
    }

    interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);

        void onViewDetachedFromWindow(View v);
    }

    private OnLayoutChangeListener mOnLayoutChangeListener;
    private OnAttachStateChangeListener mOnAttachStateChangeListener;

    public CandybarLayout(Context context) {
        this(context, null);
    }

    @SuppressLint("PrivateResource")
    public CandybarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
        mMaxInlineActionWidth = a.getDimensionPixelSize(
                R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
        if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
            ViewCompat.setElevation(this, a.getDimensionPixelSize(
                    R.styleable.SnackbarLayout_elevation, 0));
        }
        a.recycle();

        setClickable(true);




        LayoutInflater.from(context)
                .inflate(R.layout.candybar_layout_include, this);

        ViewCompat.setAccessibilityLiveRegion(this,
                ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMessageView = findViewById(R.id.candybar_text);
    }

    TextView getMessageView() {
        return mMessageView;
    }

    @Override
    @SuppressLint("PrivateResource")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mMaxWidth > 0 && getMeasuredWidth() > mMaxWidth) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        final int multiLineVPadding = getResources().getDimensionPixelSize(
                R.dimen.design_snackbar_padding_vertical_2lines);
        final int singleLineVPadding = getResources().getDimensionPixelSize(
                R.dimen.design_snackbar_padding_vertical);
        final boolean isMultiLine = mMessageView.getLayout()
                .getLineCount() > 1;

        boolean remeasure = false;
        if (isMultiLine && mMaxInlineActionWidth > 0) {
            if (updateViewsWithinLayout(VERTICAL, multiLineVPadding,
                    multiLineVPadding - singleLineVPadding)) {
                remeasure = true;
            }
        } else {
            final int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
            if (updateViewsWithinLayout(HORIZONTAL, messagePadding, messagePadding)) {
                remeasure = true;
            }
        }

        if (remeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    void animateChildrenIn(int delay, int duration) {
        mMessageView.setAlpha(0f);
        ViewCompat.animate(mMessageView)
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(delay)
                .start();
    }

    void animateChildrenOut(int delay, int duration) {
        mMessageView.setAlpha(1f);
        ViewCompat.animate(mMessageView)
                .alpha(0f)
                .setDuration(duration)
                .setStartDelay(delay)
                .start();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && mOnLayoutChangeListener != null) {
            mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mOnAttachStateChangeListener != null) {
            mOnAttachStateChangeListener.onViewAttachedToWindow(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mOnAttachStateChangeListener != null) {
            mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
        }
    }

    void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
        mOnLayoutChangeListener = onLayoutChangeListener;
    }

    void setOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
        mOnAttachStateChangeListener = listener;
    }

    private boolean updateViewsWithinLayout(final int orientation,
                                            final int messagePadTop, final int messagePadBottom) {
        boolean changed = false;
        if (orientation != getOrientation()) {
            setOrientation(orientation);
            changed = true;
        }
        if (mMessageView.getPaddingTop() != messagePadTop
                || mMessageView.getPaddingBottom() != messagePadBottom) {
            updateTopBottomPadding(mMessageView, messagePadTop, messagePadBottom);
            changed = true;
        }
        return changed;
    }

    private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
        if (ViewCompat.isPaddingRelative(view)) {
            ViewCompat.setPaddingRelative(view,
                    ViewCompat.getPaddingStart(view), topPadding,
                    ViewCompat.getPaddingEnd(view), bottomPadding);
        } else {
            view.setPadding(view.getPaddingLeft(), topPadding,
                    view.getPaddingRight(), bottomPadding);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}