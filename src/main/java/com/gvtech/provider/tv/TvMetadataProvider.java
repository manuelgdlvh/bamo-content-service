package com.gvtech.provider.tv;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.Metadata;
import com.gvtech.model.tv.Tv;
import com.gvtech.provider.content.MetadataProvider;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
@Startup
public class TvMetadataProvider extends AbstractCacheableContentProvider<ContentWithMetadata> {

    // PATTERN LANGUAGE/COUNTRY/ID
    @Inject
    MetadataProvider metadataProvider;
    @Inject
    TvProvider tvProvider;

    public TvMetadataProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(ContentWithMetadata o1, ContentWithMetadata o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<ContentWithMetadata> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 3) {
            return null;
        }

        final Tv tv = tvProvider.get(contentId);
        if (tv == null) {
            return null;
        }


        final ContentBuild<ContentWithMetadata> contentBuild = new ContentBuild<>();
        final ContentId metadataContentId = new ContentId(String.format("%s/%s", "TV", tv.id()));
        Metadata metadata = metadataProvider.get(metadataContentId);

        contentBuild.setContent(ContentWithMetadata.buildTv(tv, metadata));
        contentBuild.addDependency(metadataContentId, metadataProvider.type());
        contentBuild.addDependency(contentId, tvProvider.type());

        return contentBuild;
    }


    @Override
    public ContentType type() {
        return new ContentType("TV_METADATA");
    }
}
