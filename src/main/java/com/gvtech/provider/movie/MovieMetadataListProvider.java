package com.gvtech.provider.movie;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.ContentWithMetadataList;
import com.gvtech.support.ContentBucket;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@ApplicationScoped
@Startup
public class MovieMetadataListProvider extends AbstractCacheableContentProvider<ContentWithMetadataList> {
    // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS/FILTER_BY/PAGE

    private static final int PAGE_SIZE = 15;
    @Inject
    MovieMetadataIdsProvider movieMetadataIdsProvider;
    @Inject
    MovieMetadataProvider movieMetadataProvider;

    public MovieMetadataListProvider() {
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
        final Set<Long> movieMetadataIds = this.movieMetadataIdsProvider.get(metadataIdsContentId);
        if (movieMetadataIds == null) {
            return null;
        }

        // PAGINATION
        int fromIndex = pageNumber * PAGE_SIZE;
        if (fromIndex >= movieMetadataIds.size()) {
            return null;
        }

        int toIndex = fromIndex + PAGE_SIZE;
        if (toIndex > movieMetadataIds.size()) {
            toIndex = movieMetadataIds.size();
        }

        final ContentBuild<ContentWithMetadataList> contentBuild = new ContentBuild<>();
        final List<Long> movieMetadataIdsPaged = new ArrayList<>(movieMetadataIds).subList(fromIndex, toIndex);

        final ContentBucket<ContentWithMetadata> bucket = this.emptyBucket();
        for (Long movieId : movieMetadataIdsPaged) {
            bucket.add(() -> findContentMetadataAndRegisterDependency(movieId, language, country, contentBuild));
        }

        final List<ContentWithMetadata> moviesWithMetadata = bucket.get();
        if (moviesWithMetadata.isEmpty()) {
            return null;
        }

        contentBuild.addDependency(metadataIdsContentId, movieMetadataIdsProvider.type());
        contentBuild.setContent(new ContentWithMetadataList(moviesWithMetadata));

        return contentBuild;
    }


    private ContentWithMetadata findContentMetadataAndRegisterDependency(final Long id, final String language, final String country,
                                                                         final ContentBuild<ContentWithMetadataList> contentBuild) {
        final ContentId movieMetadataContentId = new ContentId(String.format("%s/%s/%s", language, country, id));
        final ContentWithMetadata withMetadata = movieMetadataProvider.get(movieMetadataContentId);
        if (withMetadata == null) {
            return null;
        }

        contentBuild.addDependency(movieMetadataContentId, movieMetadataProvider.type());
        return withMetadata;
    }

    @Override
    public ContentType type() {
        return new ContentType("MOVIE_METADATA_LIST");
    }
}
