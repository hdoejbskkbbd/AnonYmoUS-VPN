package com.anonymous.tunnel.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class AnonymousTextView extends AppCompatTextView {

    private static final int[] COLORS = {
        Color.parseColor("#FF6B35"),  // A - Orange
        Color.parseColor("#4CAF50"),  // n - Green
        Color.parseColor("#2196F3"),  // o - Blue
        Color.parseColor("#4CAF50"),  // n - Green
        Color.parseColor("#F44336"),  // Y - Red
        Color.parseColor("#4CAF50"),  // m - Green
        Color.parseColor("#2196F3"),  // o - Blue
        Color.parseColor("#00BCD4"),  // u - Cyan
        Color.parseColor("#00BCD4"),  // S - Cyan
    };

    public AnonymousTextView(Context context) {
        super(context);
    }

    public AnonymousTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnonymousTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAnonymousText(String text) {
        if (text == null || text.isEmpty()) {
            setText("");
            return;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);
            int colorIndex = i % COLORS.length;

            int start = builder.length();
            builder.append(charStr);
            int end = builder.length();

            builder.setSpan(
                new ForegroundColorSpan(COLORS[colorIndex]),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        setText(builder);
    }
}
