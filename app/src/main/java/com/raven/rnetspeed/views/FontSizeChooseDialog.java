package com.raven.rnetspeed.views;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;


/**
 * 字体大小弹窗
 */

public class FontSizeChooseDialog extends DialogPreference {
    public FontSizeChooseDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontSizeChooseDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FontSizeChooseDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FontSizeChooseDialog(Context context) {
        super(context);
    }
}
