package com.gvtech.core.expiration;

import com.gvtech.core.ContentType;
import com.gvtech.service.ContentService;
import com.gvtech.support.AbstractUpdatableQueueProcessor;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;


@ApplicationScoped
@Startup
public class ContentExpirationProcessor extends AbstractUpdatableQueueProcessor<ContentExpiration> {

    private static final int MAX_COUNT = 100;
    private static final int POLL_TIMEOUT = 25;

    @Inject
    ContentService contentService;

    public ContentExpirationProcessor() {
        super(MAX_COUNT, POLL_TIMEOUT);
    }

    @Override
    protected void process(List<ContentExpiration> items) {

        final Map<ContentType, Set<ContentExpiration>> expirations = new HashMap<>();
        for (ContentExpiration item : items) {
            expirations.putIfAbsent(item.getType(), new HashSet<>());
            expirations.get(item.getType()).add(item);
        }

        for (var entry : expirations.entrySet()) {
            execute(() -> contentService.onExpiration(entry.getKey(), entry.getValue()));
        }
    }
}
