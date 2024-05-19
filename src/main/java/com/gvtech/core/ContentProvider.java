package com.gvtech.core;


public interface ContentProvider<T> {
    T get(final ContentId contentId);
    ContentType type();
}
