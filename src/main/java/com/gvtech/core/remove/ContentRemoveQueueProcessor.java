package com.gvtech.core.remove;

import com.gvtech.core.ContentType;
import com.gvtech.service.ContentService;
import com.gvtech.support.AbstractQueueProcessor;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;


@ApplicationScoped
@Startup
public class ContentRemoveQueueProcessor extends AbstractQueueProcessor<ContentRemove> {

    private static final int MAX_COUNT = 100;
    private static final int POLL_TIMEOUT = 25;

    @Inject
    ContentService contentService;

    public ContentRemoveQueueProcessor() {
        super(MAX_COUNT, POLL_TIMEOUT);
    }

    @Override
    protected void process(List<ContentRemove> items) {

        final Map<ContentType, Set<ContentRemove>> updates = new HashMap<>();
        for (ContentRemove item : items) {
            updates.putIfAbsent(item.getType(), new HashSet<>());
            updates.get(item.getType()).add(item);
        }

        for (var entry : updates.entrySet()) {
            execute(() -> contentService.onRemove(entry.getKey(), entry.getValue()));
        }
    }
}
