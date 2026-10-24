package com.d1vivek.wallet.presentation.common;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class SystemBarInsets {

    private SystemBarInsets() {
    }

    public static void applyAsPadding(View root) {
        int baseLeft = root.getPaddingLeft();
        int baseTop = root.getPaddingTop();
        int baseRight = root.getPaddingRight();
        int baseBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            view.setPadding(
                    baseLeft + insets.left,
                    baseTop + insets.top,
                    baseRight + insets.right,
                    baseBottom + insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
