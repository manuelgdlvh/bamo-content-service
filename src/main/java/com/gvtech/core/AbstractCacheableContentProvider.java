package com.gvtech.core;

import com.gvtech.core.expiration.ContentExpiration;
import com.gvtech.core.remove.ContentRemove;
import com.gvtech.core.status.ContentStatusHandler;
import com.gvtech.core.update.ContentUpdate;
import com.gvtech.monitoring.MetricService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import lombok.Getter;

public abstract class AbstractCacheableContentProvider<T> implements CacheableContentProvider<T> {
    @Getter
    private final Long expirationInMillis;
    private final ContentStorage<T> contentStorage = new ContentStorage<>();
    @Inject
    ContentHandler contentHandler;
    @Inject
    ContentStatusHandler contentStatusHandler;
    @Inject
    MetricService metricService;

    public AbstractCacheableContentProvider(final Long expirationInMillis) {
        this.expirationInMillis = expirationInMillis;
    }


    @Override
    public T get(final ContentId contentId) {

        try {
            this.contentStatusHandler.awaitReadable(contentId, this.type());

            final T content = this.contentStorage.get(contentId);
            if (content != null) {
                return content;
            }

        } finally {
            this.contentStatusHandler.notifyEndRead(contentId, this.type());
        }

        try {
            this.contentStatusHandler.awaitWritable(contentId, this.type());

            final T content = this.contentStorage.get(contentId);
            if (content != null) {
                return content;
            }

            final ContentBuild<T> contentBuilt = this.safeBuild(contentId);
            if (contentBuilt == null || contentBuilt.getContent() == null) {
                return null;
            }

            this.contentStorage.add(contentId, contentBuilt.getContent());
            this.contentHandler.enqueueExpiration(new ContentExpiration(contentId, this.type(), expirationInMillis));

            final ContentDependant contentDependant = new ContentDependant(contentId, this.type());
            for (ContentDependency contentDependency : contentBuilt.getDependencies()) {
                this.contentHandler.addDependency(contentDependency, contentDependant);
            }
            return contentBuilt.getContent();

        } finally {
            this.contentStatusHandler.notifyEndWrite(contentId, this.type());
        }


    }

    public void remove(final ContentRemove contentRemove) {
        try {
            this.contentStatusHandler.awaitReadable(contentRemove.getId(), contentRemove.getType());
            if (!contentStorage.contains(contentRemove.getId())) {
                return;
            }

        } finally {
            this.contentStatusHandler.notifyEndRead(contentRemove.getId(), contentRemove.getType());
        }

        try {
            this.contentStatusHandler.awaitWritable(contentRemove.getId(), contentRemove.getType());

            if (!contentStorage.contains(contentRemove.getId())) {
                return;
            }

            this.contentStorage.remove(contentRemove.getId());
            this.contentHandler.removeExpiration(new ContentExpiration(contentRemove.getId(), contentRemove.getType(), -1L));
            for (ContentDependant dependant : this.contentHandler.getDependants(contentRemove.getId(), this.type())) {
                this.contentHandler.enqueueRemove(new ContentRemove(dependant.getId(), dependant.getType()));
            }
            this.contentHandler.removeAllDependencies(contentRemove.getId(), contentRemove.getType());

        } finally {
            this.contentStatusHandler.notifyEndWrite(contentRemove.getId(), contentRemove.getType());
        }

    }

    public void update(final ContentUpdate contentUpdate) {


        try {
            this.contentStatusHandler.awaitReadable(contentUpdate.getId(), contentUpdate.getType());
            final T oldContent = this.contentStorage.get(contentUpdate.getId());
            if (oldContent == null) {
                for (ContentDependant dependant : this.contentHandler.getDependants(contentUpdate.getId(), this.type())) {
                    this.contentHandler.enqueueUpdate(new ContentUpdate(dependant.getId(), dependant.getType()));
                }
                return;
            }
        } finally {
            this.contentStatusHandler.notifyEndRead(contentUpdate.getId(), contentUpdate.getType());
        }

        try {
            this.contentStatusHandler.awaitUpdatable(contentUpdate.getId(), contentUpdate.getType());

            final T oldContent = this.contentStorage.get(contentUpdate.getId());
            if (oldContent == null) {
                for (ContentDependant dependant : this.contentHandler.getDependants(contentUpdate.getId(), this.type())) {
                    this.contentHandler.enqueueUpdate(new ContentUpdate(dependant.getId(), dependant.getType()));
                }
                return;
            }

            final ContentBuild<T> refreshedContent = this.safeBuild(contentUpdate.getId());
            if (refreshedContent == null || refreshedContent.getContent() == null) {
                this.contentHandler.enqueueRemove(new ContentRemove(contentUpdate.getId(), contentUpdate.getType()));
                return;
            }

            this.contentStorage.add(contentUpdate.getId(), refreshedContent.getContent());
            this.contentHandler.enqueueExpirationIfAbsent(new ContentExpiration(contentUpdate.getId(), contentUpdate.getType(), expirationInMillis));

            if (this.equals(oldContent, refreshedContent.getContent())) {
                return;
            }

            this.contentHandler.refreshDependencies(contentUpdate.getId(), contentUpdate.getType(), refreshedContent.getDependencies());
            for (ContentDependant dependant : this.contentHandler.getDependants(contentUpdate.getId(), this.type())) {
                this.contentHandler.enqueueUpdate(new ContentUpdate(dependant.getId(), dependant.getType()));
            }

        } finally {
            this.contentStatusHandler.notifyEndUpdate(contentUpdate.getId(), contentUpdate.getType());
        }


    }


    public void expire(final ContentExpiration contentExpiration) {

        try {
            this.contentStatusHandler.awaitReadable(contentExpiration.getId(), contentExpiration.getType());

            if (!this.contentHandler.hasDependants(contentExpiration.getId(), this.type())) {
                this.contentHandler.enqueueRemove(new ContentRemove(contentExpiration.getId(), this.type()));
                return;
            }

            this.contentHandler.enqueueUpdate(new ContentUpdate(contentExpiration.getId(), this.type()));

        } finally {
            this.contentStatusHandler.notifyEndRead(contentExpiration.getId(), contentExpiration.getType());
        }


    }


    public int getDependantsSize() {
        return this.contentHandler.getDependantsSize(this.type());
    }

    public int getDependenciesSize() {
        return this.contentHandler.getDependenciesSize(this.type());
    }

    public int getContentSize() {
        return this.contentStorage.size();
    }

    public ContentBuild<T> safeBuild(final ContentId contentId) {

        try {
            return build(contentId);
        } catch (Exception exception) {
            Log.error(String.format("error building %s - %s content caused by: %s", type(), contentId.getId(), exception.getMessage()));
        }

        return null;
    }


    public abstract boolean equals(final T o1, final T o2);

    public abstract ContentBuild<T> build(final ContentId contentId);

}
