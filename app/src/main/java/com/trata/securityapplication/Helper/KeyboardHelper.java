package com.trata.securityapplication.Helper;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardHelper {

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        if (activity != null && view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

}
