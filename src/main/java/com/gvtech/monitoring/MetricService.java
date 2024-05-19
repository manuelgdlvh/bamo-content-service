package com.gvtech.monitoring;


import com.gvtech.core.AbstractCacheableContentProvider;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;


@ApplicationScoped
@Startup
public class MetricService {

    private final Counter totalDatabaseUpdate;
    private final Counter totalStatusLockRead;
    private final Counter totalStatusLockWrite;
    private final Counter totalStatusLockUpdate;
    private final Counter totalStatusLockRetry;
    private final Counter totalStatusLockRemove;

    public MetricService(final MeterRegistry meterRegistry, final Instance<AbstractCacheableContentProvider<?>> cacheableProviders) {
        this.totalDatabaseUpdate = Counter.builder("content.service.database.update").register(meterRegistry);
        this.totalStatusLockRead = Counter.builder("content.service.status.lock.operation").tag("operation", "read").register(meterRegistry);
        this.totalStatusLockWrite = Counter.builder("content.service.status.lock.operation").tag("operation", "write").register(meterRegistry);
        this.totalStatusLockUpdate = Counter.builder("content.service.status.lock.operation").tag("operation", "update").register(meterRegistry);
        this.totalStatusLockRetry = Counter.builder("content.service.status.lock.operation").tag("operation", "retry").register(meterRegistry);
        this.totalStatusLockRemove = Counter.builder("content.service.status.lock.operation").tag("operation", "remove").register(meterRegistry);

        init(cacheableProviders, meterRegistry);
    }


    public void init(final Instance<AbstractCacheableContentProvider<?>> cacheableProviders, final MeterRegistry meterRegistry) {

        for (AbstractCacheableContentProvider<?> cacheableProvider : cacheableProviders) {
            Gauge.builder("content.service.content.size", cacheableProvider::getContentSize).tag("provider", cacheableProvider.type().getType()).register(meterRegistry);
            Gauge.builder("content.service.dependants.size", cacheableProvider::getDependantsSize).tag("provider", cacheableProvider.type().getType()).register(meterRegistry);
            Gauge.builder("content.service.dependencies.size", cacheableProvider::getDependenciesSize).tag("provider", cacheableProvider.type().getType()).register(meterRegistry);
        }
    }

    public void incrementLockStatusRead() {
        this.totalStatusLockRead.increment();
    }


    public void incrementLockStatusRemove() {
        this.totalStatusLockRemove.increment();
    }

    public void incrementLockStatusWrite() {
        this.totalStatusLockWrite.increment();
    }

    public void incrementLockStatusUpdate() {
        this.totalStatusLockUpdate.increment();
    }

    public void incrementLockStatusRetry() {
        this.totalStatusLockRetry.increment();
    }


    public void incrementDatabaseUpdate() {
        this.totalDatabaseUpdate.increment();
    }

}
