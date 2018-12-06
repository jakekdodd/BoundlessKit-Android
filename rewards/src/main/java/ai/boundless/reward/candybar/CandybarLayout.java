package ai.boundless.reward.candybar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import ai.boundless.R;

/**
 * The type Candybar layout.
 */
public class CandybarLayout extends LinearLayout {
  private TextView mMessageView;

  private int mMaxWidth;
  private int mMaxInlineActionWidth;
  private OnLayoutChangeListener mOnLayoutChangeListener;
  private OnAttachStateChangeListener mOnAttachStateChangeListener;

  /**
   * Instantiates a new Candybar layout.
   *
   * @param context the context
   */
  public CandybarLayout(Context context) {
    this(context, null);
  }

  /**
   * Instantiates a new Candybar layout.
   *
   * @param context the context
   * @param attrs the attrs
   */
  @SuppressLint("PrivateResource")
  public CandybarLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    @SuppressLint("CustomViewStyleable") TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
    mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
    mMaxInlineActionWidth =
        a.getDimensionPixelSize(R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
    if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
      ViewCompat.setElevation(this,
          a.getDimensionPixelSize(R.styleable.SnackbarLayout_elevation, 0)
      );
    }
    a.recycle();

    setClickable(true);


    LayoutInflater.from(context).inflate(R.layout.candybar_layout_include, this);

    ViewCompat.setAccessibilityLiveRegion(this, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
  }

  /**
   * Gets message view.
   *
   * @return the message view
   */
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

    final int multiLineVPadding =
        getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical_2lines);
    final int singleLineVPadding =
        getResources().getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical);
    final boolean isMultiLine = mMessageView.getLayout().getLineCount() > 1;

    boolean remeasure = false;
    if (isMultiLine && mMaxInlineActionWidth > 0) {
      if (updateViewsWithinLayout(VERTICAL,
          multiLineVPadding,
          multiLineVPadding - singleLineVPadding
      )) {
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

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (changed && mOnLayoutChangeListener != null) {
      mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
    }
  }

  private boolean updateViewsWithinLayout(
      final int orientation, final int messagePadTop, final int messagePadBottom) {
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
          ViewCompat.getPaddingStart(view),
          topPadding,
          ViewCompat.getPaddingEnd(view),
          bottomPadding
      );
    } else {
      view.setPadding(view.getPaddingLeft(), topPadding, view.getPaddingRight(), bottomPadding);
    }
  }

  /**
   * Animate children in.
   *
   * @param delay the delay
   * @param duration the duration
   */
  void animateChildrenIn(int delay, int duration) {
    mMessageView.setAlpha(0f);
    ViewCompat.animate(mMessageView).alpha(1f).setDuration(duration).setStartDelay(delay).start();
  }

  /**
   * Animate children out.
   *
   * @param delay the delay
   * @param duration the duration
   */
  void animateChildrenOut(int delay, int duration) {
    mMessageView.setAlpha(1f);
    ViewCompat.animate(mMessageView).alpha(0f).setDuration(duration).setStartDelay(delay).start();
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

  /**
   * Sets on layout change listener.
   *
   * @param onLayoutChangeListener the on layout change listener
   */
  void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
    mOnLayoutChangeListener = onLayoutChangeListener;
  }

  /**
   * Sets on attach state change listener.
   *
   * @param listener the listener
   */
  void setOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
    mOnAttachStateChangeListener = listener;
  }

  @Override
  public boolean performClick() {
    return super.performClick();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mMessageView = findViewById(R.id.candybar_text);
  }

  /**
   * The interface On layout change listener.
   */
  interface OnLayoutChangeListener {
    /**
     * On layout change.
     *
     * @param view the view
     * @param left the left
     * @param top the top
     * @param right the right
     * @param bottom the bottom
     */
    void onLayoutChange(View view, int left, int top, int right, int bottom);
  }

  /**
   * The interface On attach state change listener.
   */
  interface OnAttachStateChangeListener {
    /**
     * On view attached to window.
     *
     * @param v the v
     */
    void onViewAttachedToWindow(View v);

    /**
     * On view detached from window.
     *
     * @param v the v
     */
    void onViewDetachedFromWindow(View v);
  }
}
