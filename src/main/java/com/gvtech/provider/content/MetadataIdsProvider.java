package com.gvtech.provider.content;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.repository.ContentMetadataRepository;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;


@ApplicationScoped
@Startup
public class MetadataIdsProvider extends AbstractCacheableContentProvider<Set<MetadataIdsProvider.MetadataId>> {

    // PATTERN TYPE/FILTER_BY
    @Inject
    ContentMetadataRepository contentMetadataRepository;

    public MetadataIdsProvider() {
        super((long) (1000 * 60));
    }


    @Override
    public ContentType type() {
        return new ContentType("METADATA_IDS");
    }

    @Override
    public boolean equals(Set<MetadataId> o1, Set<MetadataId> o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<Set<MetadataId>> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 2) {
            return null;
        }

        final String type = parts[0];
        final String filterBy = parts[1];

        final Set<MetadataId> ids = contentMetadataRepository.findAllIdsBy(type, filterBy);
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(ids);
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class MetadataId {
        private long id;
        private long contentId;
    }
}



