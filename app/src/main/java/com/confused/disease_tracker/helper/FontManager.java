package com.confused.disease_tracker.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class FontManager extends androidx.appcompat.widget.AppCompatTextView {
    public static final String ROOT = "fonts/",
            FONTAWESOME = ROOT + "fa-solid-900.ttf";

    public FontManager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FontManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FontManager(Context context) {
        super(context);
        init();
    }

    private void init() {

        //Font name should not contain "/".
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                FONTAWESOME);
        setTypeface(tf);
    }

}
