package com.gvtech.provider.tv;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.ContentWithMetadataList;
import com.gvtech.provider.content.ContentSearchableIdsProvider;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;


@ApplicationScoped
@Startup
public class TvMetadataSearchableListProvider implements ContentProvider<ContentWithMetadataList> {
    // PATTERN LANGUAGE/COUNTRY/KEYWORD/PAGE

    private static final int PAGE_SIZE = 15;
    @Inject
    ContentSearchableIdsProvider contentSearchableIdsProvider;
    @Inject
    TvMetadataProvider tvMetadataProvider;


    @Override
    public ContentWithMetadataList get(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 4) {
            return null;
        }

        final String language = parts[0];
        final String country = parts[1];
        final String keyword = parts[2];
        final int pageNumber = Integer.parseInt(parts[3]);
        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s/%s", language, "TV", keyword));
        final List<Long> contentIds = this.contentSearchableIdsProvider.get(metadataIdsContentId);
        if (contentIds == null) {
            return null;
        }

        // PAGINATION
        int fromIndex = pageNumber * PAGE_SIZE;
        if (fromIndex >= contentIds.size()) {
            return null;
        }

        int toIndex = fromIndex + PAGE_SIZE;
        if (toIndex > contentIds.size()) {
            toIndex = contentIds.size();
        }

        final List<Long> tvMetadataIdsPaged = new ArrayList<>(contentIds).subList(fromIndex, toIndex);
        final List<ContentWithMetadata> tvsWithMetadata;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<ContentWithMetadata>> subtasks =
                    tvMetadataIdsPaged.stream().map(tvId -> scope.fork(() -> findContentMetadata(tvId, language, country))).toList();
            scope.join();
            tvsWithMetadata = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        if (tvsWithMetadata.isEmpty()) {
            return null;
        }

        return new ContentWithMetadataList(tvsWithMetadata);
    }

    private ContentWithMetadata findContentMetadata(final Long id, final String language, final String country) {
        final ContentId tvMetadataContentId = new ContentId(String.format("%s/%s/%s", language, country, id));
        final ContentWithMetadata withMetadata = tvMetadataProvider.get(tvMetadataContentId);
        if (withMetadata == null) {
            return null;
        }
        return withMetadata;
    }


    @Override
    public ContentType type() {
        return new ContentType("TV_METADATA_SEARCHABLE_LIST");
    }
}
