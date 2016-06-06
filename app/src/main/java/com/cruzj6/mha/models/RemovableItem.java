package com.cruzj6.mha.models;

import android.content.Context;

/**
 * Created by Joey on 6/3/16.
 */
public abstract class RemovableItem {

    protected boolean shouldRemove = false;

    public final boolean getShouldRemove()
    {
        return shouldRemove;
    }
    public final void setShouldRemove(boolean rem)
    {
        shouldRemove = rem;
    }

    public abstract void removeFromDatabase(Context context);

}
