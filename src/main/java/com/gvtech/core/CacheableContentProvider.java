package com.gvtech.core;


import com.gvtech.core.expiration.ContentExpiration;
import com.gvtech.core.remove.ContentRemove;
import com.gvtech.core.update.ContentUpdate;

public interface CacheableContentProvider<T> extends ContentProvider<T> {
    ContentBuild<T> build(final ContentId contentId);

    void expire(final ContentExpiration contentExpiration);

    void remove(final ContentRemove contentRemoves);

    void update(final ContentUpdate contentUpdate);

}
