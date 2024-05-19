package com.gvtech.provider.content;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.ContentMetadataEntity;
import com.gvtech.model.Metadata;
import com.gvtech.repository.ContentMetadataRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
@Startup
public class MetadataProvider extends AbstractCacheableContentProvider<Metadata> {
    // PATTERN CONTENT_TYPE/CONTENT_ID

    @Inject
    ContentMetadataRepository contentMetadataRepository;

    public MetadataProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(Metadata o1, Metadata o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<Metadata> build(ContentId contentId) {
        final String contentIdSplit = contentId.getId();
        final String[] parts = contentIdSplit.split("/");
        if (parts.length < 2) {
            return null;
        }

        final String contentType = parts[0];
        final Long id = Long.valueOf(parts[1]);

        final ContentMetadataEntity contentMetadata = contentMetadataRepository.findBy(contentType, id);
        if (contentMetadata == null) {
            return null;
        }

        return new ContentBuild<>(Metadata.map(contentMetadata));
    }


    @Override
    public ContentType type() {
        return new ContentType("METADATA");
    }
}
