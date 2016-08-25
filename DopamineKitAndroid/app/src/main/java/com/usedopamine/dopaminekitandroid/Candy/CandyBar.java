package com.usedopamine.dopaminekitandroid.Candy;

/**
 * Created by cuddergambino on 6/1/16.
 */

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.usedopamine.dopaminekitandroid.R;

public final class CandyBar {
    /**
     * Represents a set of Icons to be used in a {@link CandyBar}.
     * <p>
     * <code>
     *  CERTIFICATE CROWN CROWN2 MEDALSTAR RIBBONSTAR STARS STOPWATCH THUMBSUP TROPHYHAND TROPHYSTAR WREATHSTAR
     * </code>
     */
    public enum Candy{
        CERTIFICATE(R.drawable.certificate),
        CROWN(R.drawable.crown),
        CROWN2(R.drawable.crown2),
        MEDALSTAR(R.drawable.medalstar),
        RIBBONSTAR(R.drawable.ribbonstar),
        STARS(R.drawable.stars),
        STOPWATCH(R.drawable.stopwatchone),
        THUMBSUP(R.drawable.thumbsup),
        TROPHYHAND(R.drawable.trophyhand),
        TROPHYSTAR(R.drawable.trophystar),
        WREATHSTAR(R.drawable.wreathstar)
        ;

        private final int res;
        private Candy(int id){
            res = id;
        }
    }

    public static final int LENGTH_SHORT = Snackbar.LENGTH_SHORT;
    public static final int LENGTH_LONG = Snackbar.LENGTH_LONG;

    private Snackbar snackbar;

    /**
     * Make a CandyBar to display a {@link Candy} icon and message text
     *
     * CandyBar will try and find a parent view to hold CandyBar's view from the value given
     * to {@code view}. CandyBar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     *
     * Having a {@link CoordinatorLayout} in your view hierarchy allows CandyBar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param candy    The {@link Candy} icon to show.
     * @param title    The title to show.  Will be formatted to show larger than the subtitle.
     * @param subtitle The subtitle to show.
     * @param backgroundColor The background color for the CandyBar
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    public CandyBar(View view, Candy candy, String title, String subtitle, int backgroundColor, int duration){
        SpannableString formattedText = formatText(title, subtitle);
//        formattedText.setSpan(new RelativeSizeSpan(1.25f), 0, title.length(), 0);

        snackbar = CandyBar.makeSnackbarWithIcon(view, candy, formattedText, backgroundColor, duration);
    }


    /**
     * Make a CandyBar to display a message
     *
     * CandyBar will try and find a parent view to hold CandyBar's view from the value given
     * to {@code view}. CandyBar will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     *
     * Having a {@link CoordinatorLayout} in your view hierarchy allows CandyBar to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param candy    The {@link Candy} icon to show.
     * @param text     The text to show.
     * @param backgroundColor The background color for the CandyBar
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    public CandyBar(View view, Candy candy, CharSequence text, int backgroundColor, int duration){
        snackbar = CandyBar.makeSnackbarWithIcon(view, candy, text, backgroundColor, duration);
    }


    /**
     * Present the CandyBar
     */
    public void show(){
        snackbar.show();
    }


    // set Title and Subtitle relative font sizes
    private SpannableString formatText(String title, String subtitle){
        SpannableString spannableString = new SpannableString(title +"\n"+ subtitle);
        spannableString.setSpan(new RelativeSizeSpan(1.25f), 0, title.length(), 0);

        return spannableString;
    }


    private static Snackbar makeSnackbarWithIcon(View view, Candy candy, CharSequence text, int backgroundColor,  int duration){
        Snackbar snackbar = Snackbar.make(view, text, duration);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView)snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER);

        // Add icon
        textView.setCompoundDrawablesWithIntrinsicBounds(candy.res, 0, 0, 0);
        // removed dynamic dimension in favor of hard coded value to avoid need for Context
//        textView.setCompoundDrawablePadding(context.getResources().getDimensionPixelOffset(android.support.design.R.dimen.design_snackbar_padding_horizontal));
        textView.setCompoundDrawablePadding(10);

        // Set background color
        snackbarView.setBackgroundColor(backgroundColor);
        snackbarView.setAlpha(0.85f);

        return snackbar;
    }

}
