package boundless.boundlesskitexample.Candy;

import boundless.boundlesskitexample.R;
import boundless.kit.rewards.animation.attention.CandyBar;

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