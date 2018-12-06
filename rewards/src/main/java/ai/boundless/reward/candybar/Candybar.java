package ai.boundless.reward.candybar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
import ai.boundless.R;

/**
 * Candybars provide lightweight reinforcement to users for an action. They show a brief message at
 * the top or bottom of the screen. Candybars appear above all other elements on screen and only one
 * can be displayed at a time.
 * They automatically disappear after a timeout or after user taps on the candybar view.
 * To be notified when a candybar has been shown or dismissed, you can provide a {@link Callback}
 * via {@link #setCallback(Callback)}.
 */
public final class Candybar {

  /**
   * The constant DIRECTION_TOP.
   */
  public static final int DIRECTION_TOP = 0;
  /**
   * The constant DIRECTION_BOTTOM.
   */
  public static final int DIRECTION_BOTTOM = 1;
  /*
  Code handling Looper for animating
   */
  private static final Handler S_HANDLER;
  private static final int MSG_SHOW = 0;
  private static final int MSG_DISMISS = 1;

  static {
    S_HANDLER = new Handler(Looper.getMainLooper(), new Handler.Callback() {
      @Override
      public boolean handleMessage(Message message) {
        switch (message.what) {
          case MSG_SHOW:
            ((Candybar) message.obj).showView();
            return true;
          case MSG_DISMISS:
            ((Candybar) message.obj).hideView(message.arg1);
            return true;
          default:
        }
        return false;
      }
    });
  }

  private final ViewGroup mParent;
  private final Context mContext;
  private final CandybarLayout mView;
  private final int mDirection;
  /*
  Instance of CandybarManager that will ensure only one Candybar is shown at a time
   */
  private final CandybarManager.Callback mManagerCallback = new CandybarManager.Callback() {
    @Override
    public void show() {
      S_HANDLER.sendMessage(S_HANDLER.obtainMessage(MSG_SHOW, Candybar.this));
    }

    @Override
    public void dismiss(int event) {
      S_HANDLER.sendMessage(S_HANDLER.obtainMessage(MSG_DISMISS, event, 0, Candybar.this));
    }
  };
  private int mDuration;
  private Interpolator mInterpolator = new FastOutSlowInInterpolator();
  private boolean mDismissOnTap;
  private Callback mCallback;
  /*
      Animation code
       */
  private int mAnimationDuration = 250;
  private int mAnimationFadeDuration = 180;

