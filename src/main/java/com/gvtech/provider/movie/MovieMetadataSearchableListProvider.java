package com.gvtech.provider.movie;

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
public class MovieMetadataSearchableListProvider implements ContentProvider<ContentWithMetadataList> {
    // PATTERN LANGUAGE/COUNTRY/KEYWORD/PAGE

    private static final int PAGE_SIZE = 15;
    @Inject
    ContentSearchableIdsProvider contentSearchableIdsProvider;
    @Inject
    MovieMetadataProvider movieMetadataProvider;


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
        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s/%s", language, "MOVIE", keyword));
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

        final List<Long> movieMetadataIdsPaged = new ArrayList<>(contentIds).subList(fromIndex, toIndex);
        final List<ContentWithMetadata> moviesWithMetadata;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<ContentWithMetadata>> subtasks =
                    movieMetadataIdsPaged.stream().map(movieId -> scope.fork(() -> findContentMetadata(movieId, language, country))).toList();
            scope.join();
            moviesWithMetadata = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        if (moviesWithMetadata.isEmpty()) {
            return null;
        }

        return new ContentWithMetadataList(moviesWithMetadata);
    }

    private ContentWithMetadata findContentMetadata(final Long id, final String language, final String country) {
        final ContentId movieMetadataContentId = new ContentId(String.format("%s/%s/%s", language, country, id));
        final ContentWithMetadata withMetadata = movieMetadataProvider.get(movieMetadataContentId);
        if (withMetadata == null) {
            return null;
        }
        return withMetadata;
    }


    @Override
    public ContentType type() {
        return new ContentType("MOVIE_METADATA_SEARCHABLE_LIST");
    }
}
