package com.example.chess.util;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Android implementation of IResourceProvider.
 */
public class AndroidResourceProvider implements IResourceProvider {
    private final Context context;

    public AndroidResourceProvider(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String getString(int resId) {
        return context.getString(resId);
    }

    @Override
    public String getString(int resId, Object... formatArgs) {
        return context.getString(resId, formatArgs);
    }
}
