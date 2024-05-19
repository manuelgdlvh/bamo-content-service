package com.gvtech.provider.tv;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.ContentWithMetadataList;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.StructuredTaskScope;


@ApplicationScoped
@Startup
public class TvMetadataListProvider extends AbstractCacheableContentProvider<ContentWithMetadataList> {
    // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS/FILTER_BY/PAGE

    private static final int PAGE_SIZE = 15;
    @Inject
    TvMetadataIdsProvider tvMetadataIdsProvider;
    @Inject
    TvMetadataProvider tvMetadataProvider;

    public TvMetadataListProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(ContentWithMetadataList o1, ContentWithMetadataList o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<ContentWithMetadataList> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 7) {
            return null;
        }

        final String language = parts[0];
        final String country = parts[1];
        final String platforms = parts[2];
        final String genres = parts[3];
        final String years = parts[4];
        final String filterBy = parts[5];
        final int pageNumber = Integer.parseInt(parts[6]);

        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, country, platforms, genres, years, filterBy));
        final Set<Long> tvMetadataIds = this.tvMetadataIdsProvider.get(metadataIdsContentId);
        if (tvMetadataIds == null) {
            return null;
        }

        // PAGINATION
        int fromIndex = pageNumber * PAGE_SIZE;
        if (fromIndex >= tvMetadataIds.size()) {
            return null;
        }

        int toIndex = fromIndex + PAGE_SIZE;
        if (toIndex > tvMetadataIds.size()) {
            toIndex = tvMetadataIds.size();
        }

        final ContentBuild<ContentWithMetadataList> contentBuild = new ContentBuild<>();
        final List<Long> tvMetadataIdsPaged = new ArrayList<>(tvMetadataIds).subList(fromIndex, toIndex);


        final List<ContentWithMetadata> tvsWithMetadata;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<ContentWithMetadata>> subtasks =
                    tvMetadataIdsPaged.stream().map(tvId -> scope.fork(() -> findContentMetadataAndRegisterDependency(tvId, language, country, contentBuild))).toList();
            scope.join();
            tvsWithMetadata = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        if (tvsWithMetadata.isEmpty()) {
            return null;
        }

        contentBuild.addDependency(metadataIdsContentId, tvMetadataIdsProvider.type());
        contentBuild.setContent(new ContentWithMetadataList(tvsWithMetadata));

        return contentBuild;
    }


    private ContentWithMetadata findContentMetadataAndRegisterDependency(final Long tvId, final String language, final String country,
                                                                         final ContentBuild<ContentWithMetadataList> contentBuild) {
        final ContentId tvMetadataContentId = new ContentId(String.format("%s/%s/%s", language, country, tvId));
        final ContentWithMetadata withMetadata = tvMetadataProvider.get(tvMetadataContentId);
        if (withMetadata == null) {
            return null;
        }

        contentBuild.addDependency(tvMetadataContentId, tvMetadataProvider.type());
        return withMetadata;
    }

    @Override
    public ContentType type() {
        return new ContentType("TV_METADATA_LIST");
    }
}
