package com.alecananian.earthviewlivewallpaper.preferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MultiLinePreference extends Preference {

    public MultiLinePreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public MultiLinePreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public MultiLinePreference(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView titleView = (TextView)view.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setSingleLine(false);
        }

        TextView summaryView = (TextView)view.findViewById(android.R.id.summary);
        if (summaryView != null) {
            summaryView.setSingleLine(false);
        }
    }

}