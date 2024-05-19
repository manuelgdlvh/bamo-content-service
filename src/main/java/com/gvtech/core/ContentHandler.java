package com.gvtech.core;


import com.gvtech.core.expiration.ContentExpiration;
import com.gvtech.core.expiration.ContentExpirationProcessor;
import com.gvtech.core.remove.ContentRemove;
import com.gvtech.core.remove.ContentRemoveQueueProcessor;
import com.gvtech.core.update.ContentUpdate;
import com.gvtech.core.update.ContentUpdateQueueProcessor;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Startup
public class ContentHandler {
    private final Map<ContentType, Map<ContentId, Set<ContentDependency>>> dependencies;
    private final Map<ContentType, Map<ContentId, Set<ContentDependant>>> dependants;

    @Inject
    ContentUpdateQueueProcessor updateProcessor;
    @Inject
    ContentRemoveQueueProcessor removeProcessor;
    @Inject
    ContentExpirationProcessor expirationProcessor;

    public ContentHandler(Instance<ContentProvider<?>> contentProviders) {
        final Map<ContentType, Map<ContentId, Set<ContentDependency>>> dependencies = new HashMap<>();
        final Map<ContentType, Map<ContentId, Set<ContentDependant>>> dependants = new HashMap<>();


        for (ContentProvider<?> contentProvider : contentProviders) {
            dependencies.put(contentProvider.type(), new ConcurrentHashMap<>());
            dependants.put(contentProvider.type(), new ConcurrentHashMap<>());
        }


        this.dependencies = Collections.unmodifiableMap(dependencies);
        this.dependants = Collections.unmodifiableMap(dependants);
    }


    public void enqueueUpdate(final ContentUpdate contentUpdate) {
        this.updateProcessor.enqueue(contentUpdate);
    }

    public void enqueueRemove(final ContentRemove contentRemove) {
        this.removeProcessor.enqueue(contentRemove);
    }


    /* EXPIRATION */
    public void removeExpiration(final ContentExpiration contentExpiration) {
        this.expirationProcessor.remove(contentExpiration);
    }

    public void enqueueExpiration(final ContentExpiration contentExpiration) {
        this.expirationProcessor.enqueue(contentExpiration);
    }

    public void enqueueExpirationIfAbsent(final ContentExpiration contentExpiration) {
        this.expirationProcessor.enqueueIfAbsent(contentExpiration);
    }


    /* DEPENDENCY */


    public void addDependency(final ContentDependency contentDependency, final ContentDependant contentDependant) {

        final Map<ContentId, Set<ContentDependant>> dependants = this.dependants.get(contentDependency.getType());
        final Map<ContentId, Set<ContentDependency>> dependencies = this.dependencies.get(contentDependant.getType());

        if (dependants == null || dependencies == null) {
            return;
        }

        if (!dependants.containsKey(contentDependency.getId())) {
            dependants.put(contentDependency.getId(), ConcurrentHashMap.newKeySet());
        }

        if (!dependencies.containsKey(contentDependant.getId())) {
            dependencies.put(contentDependant.getId(), ConcurrentHashMap.newKeySet());
        }

        dependants.get(contentDependency.getId()).add(contentDependant);
        dependencies.get(contentDependant.getId()).add(contentDependency);
    }


    public void refreshDependencies(final ContentId id, final ContentType type, final Set<ContentDependency> currentDependencies) {
        final List<ContentDependency> oldDependencies = this.getDependencies(id, type);

        // REMOVE
        for (ContentDependency oldDependency : oldDependencies) {
            if (!currentDependencies.contains(oldDependency)) {
                Log.info(String.format("removing dependency of %s - %s -> %s", id.getId(), type.getType(), oldDependency));
                this.removeDependant(type, id, oldDependency);
                this.removeDependency(type, id, oldDependency);
            }
        }

        // ADD
        for (ContentDependency currentDependency : currentDependencies) {
            if (!oldDependencies.contains(currentDependency)) {
                Log.info(String.format("adding dependency of %s - %s -> %s", id.getId(), type.getType(), currentDependency));
                addDependency(currentDependency, new ContentDependant(id, type));
            }
        }

    }


    public void removeAllDependencies(final ContentId id, final ContentType type) {
        if (!this.hasDependencies(id, type)) {
            return;
        }

        for (ContentDependency dependency : this.getDependencies(id, type)) {
            this.removeDependant(type, id, dependency);
            this.removeDependency(type, id, dependency);
        }
    }

    private void removeDependant(final ContentType dependantType, final ContentId dependantId, final ContentDependency dependency) {
        final Map<ContentId, Set<ContentDependant>> dependantMap = this.dependants.get(dependency.getType());
        final Set<ContentDependant> dependants = dependantMap.get(dependency.getId());

        if (dependants == null || dependants.isEmpty()) {
            return;
        }

        dependants.remove(new ContentDependant(dependantId, dependantType));
    }

    private void removeDependency(final ContentType dependantType, final ContentId dependantId, final ContentDependency dependency) {
        final Map<ContentId, Set<ContentDependency>> dependencyMap = this.dependencies.get(dependantType);
        final Set<ContentDependency> dependencies = dependencyMap.get(dependantId);

        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        dependencies.remove(new ContentDependency(dependency.getId(), dependency.getType()));
    }


    public List<ContentDependant> getDependants(final ContentId id, final ContentType type) {
        final Map<ContentId, Set<ContentDependant>> dependants = this.dependants.get(type);
        if (!dependants.containsKey(id)) {
            return new ArrayList<>();
        }

        return dependants.get(id).stream().toList();
    }

    public boolean hasDependants(final ContentId id, final ContentType type) {
        final Map<ContentId, Set<ContentDependant>> dependants = this.dependants.get(type);

        if (!dependants.containsKey(id)) {
            return false;
        }

        final Set<ContentDependant> dependantSet = dependants.get(id);

        return dependantSet != null && !dependantSet.isEmpty();
    }

    public boolean hasDependencies(final ContentId id, final ContentType type) {
        final Map<ContentId, Set<ContentDependency>> dependencies = this.dependencies.get(type);

        if (!dependencies.containsKey(id)) {
            return false;
        }

        final Set<ContentDependency> dependencySet = dependencies.get(id);

        return dependencySet != null && !dependencySet.isEmpty();
    }

    public List<ContentDependency> getDependencies(final ContentId id, final ContentType type) {
        final Map<ContentId, Set<ContentDependency>> dependencies = this.dependencies.get(type);

        if (!dependencies.containsKey(id)) {
            return new ArrayList<>();
        }

        final Set<ContentDependency> dependencySet = dependencies.get(id);
        return dependencySet.stream().toList();
    }


    // TODO: CHANGE TO CALCULATED IN EACH ADD / REMOVE
    protected int getDependantsSize(final ContentType contentType) {
        int size = 0;
        final Map<ContentId, Set<ContentDependant>> dependants = this.dependants.get(contentType);
        for (var entry : dependants.entrySet()) {
            size += entry.getValue().size();
        }

        return size;
    }


    // TODO: CHANGE TO CALCULATED IN EACH ADD / REMOVE
    protected int getDependenciesSize(final ContentType contentType) {
        int size = 0;
        final Map<ContentId, Set<ContentDependency>> dependencies = this.dependencies.get(contentType);
        for (var entry : dependencies.entrySet()) {
            size += entry.getValue().size();
        }

        return size;
    }

}
