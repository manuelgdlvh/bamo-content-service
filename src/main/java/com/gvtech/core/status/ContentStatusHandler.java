package com.gvtech.core.status;


import com.gvtech.core.CacheableContentProvider;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.monitoring.MetricService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ApplicationScoped
@Startup
public class ContentStatusHandler {

    private final Map<ContentType, Map<ContentId, ContentStatus>> contentStatusMap;
    private final MetricService metricService;


    public ContentStatusHandler(final Instance<CacheableContentProvider<?>> providers, final MetricService metricsService) {
        this.metricService = metricsService;
        final Map<ContentType, Map<ContentId, ContentStatus>> status = new HashMap<>();

        for (CacheableContentProvider<?> cacheableContentProvider : providers) {
            status.put(cacheableContentProvider.type(), new ConcurrentHashMap<>());
        }

        this.contentStatusMap = Collections.unmodifiableMap(status);
    }


    private ContentStatus getAndPutIfAbsent(final ContentId contentId, final ContentType contentType) {
        ContentStatus currentStatus = this.contentStatusMap.get(contentType).putIfAbsent(contentId, new ContentStatus(String.format("%s/%s", contentType.getType(), contentId.getId())));
        if (currentStatus == null) {
            Log.info(String.format("created status lock for %s - %s content", contentType.getType(), contentId.getId()));
            currentStatus = this.contentStatusMap.get(contentType).get(contentId);
        } else {
            Log.info(String.format(" status lock for %s - %s content not created, an valid status lock found", contentType.getType(), contentId.getId()));
        }

        return currentStatus;
    }

    public void awaitReadable(final ContentId contentId, final ContentType contentType) {
        ContentStatus contentStatus = this.getAndPutIfAbsent(contentId, contentType);
        int numRetries = 0;
        while (!contentStatus.tryChangeStatus(Status.READING)) {
            Log.warn(String.format("retrying to acquire read status lock for %s - %s, %s total retries", contentId.getId(), contentType.getType(), ++numRetries));
            contentStatus = this.getAndPutIfAbsent(contentId, contentType);
            metricService.incrementLockStatusRetry();
        }
    }

    public void awaitUpdatable(final ContentId contentId, final ContentType contentType) {
        ContentStatus contentStatus = this.getAndPutIfAbsent(contentId, contentType);
        int numRetries = 0;
        while (!contentStatus.tryChangeStatus(Status.UPDATING)) {
            Log.warn(String.format("retrying to acquire update status lock for %s - %s, %s total retries", contentId.getId(), contentType.getType(), ++numRetries));
            contentStatus = this.getAndPutIfAbsent(contentId, contentType);
            metricService.incrementLockStatusRetry();
        }
    }


    public void awaitWritable(final ContentId contentId, final ContentType contentType) {
        ContentStatus contentStatus = this.getAndPutIfAbsent(contentId, contentType);
        int numRetries = 0;
        while (!contentStatus.tryChangeStatus(Status.WRITING)) {
            Log.warn(String.format("retrying to acquire write status lock for %s - %s,  %s total retries", contentId.getId(), contentType.getType(), ++numRetries));
            contentStatus = this.getAndPutIfAbsent(contentId, contentType);
            metricService.incrementLockStatusRetry();
        }
    }

    public void notifyEndRead(final ContentId contentId, final ContentType contentType) {
        final ContentStatus contentStatus = this.getAndPutIfAbsent(contentId, contentType);
        contentStatus.endRead();
        Log.info(String.format("releasing read status lock for %s - %s", contentId.getId(), contentType.getType()));
        this.metricService.incrementLockStatusRead();
    }

    public void notifyEndUpdate(final ContentId contentId, final ContentType contentType) {
        final ContentStatus contentStatus = this.getAndPutIfAbsent(contentId, contentType);
        contentStatus.endUpdate();
        Log.info(String.format("releasing update status lock for %s - %s", contentId.getId(), contentType.getType()));
        this.metricService.incrementLockStatusUpdate();
    }

    public void notifyEndWrite(final ContentId contentId, final ContentType contentType) {
        final ContentStatus contentStatus = this.getAndPutIfAbsent(contentId, contentType);
        contentStatus.endWrite();
        Log.info(String.format("releasing write status lock for %s - %s", contentId.getId(), contentType.getType()));
        this.metricService.incrementLockStatusWrite();
    }

    @Scheduled(every = "300s")
    public void lockCleaner() {
        for (Map.Entry<ContentType, Map<ContentId, ContentStatus>> entry : this.contentStatusMap.entrySet()) {
            final ContentType contentType = entry.getKey();
            for (Map.Entry<ContentId, ContentStatus> status : entry.getValue().entrySet()) {
                if (!status.getValue().tryInvalidate()) {
                    continue;
                }

                this.contentStatusMap.get(contentType).remove(status.getKey());
                this.metricService.incrementLockStatusRemove();
                Log.info(String.format("invalidated status lock for %s - %s content", contentType.getType(), status.getKey().getId()));
            }
        }
    }

}
