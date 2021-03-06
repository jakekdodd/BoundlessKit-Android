package ai.boundless.reward.candybar;

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Manages {@link Candybar}s.
 */
class CandybarManager {

  private static final int MSG_TIMEOUT = 0;
  private static final int SHORT_DURATION_MS = 1500;
  private static final int LONG_DURATION_MS = 2750;
  private static CandybarManager sCandybarManager;
  private final Object mLock;
  private final Handler mHandler;
  private CandybarRecord mCurrentCandybar;
  private CandybarRecord mNextCandybar;

  private CandybarManager() {
    mLock = new Object();
    mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
      @Override
      public boolean handleMessage(Message message) {
        switch (message.what) {
          case MSG_TIMEOUT:
            handleTimeout((CandybarRecord) message.obj);
            return true;
          default:
        }
        return false;
      }
    });
  }

  private void handleTimeout(CandybarRecord record) {
    synchronized (mLock) {
      if (mCurrentCandybar == record || mNextCandybar == record) {
        cancelCandybarLocked(record, Candybar.Callback.DISMISS_EVENT_TIMEOUT);
      }
    }
  }

  private boolean cancelCandybarLocked(CandybarRecord record, int event) {
    final Callback callback = record.callback.get();
    if (callback != null) {
      callback.dismiss(event);
      return true;
    }
    return false;
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  static CandybarManager getInstance() {
    if (sCandybarManager == null) {
      sCandybarManager = new CandybarManager();
    }
    return sCandybarManager;
  }

  /**
   * Show.
   *
   * @param duration the duration
   * @param callback the callback
   */
  public void show(int duration, Callback callback) {
    synchronized (mLock) {
      if (isCurrentCandybar(callback)) {
        // Means that the callback is already in the queue. We'll just update the duration
        mCurrentCandybar.duration = duration;
        // If this is the Candybar currently being shown, call re-schedule it's
        // timeout
        mHandler.removeCallbacksAndMessages(mCurrentCandybar);
        scheduleTimeoutLocked(mCurrentCandybar);
        return;
      } else if (isNextCandybar(callback)) {
        // We'll just update the duration
        mNextCandybar.duration = duration;
      } else {
        // Else, we need to create a new record and queue it
        mNextCandybar = new CandybarRecord(duration, callback);
      }

      if (mCurrentCandybar != null && cancelCandybarLocked(mCurrentCandybar,
          Candybar.Callback.DISMISS_EVENT_CONSECUTIVE
      )) {
        // If we currently have a Candybar, try and cancel it and wait in line
        return;
      } else {
        // Clear out the current candybar
        mCurrentCandybar = null;
        // Otherwise, just show it now
        showNextCandybarLocked();
      }
    }
  }

  private boolean isCurrentCandybar(Callback callback) {
    return mCurrentCandybar != null && mCurrentCandybar.isCandybar(callback);
  }

  private void scheduleTimeoutLocked(CandybarRecord r) {
    if (r.duration == -1) {
      // If we're set to indefinite, we don't want to set a timeout
      return;
    }

    int durationMs = r.duration;
    mHandler.removeCallbacksAndMessages(r);
    mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r), durationMs);
  }

  private boolean isNextCandybar(Callback callback) {
    return mNextCandybar != null && mNextCandybar.isCandybar(callback);
  }

  private void showNextCandybarLocked() {
    if (mNextCandybar != null) {
      mCurrentCandybar = mNextCandybar;
      mNextCandybar = null;

      final Callback callback = mCurrentCandybar.callback.get();
      if (callback != null) {
        callback.show();
      } else {
        // The callback doesn't exist any more, clear out the Candybar
        mCurrentCandybar = null;
      }
    }
  }

  /**
   * Dismiss.
   *
   * @param callback the callback
   * @param event the event
   */
  public void dismiss(Callback callback, int event) {
    synchronized (mLock) {
      if (isCurrentCandybar(callback)) {
        cancelCandybarLocked(mCurrentCandybar, event);
      } else if (isNextCandybar(callback)) {
        cancelCandybarLocked(mNextCandybar, event);
      }
    }
  }

  /**
   * Should be called when a Candybar is no longer displayed. This is after any exit.
   * animation has finished.
   *
   * @param callback the callback
   */
  public void onDismissed(Callback callback) {
    synchronized (mLock) {
      if (isCurrentCandybar(callback)) {
        // If the callback is from a Candybar currently show, remove it and show a new one
        mCurrentCandybar = null;
        if (mNextCandybar != null) {
          showNextCandybarLocked();
        }
      }
    }
  }

  /**
   * Should be called when a Candybar is being shown. This is after any entrance animation has.
   * finished.
   *
   * @param callback the callback
   */
  public void onShown(Callback callback) {
    synchronized (mLock) {
      if (isCurrentCandybar(callback)) {
        scheduleTimeoutLocked(mCurrentCandybar);
      }
    }
  }

  /**
   * Cancel timeout.
   *
   * @param callback the callback
   */
  public void cancelTimeout(Callback callback) {
    synchronized (mLock) {
      if (isCurrentCandybar(callback)) {
        mHandler.removeCallbacksAndMessages(mCurrentCandybar);
      }
    }
  }

  /**
   * Restore timeout.
   *
   * @param callback the callback
   */
  public void restoreTimeout(Callback callback) {
    synchronized (mLock) {
      if (isCurrentCandybar(callback)) {
        scheduleTimeoutLocked(mCurrentCandybar);
      }
    }
  }

  /**
   * Is current boolean.
   *
   * @param callback the callback
   * @return the boolean
   */
  public boolean isCurrent(Callback callback) {
    synchronized (mLock) {
      return isCurrentCandybar(callback);
    }
  }

  /**
   * Is current or next boolean.
   *
   * @param callback the callback
   * @return the boolean
   */
  public boolean isCurrentOrNext(Callback callback) {
    synchronized (mLock) {
      return isCurrentCandybar(callback) || isNextCandybar(callback);
    }
  }

  /**
   * The interface Callback.
   */
  interface Callback {
    /**
     * Show.
     */
    void show();

    /**
     * Dismiss.
     *
     * @param event the event
     */
    void dismiss(int event);
  }

  private static class CandybarRecord {
    private final WeakReference<Callback> callback;
    private int duration;

    /**
     * Instantiates a new Candybar record.
     *
     * @param duration the duration
     * @param callback the callback
     */
    CandybarRecord(int duration, Callback callback) {
      this.callback = new WeakReference<>(callback);
      this.duration = duration;
    }

    /**
     * Is candybar boolean.
     *
     * @param callback the callback
     * @return the boolean
     */
    boolean isCandybar(Callback callback) {
      return callback != null && this.callback.get() == callback;
    }
  }

}