  /**
   * Instantiates a new Candybar.
   *
   * @param view View to display Candybar on top of. Usually the activity's content view.
   * @param direction Top or bottom of the screen ({@link #DIRECTION_TOP} or {@link
   *     #DIRECTION_TOP})
   * @param text Text copy for the body
   * @param duration Time, in milliseconds, to dismiss the Candybar after show() animation.
   */
  public Candybar(
      @NonNull View view, @Direction int direction, @NonNull CharSequence text, int duration) {
    mParent = findSuitableParent(view);
    mContext = mParent.getContext();
    mDirection = direction;
    LayoutInflater inflater = LayoutInflater.from(mContext);
    mView = (CandybarLayout) inflater.inflate((mDirection == DIRECTION_TOP)
        ? R.layout.candybar_top_layout
        : R.layout.candybar_bottom_layout, mParent, false);
    setText(text);
    setDuration(duration);
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

  /**
   * Sets text.
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
   * Sets duration.
   *
   * @param duration The time, in milliseconds, to wait after show() to dismiss() the view.
   *     If set to -1, the view will wait until tapped to dismiss.
   * @return The object used for Constructor Chaining
   */
  @NonNull
  public Candybar setDuration(int duration) {
    mDuration = duration;
    return this;
  }

  /**
   * Sets the icon image used on either the left or right of the Candybar.
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
   * Sets the icon image used on either the left or right of the Candybar.
   *
   * @param drawable Drawable image to be used as icon
   * @param leftSide If true icon is on the left, if false the icon is on the right side.
   * @return The object used for Constructor Chaining
   */
  public Candybar setIcon(Drawable drawable, boolean leftSide) {
    if (drawable == null) {
      return this;
    }
    final TextView tv = mView.getMessageView();
    final Drawable[] compoundDrawables = tv.getCompoundDrawables();
    tv.setCompoundDrawables(leftSide ? drawable : compoundDrawables[0],
        compoundDrawables[1],
        leftSide ? compoundDrawables[2] : drawable,
        compoundDrawables[3]
    );
    return this;
  }

  private Drawable fitDrawable(Drawable drawable, int sizePx) {
    if (drawable.getIntrinsicWidth() != sizePx || drawable.getIntrinsicHeight() != sizePx) {
      if (drawable instanceof BitmapDrawable) {
        drawable = new BitmapDrawable(mContext.getResources(),
            Bitmap.createScaledBitmap(getBitmap(drawable), sizePx, sizePx, true)
        );
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

  private static Bitmap getBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    } else if (drawable instanceof VectorDrawable) {
      return getBitmap((VectorDrawable) drawable);
    } else {
      throw new IllegalArgumentException("unsupported drawable type");
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
    Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
        vectorDrawable.getIntrinsicHeight(),
        Bitmap.Config.ARGB_8888
    );
    Canvas canvas = new Canvas(bitmap);
    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    vectorDrawable.draw(canvas);
    return bitmap;
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
   * Sets text color.
   *
   * @param color The text color. Default is white.
   * @return The object used for Constructor Chaining
   */
  @NonNull
  public Candybar setTextColor(int color) {
    getTextView().setTextColor(color);
    return this;
  }

  /*
  Callback functionality. Use `candybar.setCallback(new Callback())` to set an event callback.
   */

  /**
   * Gets text view.
   *
   * @return the text view
   */
  @NonNull
  public TextView getTextView() {
    return (TextView) mView.findViewById(R.id.candybar_text);
  }

  /**
   * Sets text size.
   *
   * @param size Set the text size to the given value, interpreted as "scaled pixel" units. This
   *     size is adjusted based on the current density and user font size preference.
   * @return The object used for Constructor Chaining
   */
  @NonNull
  public Candybar setTextSize(float size) {
    getTextView().setTextSize(size);
    return this;
  }

  /**
   * Sets background color.
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
   * Sets alpha.
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
   * Sets interpolator.
   *
   * @param interpolator The rate of change used for animating the view in and out of the screen
   * @return The object used for Constructor Chaining
   */
  @NonNull
  public Candybar setInterpolator(Interpolator interpolator) {
    mInterpolator = interpolator;
    return this;
  }

  /**
   * Sets height.
   *
   * @param pixels the pixels
   * @return the height
   */
  @NonNull
  public Candybar setHeight(int pixels) {
    getTextView().setHeight(pixels);
    return this;
  }

  /**
   * Sets dismiss on tap.
   *
   * @param dismissOnTap the dismiss on tap
   * @return the dismiss on tap
   */
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

  /**
   * Gets view.
   *
   * @return the view
   */
  @NonNull
  public View getView() {
    return mView;
  }

  /**
   * Sets callback.
   *
   * @param callback the callback
   * @return the callback
   */
  @NonNull
  public Candybar setCallback(Callback callback) {
    mCallback = callback;
    return this;
  }

  /**
   * Sets animation duration.
   *
   * @param mAnimationDuration The time taken to animate the Candybar background view onto the
   *     screen.
   * @return The object used for Constructor Chaining
   */
  public Candybar setmAnimationDuration(int mAnimationDuration) {
    this.mAnimationDuration = mAnimationDuration;
    return this;
  }

  /**
   * Sets animation fade duration.
   *
   * @param mAnimationFadeDuration The time to animate the Candybar content (icon and text) onto
   *     the view.
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
          S_HANDLER.post(new Runnable() {
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

  /**
   * Is shown boolean.
   *
   * @return the boolean
   */
  public boolean isShown() {
    return CandybarManager.getInstance().isCurrentOrNext(mManagerCallback);
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
                mAnimationFadeDuration
            );
          }

          @Override
          public void onAnimationEnd(View view) {
            if (mCallback != null) {
              mCallback.onShown(Candybar.this);
            }
            CandybarManager.getInstance().onShown(mManagerCallback);
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

  /**
   * The interface Direction.
   */
  @IntDef({DIRECTION_TOP, DIRECTION_BOTTOM})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Direction {}

  /**
   * The type Callback.
   */
  public abstract static class Callback {
    /**
     * The constant DISMISS_EVENT_TAP.
     */
    public static final int DISMISS_EVENT_TAP = 1;
    /**
     * The constant DISMISS_EVENT_TIMEOUT.
     */
    public static final int DISMISS_EVENT_TIMEOUT = 2;
    /**
     * The constant DISMISS_EVENT_MANUAL.
     */
    public static final int DISMISS_EVENT_MANUAL = 3;
    /**
     * The constant DISMISS_EVENT_CONSECUTIVE.
     */
    public static final int DISMISS_EVENT_CONSECUTIVE = 4;

    /**
     * On shown.
     *
     * @param candybar the candybar
     */
    public void onShown(Candybar candybar) {
    }

    /**
     * On dismissed.
     *
     * @param candybar the candybar
     * @param event the event
     */
    public void onDismissed(Candybar candybar, @DismissEvent int event) {
    }

    /**
     * The interface Dismiss event.
     */
    @IntDef({DISMISS_EVENT_TAP, DISMISS_EVENT_TIMEOUT, DISMISS_EVENT_MANUAL,
        DISMISS_EVENT_CONSECUTIVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DismissEvent {}
  }
}
