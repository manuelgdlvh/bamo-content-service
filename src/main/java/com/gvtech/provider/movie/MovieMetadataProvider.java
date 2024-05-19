package com.gvtech.provider.movie;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.Metadata;
import com.gvtech.model.movie.Movie;
import com.gvtech.provider.content.MetadataProvider;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
@Startup
public class MovieMetadataProvider extends AbstractCacheableContentProvider<ContentWithMetadata> {

    // PATTERN LANGUAGE/COUNTRY/ID
    @Inject
    MetadataProvider metadataProvider;
    @Inject
    MovieProvider movieProvider;

    public MovieMetadataProvider() {
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

        final Movie movie = movieProvider.get(contentId);
        if (movie == null) {
            return null;
        }

        final ContentBuild<ContentWithMetadata> contentBuild = new ContentBuild<>();
        final ContentId metadataContentId = new ContentId(String.format("%s/%s", "MOVIE", movie.id()));
        Metadata metadata = metadataProvider.get(metadataContentId);
        contentBuild.setContent(ContentWithMetadata.buildMovie(movie, metadata));


        // AUNQUE METADATA PUEDA SER NULL, SE AÑADE LA DEPENDENCIA POR SI SE CACHEA SIENDO NULL QUE SE PUEDA PROPAGAR EL UPDATE CUANDO SE CREA REGISTRO EN BBDD
        contentBuild.addDependency(metadataContentId, metadataProvider.type());
        contentBuild.addDependency(contentId, movieProvider.type());

        return contentBuild;
    }


    @Override
    public ContentType type() {
        return new ContentType("MOVIE_METADATA");
    }
}
