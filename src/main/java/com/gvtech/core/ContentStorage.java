package com.gvtech.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContentStorage<T> {

    private final Map<ContentId, T> contentStorage = new ConcurrentHashMap<>();


    protected T get(final ContentId contentId) {
        return this.contentStorage.get(contentId);
    }

    protected boolean contains(final ContentId contentId) {
        return this.contentStorage.containsKey(contentId);
    }

    protected void add(final ContentId contentId, final T content) {
        this.contentStorage.put(contentId, content);
    }

    protected void remove(final ContentId contentId) {
        this.contentStorage.remove(contentId);
    }

    protected int size() {
        return this.contentStorage.size();
    }
}
