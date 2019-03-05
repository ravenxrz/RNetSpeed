package com.raven.rnetspeed.views;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.raven.rnetspeed.R;

/**
 * 自定义字体颜色选择弹窗
 */
public class FontColorChooseDialog extends DialogPreference {
    private static final String TAG  = FontSizeChooseDialog.class.getSimpleName();
    private static final int POSITIVBUTTON = -1;
    private static final int NEGATIVEBUTTON = -2;

    private ColorChooseView colorChooseView;


    public FontColorChooseDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontColorChooseDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FontColorChooseDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FontColorChooseDialog(Context context) {
        super(context);
    }


    @Override
    protected View onCreateDialogView() {
        return super.onCreateDialogView();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        colorChooseView = view.findViewById(R.id.color_choose_view);
        /* 获取已经持久化的color */
        String color = getPersistedString("");
        colorChooseView.initSelect(color);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if(POSITIVBUTTON == which){
            String color = colorChooseView.getColor();
            persistString(color); /* 准备存入数据到sp中 */
            notifyChanged();
            callChangeListener(color);
        }else if(NEGATIVEBUTTON == which){
            dialog.dismiss();
        }
    }


}
