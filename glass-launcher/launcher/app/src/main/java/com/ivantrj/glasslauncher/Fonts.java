package com.ivantrj.glasslauncher;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;

/** Outfit (SIL OFL 1.1) is a variable font: one file, any weight from 100 to 900. */
final class Fonts {
    private static final SparseArray<Typeface> CACHE = new SparseArray<>();

    static Typeface outfit(Context context, int weight) {
        Typeface t = CACHE.get(weight);
        if (t == null) {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                t = new Typeface.Builder(context.getAssets(), "fonts/Outfit.ttf")
                        .setFontVariationSettings("'wght' " + weight)
                        .build();
            } else {
                // Fire OS 6 (API 25) can't set variable-font axes: use the file's default
                // instance, with fake bold for heavy weights.
                Typeface base = Typeface.createFromAsset(context.getAssets(), "fonts/Outfit.ttf");
                t = Typeface.create(base, weight >= 600 ? Typeface.BOLD : Typeface.NORMAL);
            }
            CACHE.put(weight, t);
        }
        return t;
    }

    private Fonts() {}
}
