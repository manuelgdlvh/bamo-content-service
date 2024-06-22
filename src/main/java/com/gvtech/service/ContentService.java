package com.gvtech.service;


import com.gvtech.core.*;
import com.gvtech.core.expiration.ContentExpiration;
import com.gvtech.core.remove.ContentRemove;
import com.gvtech.core.update.ContentUpdate;
import com.gvtech.support.AsyncExecutorAware;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@Startup
public class ContentService extends AsyncExecutorAware {

    final Map<ContentType, ContentProvider<?>> contentProviders;
    @Inject
    ContentHandler contentHandler;

    public ContentService(Instance<ContentProvider<?>> contentProviders) {

        final Map<ContentType, ContentProvider<?>> contentProviderMap = new HashMap<>();
        for (ContentProvider<?> contentProvider : contentProviders) {
            contentProviderMap.put(contentProvider.type(), contentProvider);
        }
        this.contentProviders = Collections.unmodifiableMap(contentProviderMap);
    }


    public Object get(final ContentType type, final ContentId id) {
        final ContentProvider<?> contentProvider = this.contentProviders.get(type);
        if (contentProvider == null) {
            return null;
        }

        return contentProvider.get(id);
    }


    public void onUpdate(final ContentType contentType, final Set<ContentUpdate> updates) {
        final ContentProvider<?> contentProvider = this.contentProviders.get(contentType);
        if (contentProvider instanceof CacheableContentProvider<?> cacheableContentProvider) {
            updates.forEach(cacheableContentProvider::update);
        }
    }

    public void onRemove(final ContentType contentType, final Set<ContentRemove> removes) {
        final ContentProvider<?> contentProvider = this.contentProviders.get(contentType);
        if (contentProvider instanceof CacheableContentProvider<?> cacheableContentProvider) {
            removes.forEach(cacheableContentProvider::remove);
        }
    }

    public void onExpiration(final ContentType contentType, final Set<ContentExpiration> expirations) {
        final ContentProvider<?> contentProvider = this.contentProviders.get(contentType);
        if (contentProvider instanceof CacheableContentProvider<?> cacheableContentProvider) {
            expirations.forEach(cacheableContentProvider::expire);
        }
    }

}
